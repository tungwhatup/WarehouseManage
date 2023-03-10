package com.example.warehousemanage;

import android.graphics.Color;
import android.os.Bundle;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.warehousemanage.databinding.ActivityPieChartBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class PieChartAct extends AppCompatActivity {

    private FirebaseUser user;
    private String userID;
    private FirebaseFirestore mFStore;
    private PieChart pieChart;
    private ArrayList<PieEntry> pieEntries;
    private String strStartDate,strEndDate,strSingleOrMulti,strBarCodeSingle,strInOrOut,strQtyRevenue,interval;
    private String warehouse;
    private HashMap<String,String> priceMap;
    private Date startDate,endDate;
    private Timestamp startDateStamp,endDateStamp;
    private Map<String, Integer> codeMap;
    private Map<String, Float> revenueMap;
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
        setContentView(R.layout.activity_pie_chart);
        pieChart = findViewById(R.id.pieChart);
        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();
        mFStore = FirebaseFirestore.getInstance();
        Bundle extras = getIntent().getExtras();
        strStartDate = extras.getString("strStartDate");
        strEndDate = extras.getString("strEndDate");
        strSingleOrMulti = extras.getString("singleOrMulti");
        strBarCodeSingle = extras.getString("single_code");
        strInOrOut = extras.getString("inOrOut");
        strQtyRevenue = extras.getString("qtyOrRev");
        interval = extras.getString("interval");
        warehouse = extras.getString("warehouse");
        priceMap = (HashMap<String, String>) getIntent().getSerializableExtra("priceMap");
        SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH);
        try {
            startDate = format.parse(strStartDate);
            endDate = format.parse(strEndDate);
        } catch (Exception e) {
            Log.d("Sendinginfo",e.getMessage());
        }
        startDateStamp = new Timestamp(startDate);
        endDateStamp = new Timestamp(endDate);
        pieEntries = new ArrayList<>();
        codeMap = new HashMap<>();
        revenueMap = new HashMap<>();
        if(strSingleOrMulti.equals("Multiple")) getDataForMulti();
        else getDataForSingle();
        addMyBooleanListener(new VariableChangeListener() {
            @Override
            public void onVariableChanged() {
                PieDataSet pieDataSet;
                if(strQtyRevenue.equals("Quantity")){
                    for(String i : codeMap.keySet()){
                        pieEntries.add(new PieEntry(codeMap.get(i),i));
                    }
                    pieDataSet = new PieDataSet(pieEntries,"Number of Product");
                }
                else {
                    for(String i : codeMap.keySet()){
                        pieEntries.add(new PieEntry(revenueMap.get(i),i));
                    }
                    pieDataSet = new PieDataSet(pieEntries,"Revenue");
                }
                pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                pieDataSet.setValueTextColor(Color.BLACK);
                pieDataSet.setValueTextSize(16f);
                PieData pieData = new PieData(pieDataSet);
                pieChart.setData(pieData);
                pieChart.getDescription().setEnabled(false);
                pieChart.setCenterText(strQtyRevenue);
                pieChart.animate();
                pieChart.invalidate();
            }
        });


    }

    private void getDataForSingle() {
        SimpleDateFormat format1;
        if(interval.equals("Daily"))
            format1 = new SimpleDateFormat("MMM dd yyyy",Locale.ENGLISH);
        else if(interval.equals("Monthly"))
            format1 = new SimpleDateFormat("MMM yyyy",Locale.ENGLISH);
        else format1 = new SimpleDateFormat("yyyy",Locale.ENGLISH);
        mFStore.collection(strInOrOut)
                .whereEqualTo("Product Code",strBarCodeSingle)
                .whereEqualTo("BelongsTo",warehouse)
                .whereGreaterThanOrEqualTo("Time of submit",startDateStamp)
                .whereLessThanOrEqualTo("Time of submit",endDateStamp)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                        for(DocumentSnapshot snapshot: snapshotList){
                            Timestamp timestamp = (Timestamp) snapshot.get("Time of submit");
                            Date date = timestamp.toDate();
                            String day = format1.format(date);
                            String tempQtyStr = snapshot.get("Quantity").toString();
                            int temp = Integer.parseInt(tempQtyStr);
                            Integer count = codeMap.get(day);
                            if(count == null){
                                codeMap.put(day,temp);
                            }
                            else codeMap.put(day,temp + count);
                            String strQty = snapshot.get("Quantity").toString();
                            Log.d("Sendinginfoto",day + " ," + strQty);
                        }
                        float price = Float.parseFloat(priceMap.get(strBarCodeSingle));
                        for(String key : codeMap.keySet()){
                            revenueMap.put(key,codeMap.get(key)*price);
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

    private void getDataForMulti() {
        mFStore.collection(strInOrOut)
                .whereEqualTo("BelongsTo",warehouse)
                .whereGreaterThanOrEqualTo("Time of submit",startDateStamp)
                .whereLessThanOrEqualTo("Time of submit",endDateStamp)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                        for(DocumentSnapshot snapshot: snapshotList){
                            String tempCode = snapshot.getData().get("Product Code").toString();
                            String tempQtyStr = snapshot.get("Quantity").toString();
                            int temp = Integer.parseInt(tempQtyStr);
                            Integer count = codeMap.get(tempCode);
                            if(count == null){
                                codeMap.put(tempCode,temp);
                            }
                            else codeMap.put(tempCode,temp + count);
                            String strQty = snapshot.get("Quantity").toString();
                            Log.d("Sendinginfoto",tempCode + " ," + strQty);
                        }
                        for(String key : codeMap.keySet()){
                            float price = Float.parseFloat(priceMap.get(key));
                            revenueMap.put(key,price*codeMap.get(key));
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
}