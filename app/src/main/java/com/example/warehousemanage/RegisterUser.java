package com.example.warehousemanage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.MultiFactorAssertion;
import com.google.firebase.auth.MultiFactorSession;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.PhoneMultiFactorGenerator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RegisterUser extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private TextView banner;
    private Button registerUser;
    private TextInputEditText edit_full_name, edit_age, edit_email, edit_pass,edit_phone;
    private ProgressBar progressBar;
    private Spinner spinner;
    private String usersOfFactory;


    private FirebaseAuth mAuth;
    private FirebaseFirestore mFStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        mAuth = FirebaseAuth.getInstance();
        mFStore = FirebaseFirestore.getInstance();
        banner = (TextView) findViewById(R.id.banner_register);
        banner.setOnClickListener(this);
        registerUser = (Button) findViewById(R.id.btn_register);
        registerUser.setOnClickListener(this);
        edit_full_name = findViewById(R.id.edit_full_name);
        edit_age = findViewById(R.id.edit_age);
        edit_email = findViewById(R.id.edit_email);
        edit_pass = findViewById(R.id.edit_pass);
        progressBar = (ProgressBar) findViewById(R.id.progressBar_register);
        spinner = (Spinner) findViewById(R.id.spinner);
        edit_phone = findViewById(R.id.edit_phone);
//        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
//                R.array.Factory, android.R.layout.simple_spinner_item);
//        spinnerAdapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
//        spinner.setAdapter(spinnerAdapter);
        List<String> wh = new ArrayList<>();
        wh.add("Warehouse A");
        wh.add("Warehouse B");
        SpinnerAdapter customAdapter = new SpinnerAdapter(this,R.layout.spinner_layout_test,wh);
        customAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(customAdapter);
        spinner.setOnItemSelectedListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.banner_register:
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                break;
            case R.id.btn_register:
                register_user();
                break;
        }
    }

    private void register_user() {
        String email = edit_email.getText().toString().trim();
        String password = edit_pass.getText().toString().trim();
        String age = edit_age.getText().toString().trim();
        String fullName = edit_full_name.getText().toString().trim();
        String phone = edit_phone.getText().toString().trim();
        if(email.isEmpty()||password.isEmpty()||age.isEmpty()||fullName.isEmpty()||phone.isEmpty()){
            Toast.makeText(this, "Please fill out all the above fields", Toast.LENGTH_LONG).show();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            edit_email.setError("Please provide valid email");
            edit_email.requestFocus();
            return;
        }
        String phoneNums = "";
        if(phone.startsWith("0")) phoneNums = "+84" + "" + phone.substring(1);
        else phoneNums = phone;
        Log.d("Sendinginfo",phoneNums);
        progressBar.setVisibility(View.VISIBLE);
        String finalPhoneNums = phoneNums;
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();

                    DocumentReference df = mFStore.collection("Users").document(user.getUid());
                    Map<String,Object> userInfo = new HashMap<>();
                    userInfo.put("FullName",fullName);
                    userInfo.put("Age",age);
                    userInfo.put("Email",email);
                    userInfo.put("IsUserOf",usersOfFactory);
                    userInfo.put("Phone", finalPhoneNums);
                    df.set(userInfo);

                    if(user.isEmailVerified()){
                        startActivity(new Intent(RegisterUser.this,MainActivity.class));
                        finish();
                    }else{
                        user.sendEmailVerification();
                        Toast.makeText(RegisterUser.this, "Check your email to verify your account", Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                }else{
                    Toast.makeText(RegisterUser.this, "Failed to register!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        usersOfFactory = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void backArrowFunc(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}