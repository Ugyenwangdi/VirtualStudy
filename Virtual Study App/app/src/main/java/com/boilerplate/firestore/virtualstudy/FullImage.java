package com.boilerplate.firestore.virtualstudy;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class FullImage extends AppCompatActivity{

    private ImageView fullImageView;
    //TextView title, description;


    //int image;

    private DocumentReference ref;
    private DocumentSnapshot lastVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        fullImageView = findViewById(R.id.fullImageView);

        Intent intent = getIntent();
        String image = intent.getStringExtra("image");

        Glide.with(this)
                .load(image)
                .into(fullImageView);

    }
}