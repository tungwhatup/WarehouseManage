package com.example.warehousemanage;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CodeScanner extends AppCompatActivity {

    private Button btn_scan,btn_scan_loc;
    public String decifer_text = "demo";
    public static boolean checking=false;
    public static boolean checking_loc=false;
    private static List<VariableChangeListener> listeners = new ArrayList<VariableChangeListener>();
    private static List<VariableChangeListener> listeners_loc = new ArrayList<VariableChangeListener>();
    private FirebaseUser user;
    private String userID;
    private FirebaseFirestore mFStore;
    private String inOrOut = "", warehouseAOrB = "";
    private RadioGroup radioGroupIn;
    private RadioButton radio_in_out;
    private RadioButton radio_in,radio_out;
    private ProgressBar progressBar;

    public static boolean getMyBoolean(){return checking;};

    public static void setMyBoolean(boolean value){
        checking = value;
        for(VariableChangeListener l:listeners){
            l.onVariableChanged();
        }
    }

    public static void setMyBoolean_loc(boolean value){
        checking_loc = value;
        for(VariableChangeListener l:listeners_loc){
            l.onVariableChanged();
        }
    }

    public static void addMyBooleanListener_loc(VariableChangeListener l){
        listeners_loc.add(l);
    }

    public static void addMyBooleanListener(VariableChangeListener l){
        listeners.add(l);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_scanner);
        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();
        mFStore = FirebaseFirestore.getInstance();
        DocumentReference df = mFStore.collection("Users").document(userID);

        final TextInputEditText tv_code = findViewById(R.id.tv_code);
        final TextInputEditText tv_name = findViewById(R.id.tv_name);
        final TextInputEditText tv_loc = findViewById(R.id.tv_loc);
        final TextInputEditText edit_quantity = findViewById(R.id.edit_quantity);
        Button btn_submit = findViewById(R.id.btn_submit);
        btn_scan = findViewById(R.id.btn_scan);
        btn_scan_loc = findViewById(R.id.btn_scan_loc);
        progressBar = findViewById(R.id.progressBar_popUp);
        radioGroupIn = findViewById(R.id.radio_in_out);
        radio_in = findViewById(R.id.radio_in);
        radio_out = findViewById(R.id.radio_out);
        updateRadioGroup(radio_in);
        Bundle extras = getIntent().getExtras();
        warehouseAOrB = extras.getString("warehouse");

        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                tv_name.setText(documentSnapshot.getString("FullName"));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CodeScanner.this, "Can't get data", Toast.LENGTH_SHORT).show();
            }
        });
        btn_submit.setOnClickListener(v->{
            int radio_in_id = radioGroupIn.getCheckedRadioButtonId();
            radio_in_out = findViewById(radio_in_id);
            inOrOut = radio_in_out.getText().toString();
            if(tv_code.getText().toString().isEmpty()){
                Toast.makeText(this, "Please scan the product", Toast.LENGTH_SHORT).show();
                return;
            }
            if(edit_quantity.getText().toString().isEmpty()){
                Toast.makeText(this, "Please type in the quantity", Toast.LENGTH_SHORT).show();
                return;
            }
            if(tv_name.getText().toString().isEmpty() || tv_loc.getText().toString().isEmpty()){
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
                return;
            }
            btn_submit.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            Date currentTime = Calendar.getInstance().getTime();
            Map<String,Object> inOutInfo = new HashMap<>();
            inOutInfo.put("Name",tv_name.getText().toString());
            inOutInfo.put("Product Code",tv_code.getText().toString());
            inOutInfo.put("Quantity",edit_quantity.getText().toString());
            inOutInfo.put("From or to",tv_loc.getText().toString());
            inOutInfo.put("BelongsTo",warehouseAOrB);
            inOutInfo.put("Time of submit",currentTime.toString());
            String productID = tv_code.getText().toString();
            String path = productID + ".In Storage";
            DocumentReference dRef = mFStore.collection("In Storage").document(warehouseAOrB);
            dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful() && task.getResult().exists()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if(documentSnapshot.exists()){
                            String quantityStr = ((HashMap<String, Object>) documentSnapshot.getData().get(productID))
                                    .get("In Storage").toString();
                            String whichZone = ((HashMap<String, Object>) documentSnapshot.getData().get(productID))
                                    .get("In").toString();
                            int productQty = 0;
                            if(inOrOut.equals("In")){
                                productQty = Integer.parseInt(quantityStr) + Integer.parseInt(edit_quantity.getText().toString());
                                mFStore.collection(inOrOut).add(inOutInfo).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Log.d("Sendinginfo",inOrOut + " is updated");
                                        documentReference.update("Time of submit", FieldValue.serverTimestamp());
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("Sendinginfo", e.getMessage());
                                    }
                                });
                                Map<String,Object> zoneLeft = new HashMap<>();
                                zoneLeft.put("Barcode",productID);
                                zoneLeft.put("Quantity",edit_quantity.getText().toString());
                                zoneLeft.put("Created at","");
                                mFStore.collection(whichZone).add(zoneLeft).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Log.d("Sendinginfo",whichZone + " is updated");
                                        documentReference.update("Created at", FieldValue.serverTimestamp());
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("Sendinginfo", e.getMessage());
                                    }
                                });
                            }else{
                                productQty = Integer.parseInt(quantityStr) - Integer.parseInt(edit_quantity.getText().toString());
                                mFStore.collection(inOrOut).add(inOutInfo).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Log.d("Sendinginfo",inOrOut + " is updated");
                                        documentReference.update("Time of submit", FieldValue.serverTimestamp());
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("Sendinginfo", e.getMessage());
                                    }
                                });
                                final int[] tempPro = {Integer.parseInt(edit_quantity.getText().toString())};
                                mFStore.collection(whichZone)
                                        .orderBy("Created at", Query.Direction.ASCENDING)
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                                                List<String> oldProductId = new ArrayList<>();
                                                int accumulate = 0;
                                                for(DocumentSnapshot snapshot: snapshotList){
                                                    Log.d("Sendinginfo","ID being deleted "+snapshot.getId());
                                                    accumulate += Integer.parseInt(snapshot.get("Quantity").toString());
                                                    oldProductId.add(snapshot.getId());
                                                    if(accumulate>=tempPro[0]) break;
                                                }
                                                if(accumulate==tempPro[0]){
                                                    mFStore.collection(whichZone)
                                                            .whereIn(FieldPath.documentId(),oldProductId)
                                                            .get()
                                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onSuccess(QuerySnapshot querySnapshots) {
                                                                    WriteBatch batch = mFStore.batch();
                                                                    List<DocumentSnapshot> snapshotList1 = querySnapshots.getDocuments();
                                                                    for(DocumentSnapshot snapshot: snapshotList1){
                                                                        batch.delete(snapshot.getReference());
                                                                    }
                                                                    batch.commit()
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void unused) {
                                                                                    Log.d("Sendinginfo","Old ID deleted");
                                                                                    mFStore.collection(whichZone)
                                                                                            .orderBy("Created at", Query.Direction.ASCENDING)
                                                                                            .limit(1)
                                                                                            .get()
                                                                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                                                @Override
                                                                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                                                    List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                                                                                                    String idOfOld = "";
                                                                                                    for(DocumentSnapshot snapshot: snapshotList){
                                                                                                        idOfOld += snapshot.getId();
                                                                                                        Log.d("Sendinginfo","new oldest id is "+idOfOld);
                                                                                                    }
                                                                                                    mFStore.collection(whichZone)
                                                                                                            .document(idOfOld)
                                                                                                            .get()
                                                                                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                                                                @Override
                                                                                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                                                                    Timestamp timestamp = (Timestamp) documentSnapshot
                                                                                                                            .getData()
                                                                                                                            .get("Created at");
                                                                                                                    String pathToSince = productID + ".Since";
                                                                                                                    dRef.update(pathToSince,timestamp)
                                                                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                                @Override
                                                                                                                                public void onSuccess(Void unused) {
                                                                                                                                    Log.d("Sendinginfo", "Date in storage updated");
                                                                                                                                }
                                                                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                                                                @Override
                                                                                                                                public void onFailure(@NonNull Exception e) {
                                                                                                                                    Log.d("Sendinginfo", e.getMessage());
                                                                                                                                }
                                                                                                                            });
                                                                                                                }
                                                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                                                @Override
                                                                                                                public void onFailure(@NonNull Exception e) {
                                                                                                                    Log.d("Sendinginfo", e.getMessage());
                                                                                                                }
                                                                                                            });
                                                                                                }
                                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                                @Override
                                                                                                public void onFailure(@NonNull Exception e) {
                                                                                                    Log.d("Sendinginfo", e.getMessage());
                                                                                                }
                                                                                            });
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Log.d("Sendinginfo", e.getMessage());
                                                                                }
                                                                            });
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.d("Sendinginfo", e.getMessage());
                                                                }
                                                            });

                                                }else{
                                                    String tempId = oldProductId.get(oldProductId.size()-1);
                                                    oldProductId.remove(tempId);
                                                    if(oldProductId.size()>0){
                                                        mFStore.collection(whichZone)
                                                                .whereIn(FieldPath.documentId(),oldProductId)
                                                                .get()
                                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(QuerySnapshot querySnapshots) {
                                                                        WriteBatch batch = mFStore.batch();
                                                                        List<DocumentSnapshot> snapshotList1 = querySnapshots.getDocuments();
                                                                        for(DocumentSnapshot snapshot: snapshotList1){
                                                                            batch.delete(snapshot.getReference());
                                                                        }
                                                                        batch.commit()
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void unused) {
                                                                                        Log.d("Sendinginfo","Old ID deleted");
                                                                                    }
                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        Log.d("Sendinginfo", e.getMessage());
                                                                                    }
                                                                                });
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.d("Sendinginfo", e.getMessage());
                                                                    }
                                                                });
                                                    }
                                                    int remainingPro = Math.abs(tempPro[0]-accumulate);
                                                    mFStore.collection(whichZone)
                                                            .document(tempId)
                                                            .update("Quantity",String.valueOf(remainingPro))
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Log.d("Sendinginfo",tempId+"/Quantity is updated");
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.d("Sendinginfo", e.getMessage());
                                                                }
                                                            });
                                                    mFStore.collection(whichZone).document(tempId).get()
                                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                    Timestamp timestamp = (Timestamp) documentSnapshot
                                                                            .getData().get("Created at");
                                                                    String pathToSince = productID + ".Since";
                                                                    dRef.update(pathToSince,timestamp)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void unused) {
                                                                                    Log.d("Sendinginfo", "Date in storage updated");
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Log.d("Sendinginfo", e.getMessage());
                                                                                }
                                                                            });
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.d("Sendinginfo", e.getMessage());
                                                                }
                                                            });
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("Sendinginfo", e.getMessage());
                                            }
                                        });

                            }
                            dRef.update(path,productQty).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d("Sendinginfo",warehouseAOrB + "/" + productID + " is updated");
                                    btn_submit.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(CodeScanner.this, "Information sent successfully", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CodeScanner.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else {
                            Log.d("Sendinginfo","No such document in In Storage/" + warehouseAOrB);
                        }
                    }else{
                        Log.d("Sendinginfo","get failed with ", task.getException());
                    }
                }
            });
        });
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanCode();
            }
        });
        btn_scan_loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanLoc();
            }
        });
        addMyBooleanListener(new VariableChangeListener() {
            @Override
            public void onVariableChanged() {
                tv_code.setText(decifer_text);
            }
        });
        addMyBooleanListener_loc(new VariableChangeListener() {
            @Override
            public void onVariableChanged() {
                tv_loc.setText(decifer_text);
            }
        });


    }

    private void scanLoc() {
        ScanOptions options = new ScanOptions();
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher_loc.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher_loc = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents() != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CodeScanner.this);
            builder.setTitle("Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    decifer_text = result.getContents().toString();
                    setMyBoolean_loc(true);
                    dialogInterface.dismiss();
                }
            }).show();
        }
    });

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents() != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CodeScanner.this);
            builder.setTitle("Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    decifer_text = result.getContents().toString();
                    setMyBoolean(true);
                    dialogInterface.dismiss();
                }
            }).show();
        }
    });

    public void backArrowFunc(View view) {
        Intent intent = new Intent(CodeScanner.this,MainScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void radioTapped(View view) {
        int selectedID = view.getId();
        if(selectedID == R.id.radio_in){
            updateRadioGroup(radio_in);
        }else updateRadioGroup(radio_out);
    }

    public void updateRadioGroup(RadioButton selected){
        radio_in.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.radio_off));
        radio_out.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.radio_off));
        selected.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.radio_on));
    }
}