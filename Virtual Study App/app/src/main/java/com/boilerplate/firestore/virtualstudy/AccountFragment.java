package com.boilerplate.firestore.virtualstudy;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//import com.bumptech.glide.Glide;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

//import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {


    private TextView nameText, usernameText, courseText, yearText;

    private ImageView imageView;


    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        FirebaseAuth user = FirebaseAuth.getInstance();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        // getUid gives an error, for null user


        nameText = view.findViewById(R.id.ProfileName);
        usernameText = view.findViewById(R.id.ProfileUserName);
        courseText = view.findViewById(R.id.ProfileCourse);
        yearText = view.findViewById(R.id.ProfileYear);
        imageView = view.findViewById(R.id.profileImg);


        if (user.getCurrentUser() != null) {

            String user_id = user.getCurrentUser().getUid();

            firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            String name = task.getResult().getString("name");
                            String username = task.getResult().getString("username");
                            String course = task.getResult().getString("course");
                            String year = task.getResult().getString("year");
                            String img_uri = task.getResult().getString("image");

                            nameText.setText(name);
                            usernameText.setText(username);
                            courseText.setText(course);
                            yearText.setText(year);

                        Glide.with(getActivity().getApplicationContext())
                                .load(img_uri)
                                .into(imageView);

                        } else {
                            Intent intent = new Intent(getActivity(), SetupActivity.class);
                            startActivity(intent);
                        }
                    } else {

                        Toasty.error(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG, true).show();

                    }


                }
            });

        }

        return view;

    }

}
