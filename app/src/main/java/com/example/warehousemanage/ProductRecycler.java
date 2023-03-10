package com.example.warehousemanage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductRecycler extends AppCompatActivity {

    private RecyclerView rcv_product;
    private ProductAdapter productAdapter;
    private FirebaseFirestore mFStore;
    private String inOrOut = "";
    private String warehouse;
    private TextView history_title;
    private List<Product> productList;
    private List<String> employeeList;

    private static List<VariableChangeListener> listeners = new ArrayList<VariableChangeListener>();
    public static boolean checkData =false;

    public static boolean getMyBoolean(){return checkData;};

    public static void setMyBoolean(boolean value){
        checkData = value;
        for(VariableChangeListener l:listeners){
            l.onVariableChanged();
        }
    }

    public static void addMyBooleanListener(VariableChangeListener l){
        listeners.add(l);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_recycler);
        rcv_product = findViewById(R.id.rcv_product);
        mFStore = FirebaseFirestore.getInstance();
        Bundle extras = getIntent().getExtras();
        inOrOut = extras.getString("collection");
        warehouse = extras.getString("warehouse");
        history_title = findViewById(R.id.history_title);
        if(inOrOut.equals("Out")){
            history_title.setText("Sale History");
        }
        productAdapter = new ProductAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        rcv_product.setLayoutManager(linearLayoutManager);

        productList = new ArrayList<>();
        employeeList = new ArrayList<>();
        getDataList();
        productAdapter.setData(productList,employeeList);
        rcv_product.setAdapter(productAdapter);
        addMyBooleanListener(new VariableChangeListener() {
            @Override
            public void onVariableChanged() {
                productAdapter.notifyDataSetChanged();
            }
        });
    }

    private void getDataList() {
        Map<String,String> productNameMap = new HashMap<>();
        SimpleDateFormat format1 = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH);
        mFStore.collection("In Storage")
                .document(warehouse)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String,Object> temp = documentSnapshot.getData();
                        for(String key: temp.keySet()){
                            String title = ((HashMap<String, Object>) documentSnapshot.getData().get(key))
                                    .get("Title").toString();
                            productNameMap.put(key,title);
                        }
                        mFStore.collection(inOrOut)
                                .whereEqualTo("BelongsTo",warehouse)
                                .orderBy("Time of submit", Query.Direction.ASCENDING)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                                        for(DocumentSnapshot snapshot : snapshotList){
                                            Timestamp timestamp = (Timestamp) snapshot.get("Time of submit");
                                            Date date = timestamp.toDate();
                                            String code = (String) snapshot.get("Product Code");
                                            String name = productNameMap.get(code);
                                            String timeSubmit = format1.format(date);
                                            String quantity = (String) snapshot.get("Quantity");
                                            String location = (String) snapshot.get("From or to");
                                            String belongsTo = (String) snapshot.get("BelongsTo");
                                            productList.add(new Product(name,code,timeSubmit,quantity + " items",location,belongsTo));
                                            String employee = (String) snapshot.get("Name");
                                            employeeList.add(employee);
                                        }
                                        setMyBoolean(true);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("Sendinginfo",e.getMessage());
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Sendinginfo",e.getMessage());
                    }
                });
    }


    public void backArrowFunc(View view) {
        Intent intent = new Intent(ProductRecycler.this,MainScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}