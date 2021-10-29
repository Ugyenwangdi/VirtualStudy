package com.boilerplate.firestore.virtualstudy;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.dmoral.toasty.Toasty;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText reg_email_field;
    private EditText reg_pass_field;
    private EditText reg_confrim_pass_field;
    private Button reg_btn;
    private Button reg_login_btn;
    private ProgressBar reg_progress;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        reg_email_field = findViewById(R.id.reg_email);
        reg_pass_field = findViewById(R.id.reg_pass);
        reg_confrim_pass_field = findViewById(R.id.reg_confrim_pass);
        reg_btn = findViewById(R.id.reg_btn);
        reg_login_btn = findViewById(R.id.reg_login_btn);
        reg_progress = findViewById(R.id.reg_progress);

        reg_btn.setEnabled(true);

        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String email = reg_email_field.getText().toString();
                final String password = reg_pass_field.getText().toString();
                final String confrim_password = reg_confrim_pass_field.getText().toString();

                if(!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password)&&!TextUtils.isEmpty(confrim_password)) {

                    if(password.equals(confrim_password)) {

                        reg_progress.setVisibility(View.VISIBLE);

                        reg_btn.setEnabled(false);

                        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()) {

                                    SendEmailVerificationMessage();



                                } else {

//                                    if(TextUtils.isEmpty(email)){
//                                        reg_email_field.setError("Email is Required.");
//                                        return;
//                                    }
//
//                                    if(TextUtils.isEmpty(password)){
//                                        reg_pass_field.setError("Password is Required.");
//                                        return;
//                                    }
//
//                                    if (password.length() < 6){
//                                        reg_pass_field.setError("Password Must be >=6 Characters");
//                                        return;
//                                    }
//
//                                    if (TextUtils.isEmpty(confrim_password)){
//                                        reg_confrim_pass_field.setError("Enter your confirmation password");
//                                        return;
//                                    }

                                    String errorMessage = task.getException().getMessage();

                                    Toasty.error(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG, true).show();

                                }

                                reg_progress.setVisibility(View.INVISIBLE);

                            }
                        });

                    }
                    else {

                        Toasty.warning(RegisterActivity.this, "Passwords Don't match", Toast.LENGTH_SHORT, true).show();


                    }

                }

            }
        });

        reg_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendToLogin();

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {

            sendToMain();

        }

    }

    private void sendToMain() {

        Intent intent = new Intent(RegisterActivity.this,loginActivity.class);
        startActivity(intent);
        finish();

    }

    private void sendToLogin() {

        Intent intent = new Intent(RegisterActivity.this,loginActivity.class);
        startActivity(intent);
        finish();

    }

    private void SendEmailVerificationMessage(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toasty.success(RegisterActivity.this, "Successfully Registered User, Please Verify!", Toast.LENGTH_LONG, true).show();

//                        Intent setupIntent = new Intent(RegisterActivity.this, loginActivity.class);
//                        setupIntent.putExtra("Display_arrow","no");
//                        startActivity(setupIntent);
//                        finish();
                        sendToLogin();

                        mAuth.signOut();
                    }else {

                        String errorMessage = task.getException().getMessage();

                        Toasty.error(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG, true).show();

                        mAuth.signOut();
                    }
                }
            });
        }
    }
}
