package com.example.warehousemanage;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FirebaseUser user;
    private FirebaseFirestore mFStore;
    private String userID;
    private String warehouse;
    private CardView purchaseCV,saleCV,stockCV,scannerCV,singleCV,logOutCV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();
        mFStore = FirebaseFirestore.getInstance();
        DocumentReference df = mFStore.collection("Users").document(userID);
        purchaseCV = view.findViewById(R.id.cv_purchase);
        saleCV = view.findViewById(R.id.cv_sale);
        stockCV = view.findViewById(R.id.cv_stock);
        scannerCV = view.findViewById(R.id.cv_scanner);
        singleCV = view.findViewById(R.id.cv_single_item);
        logOutCV = view.findViewById(R.id.cv_logout);
        mFStore.collection("Users")
                .document(userID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                warehouse = documentSnapshot.get("IsUserOf").toString();
            }
        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Sendinginfo",e.getMessage());
                    }
                });
        purchaseCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),ProductRecycler.class);
                intent.putExtra("collection","In");
                intent.putExtra("warehouse",warehouse);
                startActivity(intent);
            }
        });
        saleCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),ProductRecycler.class);
                intent.putExtra("collection","Out");
                intent.putExtra("warehouse",warehouse);
                startActivity(intent);
            }
        });
        stockCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),StorageRecycler.class);
                intent.putExtra("warehouse",warehouse);
                startActivity(intent);
            }
        });
        scannerCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),CodeScanner.class);
                intent.putExtra("warehouse",warehouse);
                startActivity(intent);
            }
        });
        singleCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //make new recycler view for each zone, maybe a popup window to choose which zone
                Intent intent = new Intent(getActivity(),UserlistRecycler.class);
                intent.putExtra("warehouse",warehouse);
                startActivity(intent);
            }
        });
        logOutCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(),MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        return view;
    }

}