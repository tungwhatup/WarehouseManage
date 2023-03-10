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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthMultiFactorException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.MultiFactorAssertion;
import com.google.firebase.auth.MultiFactorInfo;
import com.google.firebase.auth.MultiFactorResolver;
import com.google.firebase.auth.MultiFactorSession;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.PhoneMultiFactorGenerator;
import com.google.firebase.auth.PhoneMultiFactorInfo;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView register, forgotPass,resendOtp;
    private Button login,verifyOtp;
    private TextInputEditText editTextEmail, editTextPass;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private EditText inputCode1,inputCode2,inputCode3,inputCode4,inputCode5,inputCode6;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String otpStr;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar,progressBar_popUp;
    public static boolean first_time = false;
    private static List<VariableChangeListener> listeners = new ArrayList<VariableChangeListener>();

    public static void setMyBoolean(boolean value){
        first_time = value;
        for(VariableChangeListener l : listeners){
            l.onVariableChanged();
        }
    }

    public static void addMyBooleanListener(VariableChangeListener l){listeners.add(l);}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        register = (TextView) findViewById(R.id.tv_register);
        register.setOnClickListener(this);
        login = (Button) findViewById(R.id.btn_login);
        login.setOnClickListener(this);
        forgotPass = (TextView) findViewById(R.id.tv_forgot_pass);
        forgotPass.setOnClickListener(this);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        editTextEmail = findViewById(R.id.edit_email_login);
        editTextPass = findViewById(R.id.edit_pass_login);
        mAuth = FirebaseAuth.getInstance();
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.d("Sendinginfo",e.getMessage());
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Toast.makeText(MainActivity.this, "Code has been sent", Toast.LENGTH_SHORT).show();
                otpStr = s;
            }
        };
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_register:
                startActivity(new Intent(this, RegisterUser.class));
                break;
            case R.id.btn_login:
                userLogin();
                break;
            case R.id.tv_forgot_pass:
                startActivity(new Intent(this, ForgotPassword.class));
                break;
        }
    }

    private void userLogin() {
        String email = editTextEmail.getText().toString().trim();
        String pass = editTextPass.getText().toString().trim();
        final String[] phone = new String[1];

        if(email.isEmpty()){
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            return;
        }
        if(pass.isEmpty()){
            editTextPass.setError("Password is required");
            editTextPass.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
//                    Intent intent = new Intent(MainActivity.this,MainScreen.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
                    user = mAuth.getCurrentUser();
                    FirebaseFirestore.getInstance()
                            .collection("Users")
                            .document(user.getUid())
                            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    phone[0] = documentSnapshot.getString("Phone");
                                    user.getMultiFactor().getSession().addOnCompleteListener(new OnCompleteListener<MultiFactorSession>() {
                                        @Override
                                        public void onComplete(@NonNull Task<MultiFactorSession> task) {
                                            if (task.isSuccessful()){
                                                MultiFactorSession multiFactorSession = task.getResult();
                                                PhoneAuthOptions phoneAuthOptions =
                                                        PhoneAuthOptions.newBuilder()
                                                                .setPhoneNumber(phone[0])
                                                                .setTimeout(30L, TimeUnit.SECONDS)
                                                                .setMultiFactorSession(multiFactorSession)
                                                                .setActivity(MainActivity.this)
                                                                .setCallbacks(callbacks)
                                                                .requireSmsValidation(true)
                                                                .build();
                                                PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
                                                createNewDialogEnroll(user,phone[0],multiFactorSession);
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("Sendinginfo",e.getMessage());
                                }
                            });

                }else if(task.getException() instanceof FirebaseAuthMultiFactorException){
                    FirebaseAuthMultiFactorException e = (FirebaseAuthMultiFactorException) task.getException();
                    MultiFactorResolver multiFactorResolver = e.getResolver();
                    MultiFactorInfo selectedHint = multiFactorResolver.getHints().get(0);
                    PhoneAuthProvider.verifyPhoneNumber(
                            PhoneAuthOptions.newBuilder()
                                    .setActivity(MainActivity.this)
                                    .setMultiFactorSession(multiFactorResolver.getSession())
                                    .setMultiFactorHint((PhoneMultiFactorInfo) selectedHint)
                                    .setCallbacks(callbacks)
                                    .setTimeout(30L,TimeUnit.SECONDS)
                                    .requireSmsValidation(true)
                                    .build()
                    );
                    createNewDialogLogin(multiFactorResolver,selectedHint);
                }
                else{

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Sendinginfo",e.getMessage());

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null){
            FirebaseAuth.getInstance().signOut();
        }
    }

    public void createNewDialogEnroll(FirebaseUser u,String str,MultiFactorSession session){
        builder = new AlertDialog.Builder(this);
        final View otpPopupView = getLayoutInflater().inflate(R.layout.popup_otp,null);
        inputCode1 = (EditText) otpPopupView.findViewById(R.id.inputCode1);
        inputCode2 = (EditText) otpPopupView.findViewById(R.id.inputCode2);
        inputCode3 = (EditText) otpPopupView.findViewById(R.id.inputCode3);
        inputCode4 = (EditText) otpPopupView.findViewById(R.id.inputCode4);
        inputCode5 = (EditText) otpPopupView.findViewById(R.id.inputCode5);
        inputCode6 = (EditText) otpPopupView.findViewById(R.id.inputCode6);
        inputCode1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){
                    inputCode2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputCode2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){
                    inputCode3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputCode3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){
                    inputCode4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputCode4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){
                    inputCode5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputCode5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){
                    inputCode6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        verifyOtp = (Button) otpPopupView.findViewById(R.id.btnVerify);
        resendOtp = (TextView) otpPopupView.findViewById(R.id.textResendOTP);
        progressBar_popUp = otpPopupView.findViewById(R.id.progressBar_popUp);
        builder.setView(otpPopupView);
        dialog = builder.create();
        dialog.show();
        verifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputCode1.getText().toString().trim().isEmpty()
                        ||inputCode2.getText().toString().trim().isEmpty()
                        ||inputCode3.getText().toString().trim().isEmpty()
                        ||inputCode4.getText().toString().trim().isEmpty()
                        ||inputCode5.getText().toString().trim().isEmpty()
                        ||inputCode6.getText().toString().trim().isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter a valid code", Toast.LENGTH_SHORT).show();
                    return;
                }
                String code = inputCode1.getText().toString() + inputCode2.getText().toString()
                        + inputCode3.getText().toString() + inputCode4.getText().toString()
                        + inputCode5.getText().toString() + inputCode6.getText().toString();
                if(otpStr != null){
                    progressBar_popUp.setVisibility(View.VISIBLE);
                    verifyOtp.setVisibility(View.INVISIBLE);
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(otpStr,code);
                    MultiFactorAssertion multiFactorAssertion = PhoneMultiFactorGenerator.getAssertion(credential);
                    u.getMultiFactor()
                            .enroll(multiFactorAssertion,"My personal phone number")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressBar_popUp.setVisibility(View.GONE);
                                    verifyOtp.setVisibility(View.VISIBLE);
                                    dialog.dismiss();
                                    Toast.makeText(MainActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    Intent intent = new Intent(MainActivity.this,MainScreen.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("Sendinginfo","Unable to enroll due to /n"+e.getMessage());
                                }
                            });
                }
            }

        });
        resendOtp = otpPopupView.findViewById(R.id.textResendOTP);
        resendOtp.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            PhoneAuthOptions phoneAuthOptions =
                    PhoneAuthOptions.newBuilder()
                            .setPhoneNumber(str)
                            .setTimeout(30L, TimeUnit.SECONDS)
                            .setMultiFactorSession(session)
                            .setActivity(MainActivity.this)
                            .setCallbacks(callbacks)
                            .requireSmsValidation(true)
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
        }
    });
    }

    public void createNewDialogLogin(MultiFactorResolver resolver,MultiFactorInfo hint){
        builder = new AlertDialog.Builder(this);
        final View otpPopupView = getLayoutInflater().inflate(R.layout.popup_otp,null);
        inputCode1 = (EditText) otpPopupView.findViewById(R.id.inputCode1);
        inputCode2 = (EditText) otpPopupView.findViewById(R.id.inputCode2);
        inputCode3 = (EditText) otpPopupView.findViewById(R.id.inputCode3);
        inputCode4 = (EditText) otpPopupView.findViewById(R.id.inputCode4);
        inputCode5 = (EditText) otpPopupView.findViewById(R.id.inputCode5);
        inputCode6 = (EditText) otpPopupView.findViewById(R.id.inputCode6);
        inputCode1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){
                    inputCode2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputCode2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){
                    inputCode3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputCode3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){
                    inputCode4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputCode4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){
                    inputCode5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputCode5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){
                    inputCode6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        verifyOtp = (Button) otpPopupView.findViewById(R.id.btnVerify);
        resendOtp = (TextView) otpPopupView.findViewById(R.id.textResendOTP);
        progressBar_popUp = otpPopupView.findViewById(R.id.progressBar_popUp);
        builder.setView(otpPopupView);
        dialog = builder.create();
        dialog.show();
        verifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputCode1.getText().toString().trim().isEmpty()
                        ||inputCode2.getText().toString().trim().isEmpty()
                        ||inputCode3.getText().toString().trim().isEmpty()
                        ||inputCode4.getText().toString().trim().isEmpty()
                        ||inputCode5.getText().toString().trim().isEmpty()
                        ||inputCode6.getText().toString().trim().isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter a valid code", Toast.LENGTH_SHORT).show();
                    return;
                }
                String code = inputCode1.getText().toString() + inputCode2.getText().toString()
                        + inputCode3.getText().toString() + inputCode4.getText().toString()
                        + inputCode5.getText().toString() + inputCode6.getText().toString();
                if(otpStr != null){
                    progressBar_popUp.setVisibility(View.VISIBLE);
                    verifyOtp.setVisibility(View.INVISIBLE);
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(otpStr,code);
                    MultiFactorAssertion multiFactorAssertion = PhoneMultiFactorGenerator.getAssertion(credential);
                    resolver.resolveSignIn(multiFactorAssertion).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                progressBar_popUp.setVisibility(View.GONE);
                                verifyOtp.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                Intent intent = new Intent(MainActivity.this,MainScreen.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Verification failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        resendOtp = otpPopupView.findViewById(R.id.textResendOTP);
        resendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhoneAuthProvider.verifyPhoneNumber(
                        PhoneAuthOptions.newBuilder()
                                .setActivity(MainActivity.this)
                                .setMultiFactorSession(resolver.getSession())
                                .setMultiFactorHint((PhoneMultiFactorInfo) hint)
                                .setCallbacks(callbacks)
                                .setTimeout(30L,TimeUnit.SECONDS)
                                .requireSmsValidation(true)
                                .build()
                );
            }
        });
    }
}