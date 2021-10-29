package com.boilerplate.firestore.virtualstudy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import es.dmoral.toasty.Toasty;

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {

    public List<Comments> commentsList;
    public Context context;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    public CommentsRecyclerAdapter(List<Comments> commentsList){

        this.commentsList = commentsList;

    }

    @Override
    public CommentsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
        context = parent.getContext();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        return new CommentsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CommentsRecyclerAdapter.ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        if (firebaseAuth != null) {

            firebaseFirestore.collection("Users").document(commentsList.get(position).getUser_id()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if(task.isSuccessful()) {

                        holder.setUserDescription(task.getResult().getString("name"),task.getResult().getString("image"));

                    } else {

                        Toasty.error(context, "ERROR : " + task.getException().getMessage(), Toast.LENGTH_LONG, true).show();

                    }

                }
            });

        }

        String commentMessage = commentsList.get(position).getMessage();
        holder.setComment_message(commentMessage);

    }


    @Override
    public int getItemCount() {

        if(commentsList != null) {

            return commentsList.size();

        } else {

            return 0;

        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private TextView name;

        private ImageView userImage;

        private TextView comment_message;


        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setComment_message(String message){

            comment_message = mView.findViewById(R.id.comment_message);
            comment_message.setText(message);

        }

        public void setUserDescription(String nameText, String userImageString) {

            name = mView.findViewById(R.id.comment_username);
            userImage = mView.findViewById(R.id.comment_image);
            name.setText(nameText);
            Glide.with(context)
                    .load(userImageString)
                    .into(userImage);

        }

    }

}