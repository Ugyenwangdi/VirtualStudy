package com.boilerplate.firestore.virtualstudy;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.dmoral.toasty.Toasty;

public class loginActivity extends AppCompatActivity {

    private TextInputEditText loginEmailText;
    private TextInputEditText loginPassText;
    private Button loginBtn;
    private Button loginRegBtn;
    private ProgressBar loginProgress;
    private TextView forgot_pass_label;

    private FirebaseAuth mAuth;
    private Boolean emailAddressChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        loginEmailText = findViewById(R.id.reg_email);
        loginPassText = findViewById(R.id.reg_confrim_pass);
        loginBtn = findViewById(R.id.login_button);
        loginRegBtn = findViewById(R.id.login_reg_button);
        loginProgress = findViewById(R.id.login_progress);
        forgot_pass_label = findViewById(R.id.forgot_pass_label);

        forgot_pass_label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent password_reset_intent = new Intent(loginActivity.this,passwordResestActivity.class);
                startActivity(password_reset_intent);

            }
        });

        loginBtn.setEnabled(true);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String loginEmail = loginEmailText.getText().toString();
                String loginPass = loginPassText.getText().toString();

                if(!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPass)) {

                    loginProgress.setVisibility(View.VISIBLE);

                    loginBtn.setEnabled(false);

                    mAuth.signInWithEmailAndPassword(loginEmail,loginPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()) {

                                VerifyEmailAddress();
                               // sendToMain();

                            } else {

                                loginBtn.setEnabled(true);

                                String errorMessage = task.getException().getMessage();
                                /*Toast.makeText(loginActivity.this, "ERROR : "+errorMessage, Toast.LENGTH_LONG).show();*/

                                Toasty.error(loginActivity.this, errorMessage, Toast.LENGTH_LONG, true).show();

                            }

                            loginProgress.setVisibility(View.INVISIBLE);

                        }
                    });

                } else {

                    Toasty.warning(loginActivity.this, "Enter login Credentials", Toast.LENGTH_SHORT, true).show();

                }

            }
        });

        loginRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(loginActivity.this,RegisterActivity.class);
                startActivity(intent);
                finish();

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

        Intent mainIntent = new Intent(loginActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();

    }

    private void VerifyEmailAddress(){
        FirebaseUser user = mAuth.getCurrentUser();
        emailAddressChecker = user.isEmailVerified();
        if (emailAddressChecker){
            sendToMain();
        }else {
            Toasty.warning(loginActivity.this, "Please Verify Your Account First", Toast.LENGTH_SHORT, true).show();
            mAuth.signOut();
        }
    }
}
