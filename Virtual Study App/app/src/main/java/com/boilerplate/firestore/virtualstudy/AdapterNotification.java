package com.boilerplate.firestore.virtualstudy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import es.dmoral.toasty.Toasty;

public class AdapterNotification extends  RecyclerView.Adapter<AdapterNotification.HolderNotification> {

    private NotificationFragment activity;

    private List<ModelNotification> notificationsList;
    private NotificationFragment notificationFragment;

    private FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    // Convert timestamp to dd/mm/yy h:mm am/pm
    private @ServerTimestamp
    String timestamp;

    String currentUserID;

    public AdapterNotification(List<ModelNotification> notificationsList, NotificationFragment activity) {
        this.activity = activity;
        this.notificationsList = notificationsList;

    }

    @NonNull
    @Override
    public HolderNotification onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_notification, parent, false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();

        notificationFragment = new NotificationFragment();

        return new HolderNotification(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final HolderNotification holder, final int position) {

        //get data

        final ModelNotification model = notificationsList.get(position);
        holder.nameTv.setText(model.getHisName());
        holder.notificationTv.setText(model.getNotification()+ " posted on");
        holder.timeTv.setText(model.getTimestamp());
        Glide.with(activity.getContext())
                .load(model.getHisImage())
                .into(holder.avatarIv);


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(activity.getContext(), MainActivity.class);
                intent.putExtra("postId", model.getBlog_post_id());//post id
                activity.getContext().startActivity(intent);

            }
        });

        /// LongClick Listener to delete Notification

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity.getContext());
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this notification");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        CollectionReference ref = FirebaseFirestore.getInstance().collection("Users");

                        if (currentUserID != null) {

                            ref.document(currentUserID).collection("Likes").document(model.getHisUid()).delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            // deleted
                                            Toasty.success(activity.getContext(), "Notification deleted", Toast.LENGTH_SHORT).show();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            // failed
                                            Toast.makeText(activity.getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                        }
                                    });


                        }

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    // holder class for views
    public class HolderNotification extends RecyclerView.ViewHolder {

        private View mView;

        ImageView avatarIv;
        TextView nameTv, notificationTv, timeTv;


        public HolderNotification(View itemView) {
            super(itemView);

            mView = itemView;

            avatarIv = itemView.findViewById(R.id.avaterIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            notificationTv = itemView.findViewById(R.id.notificationTV);
            timeTv = itemView.findViewById(R.id.timeTV);

        }
    }

}


