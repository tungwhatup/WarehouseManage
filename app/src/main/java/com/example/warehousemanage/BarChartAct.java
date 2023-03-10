package com.example.warehousemanage;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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

public class BarChartAct extends AppCompatActivity {

    private FirebaseUser user;
    private String userID;
    private FirebaseFirestore mFStore;
    private String strStartDate,strEndDate,strSingleOrMulti,strBarCodeSingle,strInOrOut,strQtyRevenue,interval;
    private String warehouse;
    private HashMap<String,String> priceMap;
    private Date startDate,endDate;
    private Timestamp startDateStamp,endDateStamp;
    private BarChart barChart;
    private ArrayList<BarEntry> barEntries;
    private ArrayList<String> labelsName;
    private ArrayList<Integer> qtyOfItem;
    private ArrayList<Float> revenue;
    private float price = 0;
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart);
        barChart = findViewById(R.id.barChart);
        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();
        mFStore = FirebaseFirestore.getInstance();
        DocumentReference df = mFStore.collection("Users").document(userID);
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
        SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy",Locale.ENGLISH);
        try {
            startDate = format.parse(strStartDate);
            endDate = format.parse(strEndDate);
        } catch (Exception e) {
            Log.d("Sendinginfo",e.getMessage());
        }
        startDateStamp = new Timestamp(startDate);
        endDateStamp = new Timestamp(endDate);
        barEntries = new ArrayList<>();
        labelsName = new ArrayList<>();
        qtyOfItem = new ArrayList<>();
        revenue = new ArrayList<>();
        if(strSingleOrMulti.equals("Multiple")) getDataForMUlti();
        else getDataForSingle();

        addMyBooleanListener(new VariableChangeListener() {
            @Override
            public void onVariableChanged() {
                if(strQtyRevenue.equals("Revenue")){
                    for(int i = 0; i < labelsName.size(); i++){
                        barEntries.add(new BarEntry( i, revenue.get(i)));
                    }
                }
                else {
                    for(int i = 0; i < labelsName.size(); i++){
                        barEntries.add(new BarEntry( i, qtyOfItem.get(i)));
                    }
                }
                String chartLabel = "";
                if(strQtyRevenue.equals("Quantity")){
                    if(strSingleOrMulti.equals("Multiple")) chartLabel = "Number of product";
                    else chartLabel = "Number of product: " + strBarCodeSingle;
                }
                else{
                    if(strSingleOrMulti.equals("Multiple")) chartLabel = "USD";
                    else chartLabel = "Revenue from " + strBarCodeSingle;
                }
                BarDataSet barDataSet = new BarDataSet(barEntries,chartLabel);
                barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                barDataSet.setValueTextColor(Color.BLACK);
                barDataSet.setValueTextSize(16f);
                Description description = new Description();
                description.setTextSize(16f);
                description.setText(strQtyRevenue);
                barChart.setDescription(description);
                BarData barData = new BarData(barDataSet);
                barChart.setData(barData);
                XAxis xAxis = barChart.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(labelsName));
                xAxis.setPosition(XAxis.XAxisPosition.TOP);
                xAxis.setGranularity(1f);
                xAxis.setLabelCount(labelsName.size());
                xAxis.setLabelRotationAngle(270);
                barChart.animateY(2000);
                barChart.invalidate();
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
                .orderBy("Time of submit", Query.Direction.ASCENDING)
                .whereEqualTo("Product Code",strBarCodeSingle)
                .whereEqualTo("BelongsTo",warehouse)
                .whereGreaterThanOrEqualTo("Time of submit",startDateStamp)
                .whereLessThanOrEqualTo("Time of submit",endDateStamp)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                        int index = -1;
                        for(DocumentSnapshot snapshot: snapshotList){
                            Timestamp timestamp = (Timestamp) snapshot.get("Time of submit");
                            Date date = timestamp.toDate();
                            String day = format1.format(date);
                            if(!labelsName.contains(day)) {
                                labelsName.add(day);
                                qtyOfItem.add(0);
                                index += 1;
                            }
                            String tempQtyStr = snapshot.get("Quantity").toString();
                            qtyOfItem.set(index,qtyOfItem.get(index)+Integer.parseInt(tempQtyStr));
                            String strQty = snapshot.get("Quantity").toString();
                            Log.d("Sendinginfoto",day + " ," + strQty);
                            Log.d("Sendinginfo",qtyOfItem.toString());
                        }
                        float price = Float.parseFloat(priceMap.get(strBarCodeSingle));
                        for(int i = 0; i < qtyOfItem.size() ; i++){
                            revenue.add(qtyOfItem.get(i)*price);
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

    private void getDataForMUlti(){
        SimpleDateFormat format1;
        if(interval.equals("Daily"))
            format1 = new SimpleDateFormat("MMM dd yyyy",Locale.ENGLISH);
        else if(interval.equals("Monthly"))
            format1 = new SimpleDateFormat("MMM yyyy",Locale.ENGLISH);
        else format1 = new SimpleDateFormat("yyyy",Locale.ENGLISH);
        mFStore.collection(strInOrOut)
                .orderBy("Time of submit", Query.Direction.ASCENDING)
                .whereEqualTo("BelongsTo",warehouse)
                .whereGreaterThanOrEqualTo("Time of submit",startDateStamp)
                .whereLessThanOrEqualTo("Time of submit",endDateStamp)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                        int count = 0;
                        int index = -1;
                        for(DocumentSnapshot snapshot: snapshotList){
                            count++;
                            Timestamp timestamp = (Timestamp) snapshot.get("Time of submit");
                            Date date = timestamp.toDate();
                            String day = format1.format(date);
                            if(!labelsName.contains(day)) {
                                labelsName.add(day);
                                qtyOfItem.add(0);
                                revenue.add(0f);
                                index += 1;
                            }
                            String tempQtyStr = snapshot.get("Quantity").toString();
                            qtyOfItem.set(index,qtyOfItem.get(index)+Integer.parseInt(tempQtyStr));
                            String code = snapshot.get("Product Code").toString();
                            String strPrice = priceMap.get(code);
                            float price = Float.parseFloat(strPrice);
                            revenue.set(index,revenue.get(index) + Float.parseFloat(tempQtyStr)*price);
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