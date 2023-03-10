package com.example.warehousemanage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserlistRecycler extends AppCompatActivity {

    private RecyclerView rcv_user;
    private UserAdapter adapter;
    private List<User> userList;
    private FirebaseFirestore mFStore;
    private String warehouse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userlist_recycler);
        rcv_user = findViewById(R.id.rcv_user);
        mFStore = FirebaseFirestore.getInstance();
        rcv_user.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<User>();
        Bundle extras = getIntent().getExtras();
        warehouse = extras.getString("warehouse");
        getDataList();
        adapter = new UserAdapter(UserlistRecycler.this,userList);
        rcv_user.setAdapter(adapter);
    }

    private void getDataList() {
        mFStore.collection("Users")
                .whereEqualTo("IsUserOf",warehouse)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                        for(DocumentSnapshot snapshot: snapshotList){
                            String name = snapshot.get("FullName").toString();
                            String age = snapshot.get("Age").toString();
                            String email = snapshot.get("Email").toString();
                            String isuser = snapshot.get("IsUserOf").toString();
                            String phone = snapshot.get("Phone").toString();
                            Log.d("Sendinginfo",name);
                            userList.add(new User(age,email,name,isuser,phone));
                        }
                        adapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Sendinginfo",e.getMessage());
                    }
                });
    }

    public void backArrowFunc(View view) {
        Intent intent = new Intent(UserlistRecycler.this,MainScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}