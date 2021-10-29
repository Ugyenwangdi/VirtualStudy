package com.boilerplate.firestore.virtualstudy;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import es.dmoral.toasty.Toasty;

public class passwordResestActivity extends AppCompatActivity {

    private EditText forgot_pass_email;
    private Button forgot_pass_btn;
    private Toolbar forgot_pass_toolbar;
    private ProgressBar forgot_pass_progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_resest);

        mAuth = FirebaseAuth.getInstance();

        forgot_pass_toolbar = findViewById(R.id.forgot_pass_toolbar);
        forgot_pass_email = findViewById(R.id.forgot_pass_email);
        forgot_pass_btn = findViewById(R.id.forgot_pass_btn);
        forgot_pass_progressBar = findViewById(R.id.forgot_pass_progress);

        setSupportActionBar(forgot_pass_toolbar);
        getSupportActionBar().setTitle("Forgot Password ?");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        forgot_pass_progressBar.setVisibility(View.INVISIBLE);

        forgot_pass_btn.setEnabled(true);

        forgot_pass_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = forgot_pass_email.getText().toString();

                forgot_pass_progressBar.setVisibility(View.VISIBLE);

                if (!TextUtils.isEmpty(email)) {

                    forgot_pass_btn.setEnabled(false);

                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                               /* Toast.makeText(passwordResestActivity.this, "Email Sent Successfully", Toast.LENGTH_SHORT).show();*/

                                Toasty.success(passwordResestActivity.this, "Email Sent Successfully", Toast.LENGTH_SHORT, true).show();

                                finish();

                            } else {

                                forgot_pass_btn.setEnabled(true);

                               /* Toast.makeText(passwordResestActivity.this, "Error : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();*/

                                Toasty.error(passwordResestActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG, true).show();

                            }

                            forgot_pass_progressBar.setVisibility(View.INVISIBLE);

                        }
                    });

                } else {

                    forgot_pass_btn.setEnabled(true);

                    forgot_pass_progressBar.setVisibility(View.INVISIBLE);

                   /* Toast.makeText(passwordResestActivity.this, "Please Enter the Registered Email Address", Toast.LENGTH_LONG).show();*/

                    Toasty.warning(passwordResestActivity.this, "Please Enter the Registered Email Address", Toast.LENGTH_LONG, true).show();

                }

            }
        });
    }

}
