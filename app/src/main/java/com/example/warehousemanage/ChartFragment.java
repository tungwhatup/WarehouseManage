package com.example.warehousemanage;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.BarChart;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.A;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChartFragment extends Fragment {

    private Spinner spinner_in_out,spinner_qty_rev,spinner_single_multi,spinner_choose,spinner_periodicity;
    private static List<String> collections,qty_rev,single_multi,choose,periodicity;
    String inOrOut = "Out",qtyOrRev,singleOrMulti,single_code,interval,warehouse;
    private DatePickerDialog startDate, endDate;
    private ConstraintLayout layout_choose;
    private Button startDatePicker,endDatePicker,btn_bar_chart,btn_pie_chart;
    private FirebaseFirestore mFStore;
    private FirebaseUser user;
    private HashMap<String,String> priceMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        initSpinner_in(view);
        initSpinner_qty(view);
        initSpinner_multi(view);
        initSpinner_choose(view);
        initSpinner_peri(view);
        startDatePicker = view.findViewById(R.id.btn_start_date_picker);
        startDatePicker.setText(getTodaysDate());
        startDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openStartDatePicker(view);
            }
        });
        endDatePicker = view.findViewById(R.id.btn_end_date_picker);
        endDatePicker.setText(getTodaysDate());
        endDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEndDatePicker(view);
            }
        });
        btn_bar_chart = view.findViewById(R.id.btn_bar_chart);
        btn_bar_chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strStartDate = startDatePicker.getText().toString();
                String strEndDate = endDatePicker.getText().toString();
                Intent intent = new Intent(view.getContext(), BarChartAct.class);
                intent.putExtra("strStartDate",strStartDate);
                intent.putExtra("strEndDate",strEndDate);
                intent.putExtra("singleOrMulti",singleOrMulti);
                intent.putExtra("inOrOut",inOrOut);
                intent.putExtra("qtyOrRev",qtyOrRev);
                intent.putExtra("single_code",single_code);
                intent.putExtra("interval",interval);
                intent.putExtra("priceMap", priceMap);
                intent.putExtra("warehouse",warehouse);
                startActivity(intent);
            }
        });
        btn_pie_chart = view.findViewById(R.id.btn_pie_chart);
        btn_pie_chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strStartDate = startDatePicker.getText().toString();
                String strEndDate = endDatePicker.getText().toString();
                Intent intent = new Intent(view.getContext(), PieChartAct.class);
                intent.putExtra("strStartDate",strStartDate);
                intent.putExtra("strEndDate",strEndDate);
                intent.putExtra("singleOrMulti",singleOrMulti);
                intent.putExtra("inOrOut",inOrOut);
                intent.putExtra("qtyOrRev",qtyOrRev);
                intent.putExtra("single_code",single_code);
                intent.putExtra("interval",interval);
                intent.putExtra("priceMap", priceMap);
                intent.putExtra("warehouse",warehouse);
                startActivity(intent);
            }
        });
        initDatePicker();
        mFStore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        String userID = user.getUid();
        priceMap = new HashMap<>();
        mFStore.collection("In Storage")
                .document("Warehouse A")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String,Object> temp = documentSnapshot.getData();
                        for(String key : temp.keySet()){
                            String price = ((HashMap<String, Object>) documentSnapshot.getData().get(key))
                                    .get("Price").toString();
                            priceMap.put(key,price);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Sendinginfo",e.getMessage());
                    }
                });
        mFStore.collection("Users")
                .document(userID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                warehouse = documentSnapshot.get("BelongsTo").toString();
            }
        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Sendinginfo",e.getMessage());
                    }
                });
        return view;

    }

    private void initSpinner_peri(View view) {
        spinner_periodicity = view.findViewById(R.id.spinner_periodicity);
        periodicity = new ArrayList<>();
        periodicity.add("Daily");
        periodicity.add("Monthly");
        periodicity.add("Yearly");
        SpinnerAdapter adapter = new SpinnerAdapter(getContext(),R.layout.spinner_layout_test,periodicity);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner_periodicity.setAdapter(adapter);
        spinner_periodicity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                interval = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initSpinner_choose(View view) {
        spinner_choose = view.findViewById(R.id.spinner_choose);
        choose = new ArrayList<>();
        choose.add("2050002873537");
        choose.add("636983977758");
        choose.add("638932426683");
        choose.add("671607153118");
        choose.add("671607535129");
        choose.add("723079904184");
        choose.add("785901878452");
        choose.add("790576189932");
        choose.add("796709311317");
        choose.add("804428065814");
        SpinnerAdapter adapter = new SpinnerAdapter(getContext(),R.layout.spinner_layout_test,choose);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner_choose.setAdapter(adapter);
        spinner_choose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                single_code = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initSpinner_multi(View view) {
        spinner_single_multi = view.findViewById(R.id.spinner_single_multi);
        layout_choose = view.findViewById(R.id.layout_choose);
        single_multi = new ArrayList<>();
        single_multi.add("Single");
        single_multi.add("Multiple");
        SpinnerAdapter adapter = new SpinnerAdapter(getContext(),R.layout.spinner_layout_test,single_multi);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner_single_multi.setAdapter(adapter);
        spinner_single_multi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                singleOrMulti = adapterView.getItemAtPosition(i).toString();
                if(singleOrMulti.equals("Single")){
                    layout_choose.setVisibility(View.VISIBLE);
                }
                else layout_choose.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initSpinner_qty(View view) {
        spinner_qty_rev = view.findViewById(R.id.spinner_qty_rev);
        qty_rev = new ArrayList<>();
        qty_rev.add("Quantity");
        qty_rev.add("Revenue");
        SpinnerAdapter adapter = new SpinnerAdapter(getContext(),R.layout.spinner_layout_test,qty_rev);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner_qty_rev.setAdapter(adapter);
        spinner_qty_rev.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                qtyOrRev = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initSpinner_in(View view) {
        spinner_in_out = view.findViewById(R.id.spinner_in_out);
        collections = new ArrayList<>();
        collections.add("Sale");
        collections.add("Purchase");
        SpinnerAdapter collectionAdapter = new SpinnerAdapter(getContext(), R.layout.spinner_layout_test,collections);
        collectionAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner_in_out.setAdapter(collectionAdapter);
        spinner_in_out.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getItemAtPosition(i).toString().equals("Sale")) inOrOut = "Out";
                else inOrOut = "In";
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private String getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day,month,year);
    }
    private String makeDateString(int day, int month, int year) {
        return getMonthFormat(month) + " " + day + " " + year;
    }
    private String getMonthFormat(int month) {
        if(month == 1) return "Jan";
        if(month == 2) return "Feb";
        if(month == 3) return "Mar";
        if(month == 4) return "Apr";
        if(month == 5) return "May";
        if(month == 6) return "Jun";
        if(month == 7) return "Jul";
        if(month == 8) return "Aug";
        if(month == 9) return "Sep";
        if(month == 10) return "Oct";
        if(month == 11) return "Nov";
        if(month == 12) return "Dec";
        return "Jan";
    }
    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date = makeDateString(day,month,year);
                startDatePicker.setText(date);
            }
        };
        DatePickerDialog.OnDateSetListener dateSetListener1 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date = makeDateString(day,month,year);
                endDatePicker.setText(date);
            }
        };
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int style = AlertDialog.THEME_HOLO_LIGHT;
        startDate = new DatePickerDialog(getContext(), style,dateSetListener, year, month,day);
        endDate= new DatePickerDialog(getContext(), style,dateSetListener1,year,month,day);
    }
    public void openStartDatePicker(View view) {
        startDate.show();
    }

    public void openEndDatePicker(View view) {
        endDate.show();
    }
}