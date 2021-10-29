package com.boilerplate.firestore.virtualstudy;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class Feedback extends AppCompatActivity {

    private Button FeedbackBtn;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private Toolbar feedback_toolbar;

    String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        feedback_toolbar = findViewById(R.id.feedback_toolbar);
        setSupportActionBar(feedback_toolbar);
        getSupportActionBar().setTitle("About App");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        current_user_id = firebaseAuth.getCurrentUser().getUid();

        FeedbackBtn = findViewById(R.id.FeedbackBtn);

        FeedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    private void showDialog(){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Feedback Form");
        dialog.setMessage("Provide us your Valueable Feedback ");

        LayoutInflater inflater = LayoutInflater.from(this);

        View reg_layout = inflater.inflate(R.layout.send_feedback,null);

        final MaterialEditText editEmail = reg_layout.findViewById(R.id.editEmail);
        final MaterialEditText editName = reg_layout.findViewById(R.id.editName);
        final MaterialEditText editFeedback = reg_layout.findViewById(R.id.editFeedback);

        dialog.setView(reg_layout);

        dialog.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (TextUtils.isEmpty(editEmail.getText().toString())){
                    Toasty.warning(Feedback.this, "Please Enter Your Email..", Toast.LENGTH_LONG, true).show();
                    return;
                }
                if (TextUtils.isEmpty(editName.getText().toString())){
                    Toasty.warning(Feedback.this, "Name Field Cannot Be Empty..", Toast.LENGTH_LONG, true).show();
                    return;
                }
                if (TextUtils.isEmpty(editFeedback.getText().toString())){
                    Toasty.warning(Feedback.this, "Feedback Field Cannot Be Empty..", Toast.LENGTH_LONG, true).show();
                    return;
                }
                if (firebaseFirestore.collection("Users").document(current_user_id) != null){

                    firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                if (task.getResult().exists()){
                                    String email = editEmail.getText().toString();
                                    String name = editName.getText().toString();
                                    String feedback = editFeedback.getText().toString();

                                    Map<String, Object> feedbackMap = new HashMap<>();
                                    feedbackMap.put("email", email);
                                    feedbackMap.put("name", name);
                                    feedbackMap.put("feedback", feedback);

                                    CollectionReference ref = FirebaseFirestore.getInstance().collection("Feedback");
                                    ref.document(current_user_id).set(feedbackMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toasty.success(Feedback.this, "Thanks For Your Feedback.", Toast.LENGTH_LONG, true).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toasty.error(Feedback.this, "Something Went Wrong!", Toast.LENGTH_LONG, true).show();

                                        }
                                    });


                                }
                            }
                        }
                    });




                }

//                FirebaseDatabase database = FirebaseDatabase.getInstance();
//
//                DatabaseReference myRef = database.getReference().child("Users");
//
//                myRef.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        Object value = dataSnapshot.getValue();
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        Toasty.error(Feedback.this, "Failed To Read Value", Toast.LENGTH_LONG, true).show();
//
//                    }
//                });
//                myRef.child(editName.getText().toString()).child("Email").setValue(editEmail.getText().toString());
//                myRef.child(editName.getText().toString()).child("Feedback").setValue(editFeedback.getText().toString());
//                myRef.child(editName.getText().toString()).child("Name").setValue(editName.getText().toString());

 //               Toasty.success(Feedback.this, "Thanks For Your Feedback.", Toast.LENGTH_LONG, true).show();


            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                   dialogInterface.dismiss();
            }
        });

        dialog.show();
    }
}