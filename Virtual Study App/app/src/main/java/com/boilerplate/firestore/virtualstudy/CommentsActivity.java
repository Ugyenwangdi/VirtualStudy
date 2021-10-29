package com.boilerplate.firestore.virtualstudy;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class CommentsActivity extends AppCompatActivity {

    private Toolbar commentToolbar;

    private EditText comment_field;
    private ImageView comment_post_btn;
    private ImageView comment_current_user;

    private String blog_post_id;
    private String current_user_id;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private RecyclerView comment_list;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private List<Comments> commentsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        commentToolbar = findViewById(R.id.comment_toolbar);
        setSupportActionBar(commentToolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        current_user_id = firebaseAuth.getCurrentUser().getUid();

        blog_post_id = getIntent().getStringExtra("blog_post_id");

        comment_field = findViewById(R.id.comment_field);
        comment_post_btn = findViewById(R.id.comment_post_btn);
        comment_list = findViewById(R.id.comment_list);
        comment_current_user = findViewById(R.id.comment_current_user);

        commentsList = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentsList);
        comment_list.setHasFixedSize(true);
        comment_list.setLayoutManager(new LinearLayoutManager(this));
        comment_list.setAdapter(commentsRecyclerAdapter);

        firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments")
                .addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (documentSnapshots != null) {

                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String commentId = doc.getDocument().getId();
                                    Comments comments = doc.getDocument().toObject(Comments.class);
                                    commentsList.add(comments);
                                    commentsRecyclerAdapter.notifyDataSetChanged();


                                }

                            }

                        }

                    }
                });

        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).get()
                .addOnCompleteListener(CommentsActivity.this,new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.isSuccessful()) {

                            System.out.println(task.getResult().getString("image"));
                            Glide.with(CommentsActivity.this)
                                    .load(task.getResult().getString("image"))
                                    .into(comment_current_user);

                        } else {

                            Toasty.error(CommentsActivity.this, "ERROR : " + task.getException().getMessage(), Toast.LENGTH_LONG, true).show();

                        }

                    }
                });

        comment_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String comment_text = comment_field.getText().toString();

                if (!TextUtils.isEmpty(comment_text)) {
                    Map<String, Object> commentsMap = new HashMap<>();
                    commentsMap.put("message", comment_text);
                    commentsMap.put("user_id", current_user_id);
                    commentsMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments").add(commentsMap)
                            .addOnCompleteListener(CommentsActivity.this,new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {

                            if (task.isSuccessful()) {

                                comment_field.setText("");

                                Toasty.success(CommentsActivity.this, "Comment Successfully posted ", Toast.LENGTH_LONG, true).show();

                            } else {

                                Toasty.error(CommentsActivity.this, "Error Posting Comment : " + task.getException().getMessage(), Toast.LENGTH_LONG, true).show();

                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toasty.error(CommentsActivity.this, "Error Posting Comment : " + e.getMessage(), Toast.LENGTH_LONG, true).show();


                        }
                    });

                } else {

                    Toasty.warning(CommentsActivity.this, "Please Enter a comment", Toast.LENGTH_LONG, true).show();


                }

            }

        });
    }
}
