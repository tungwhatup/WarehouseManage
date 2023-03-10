package com.example.warehousemanage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.ReceiverCallNotAllowedException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StorageRecycler extends AppCompatActivity {

    private RecyclerView rcv_storage;
    private InStorageAdapter adapter;
    private FirebaseFirestore mFStore;
    private List<Map<String,String>> mapsOfPro;
    private List<String> barcodeList;
    private Map<String,Object> temp;
    private String warehouse;
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
        setContentView(R.layout.activity_storage_recycler);

        rcv_storage = findViewById(R.id.rcv_storage);
        mFStore = FirebaseFirestore.getInstance();
        Bundle extras = getIntent().getExtras();
        warehouse = extras.getString("warehouse");
        adapter = new InStorageAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        rcv_storage.setLayoutManager(linearLayoutManager);
        mapsOfPro = new ArrayList<>();
        barcodeList = new ArrayList<>();
        temp = new HashMap<>();
        getDataList();
        adapter.setData(mapsOfPro,barcodeList);
        rcv_storage.setAdapter(adapter);
        addMyBooleanListener(new VariableChangeListener() {
            @Override
            public void onVariableChanged() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void getDataList() {
        SimpleDateFormat format1 = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH);
        mFStore.collection("In Storage")
                .document(warehouse)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        temp = documentSnapshot.getData();
                        for(String key : temp.keySet()){
                            barcodeList.add(key);
                            String currency = ((HashMap<String, Object>) documentSnapshot.getData().get(key))
                                    .get("Currency").toString();
                            String inStr = ((HashMap<String, Object>) documentSnapshot.getData().get(key))
                                    .get("In").toString();
                            String inStorage = ((HashMap<String, Object>) documentSnapshot.getData().get(key))
                                    .get("In Storage").toString();
                            String manufacturer = ((HashMap<String, Object>) documentSnapshot.getData().get(key))
                                    .get("Manufacturer").toString();
                            String price = ((HashMap<String, Object>) documentSnapshot.getData().get(key))
                                    .get("Price").toString();
                            Timestamp timestamp = (Timestamp) ((HashMap<String, Object>) documentSnapshot.getData().get(key))
                                    .get("Since");
                            Date date = timestamp.toDate();
                            String since = format1.format(date);
                            String title = ((HashMap<String, Object>) documentSnapshot.getData().get(key))
                                    .get("Title").toString();
                            Map<String,String> map = new HashMap<>();
                            map.put("Currency",currency);
                            map.put("In",inStr);
                            map.put("In Storage",inStorage);
                            map.put("Manufacturer",manufacturer);
                            map.put("Price",price);
                            map.put("Since",since);
                            map.put("Title",title);
                            mapsOfPro.add(map);
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

    public void backArrowFunc(View view) {
        Intent intent = new Intent(StorageRecycler.this,MainScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}