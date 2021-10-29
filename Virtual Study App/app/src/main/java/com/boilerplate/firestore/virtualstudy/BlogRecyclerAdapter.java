package com.boilerplate.firestore.virtualstudy;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.koushikdutta.ion.Ion;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import es.dmoral.toasty.Toasty;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {


    public List<BlogPost> blog_list;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private Context context;

    String myUid;
    //Notification

    String hisUid;
    String currentUserID;
    String post_id;

    private @ServerTimestamp
    Date timestamp;

    public BlogRecyclerAdapter(List<BlogPost> blog_list, Context context) {

        this.context = context;
        this.blog_list = blog_list;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        currentUserID = firebaseAuth.getCurrentUser().getUid();

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        final String blog_post_id = blog_list.get(position).blogPostId;
        final String currentUserID = firebaseAuth.getCurrentUser().getUid();

        post_id = blog_list.get(position).blogPostId;

        String desc_data = blog_list.get(position).getDesc();
        String blog_image_thumb = blog_list.get(position).getImage_thumb();
        final String blog_image = blog_list.get(position).getImage_url();

        timestamp = blog_list.get(position).getTimestamp();

        //hisUid = blog_list.get(position).getUser_id();

        final String hisUid = blog_list.get(position).getUser_id();

        final String user_id = blog_list.get(position).getUser_id();


        if(firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore.collection("Users").document(user_id).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (task.isSuccessful()) {
                                holder.setUserDescription(task.getResult().getString("name"), task.getResult().getString("image"));

                            } else {

                                Toasty.error(context, "Image Upload error " + task.getException().getMessage(), Toast.LENGTH_LONG, true).show();

                            }

                        }
                    });
        }



        holder.setBlogImage(blog_image_thumb, blog_image);
        holder.setDescText(desc_data);


        if (timestamp != null) {

            holder.setTimeStamp();

        }

        if (firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore.collection("Posts/" + blog_post_id + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (documentSnapshots != null) {

                        if (!documentSnapshots.isEmpty()) {

                            int count = documentSnapshots.size();

                            holder.updateLikeCounts(count);

                        } else {

                            holder.updateLikeCounts(0);

                        }

                    }

                }

            });

            firebaseFirestore.collection("Posts/").document(blog_post_id).collection("Report").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (documentSnapshots != null) {

                        if (!documentSnapshots.isEmpty()) {

                            int count = documentSnapshots.size();

                            if (count >= 3) {

                                firebaseFirestore.collection("Posts/").document(blog_post_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if (task.isSuccessful()) {

                                            if (task.getResult().exists()) {

                                                final String blog_image = task.getResult().getString("image_url");

                                                // delete the post
                                                beginDeleteOnReport(blog_post_id,blog_image);

                                            }
                                        }

                                    }
                                });

                            }

                        } else {

                        }

                    }

                }

            });





            firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (documentSnapshots != null) {

                        if (!documentSnapshots.isEmpty()) {

                            int count = documentSnapshots.size();

                            holder.updateCommentCounts(count);

                        } else {

                            holder.updateCommentCounts(0);

                        }

                    }

                }

            });

        }


        if (firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore.collection("Posts/" + blog_post_id + "/Likes").document(currentUserID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                    if (documentSnapshot != null) {

                        if (documentSnapshot.exists()) {

                            holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_accent));

                        } else {

                            holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like__gray));

                        }

                    }

                }
            });

        }

        if (firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore.collection("Posts/" + blog_post_id + "/Report").document(currentUserID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                    if (documentSnapshot != null) {

                        if (documentSnapshot.exists()) {

                            holder.blogReportBtn.setImageDrawable(context.getDrawable(R.drawable.action_warning_yellow));

                        } else {

                            holder.blogReportBtn.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_warning_24));

                        }

                    }

                }
            });

        }




        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts/" + blog_post_id + "/Likes").document(currentUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {
                            if (!task.getResult().exists()) {

                                Map<String, Object> likesMap = new HashMap<>();
                                likesMap.put("timestamp", FieldValue.serverTimestamp());

                                firebaseFirestore.collection("Posts/" + blog_post_id + "/Likes").document(currentUserID).set(likesMap);

                                firebaseFirestore.collection("Users/").document(currentUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if (task.isSuccessful()) {

                                            if (task.getResult().exists()) {

                                                //////add to his notification
                                                final String hisName = task.getResult().getString("name");
                                                final String hisImage = task.getResult().getString("image");

                                                addToHisLikeNotifications(""+hisName, ""+ hisUid, "Liked your post", ""+timestamp, "" + hisImage);
                                                // Toast.makeText(context, name, Toast.LENGTH_SHORT).show();

                                            }
                                        }

                                    }
                                });


                            } else {

                                firebaseFirestore.collection("Posts/" + blog_post_id + "/Likes").document(currentUserID).delete();

                                // Delete liked notification
                                firebaseFirestore.collection("Users").document(hisUid).collection("Likes").document(currentUserID).delete();

                            }

                        } else {

                            Toasty.error(context, "ERROR : " + task.getException().getMessage(), Toast.LENGTH_LONG, true).show();

                        }

                    }

                });

            }
        });

        //
        holder.blogReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts/" + blog_post_id + "/Report").document(currentUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {
                            if (!task.getResult().exists()) {

                                Map<String, Object> likesMap = new HashMap<>();
                                likesMap.put("timestamp", FieldValue.serverTimestamp());


                                firebaseFirestore.collection("Posts/" + blog_post_id + "/Report").document(currentUserID).set(likesMap);

                            } else {

                                firebaseFirestore.collection("Posts/" + blog_post_id + "/Report").document(currentUserID).delete();

                            }

                        } else {

                            Toasty.error(context, "ERROR : " + task.getException().getMessage(), Toast.LENGTH_LONG, true).show();

                        }

                    }

                });

            }
        });

        holder.blogCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra("blog_post_id", blog_post_id);
                context.startActivity(commentIntent);
            }
        });

        holder.blogCommentCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra("blog_post_id", blog_post_id);
                context.startActivity(commentIntent);
            }
        });

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOptions(holder.moreBtn, user_id, myUid, blog_post_id, blog_image);
            }
        });

        holder.blog_post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,FullImage.class);
                intent.putExtra("image", blog_image);
                context.startActivity(intent);
            }
        });


    }

    private void addToHisLikeNotifications(String hisName, String hisUid, String notification, String timestamp, String hisImage) {


        ModelNotification modelNotification = new ModelNotification( hisName,  currentUserID,  hisUid,  notification,  timestamp,  hisImage, post_id);
        CollectionReference ref = FirebaseFirestore.getInstance().collection("Users");
        ref.document(hisUid).collection("Likes").document(currentUserID).set(modelNotification)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        // added successfully



                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        // failed

                    }
                });

    }


    private void showMoreOptions(ImageButton moreBtn, String user_id, String myUid, final String blog_post_id, final String blog_image) {
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        if(user_id.equals(myUid)){
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");

        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if(id==0){
                    begainDelete(blog_post_id,blog_image);
                }
                return false;
            }
        });
        popupMenu.show();

    }

    private void begainDelete(String blog_post_id, String blog_image) {
        if(blog_image.equals("noImage")){
            deleteWithoutImg(blog_post_id);
        }else {
            deleteWithImg(blog_post_id,blog_image);
        }
    }

    private void deleteWithImg(final String blog_post_id, final String blog_image) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Post");
        builder.setMessage("Are you sure to delete your post?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // To delete all the comments, likes and reports

                firebaseFirestore.collection("Posts/").document(blog_post_id).collection("Comments").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        for (DocumentSnapshot snapshot : task.getResult()) {
                            firebaseFirestore.collection("Posts/").document(blog_post_id).collection("Comments").document(snapshot.getId()).delete();
                        }
                    }
                });

                firebaseFirestore.collection("Posts/").document(blog_post_id).collection("Likes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        for (DocumentSnapshot snapshot : task.getResult()) {
                            firebaseFirestore.collection("Posts/").document(blog_post_id).collection("Likes").document(snapshot.getId()).delete();
                        }
                    }
                });


                firebaseFirestore.collection("Posts/").document(blog_post_id).collection("Report").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        for (DocumentSnapshot snapshot : task.getResult()) {
                            firebaseFirestore.collection("Posts/").document(blog_post_id).collection("Report").document(snapshot.getId()).delete();
                        }
                    }
                });


                final StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(blog_image);
                picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        final FirebaseFirestore db =  FirebaseFirestore.getInstance();
                        DocumentReference noteRef = db.collection("Posts")
                                .document(blog_post_id);
                        noteRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toasty.success(context, "Successfully Deleted!", Toast.LENGTH_LONG, true).show();

                                } else {
                                    Toasty.error(context, "Error Deleting ", Toast.LENGTH_LONG, true).show();
                                }
                            }

                        });
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void beginDeleteOnReport(String blog_post_id, String blog_image) {
        if(blog_image.equals("noImage")){
            deleteWithoutImg(blog_post_id);
        }else {
            deleteOnReportWithImg(blog_post_id,blog_image);
        }
    }

    private void deleteOnReportWithImg(final String blog_post_id, final String blog_image) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        final StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(blog_image);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {


                firebaseFirestore.collection("Posts/").document(blog_post_id).collection("Comments").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        for (DocumentSnapshot snapshot : task.getResult()) {
                            firebaseFirestore.collection("Posts/").document(blog_post_id).collection("Comments").document(snapshot.getId()).delete();
                        }
                    }
                });

                firebaseFirestore.collection("Posts/").document(blog_post_id).collection("Likes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        for (DocumentSnapshot snapshot : task.getResult()) {
                            firebaseFirestore.collection("Posts/").document(blog_post_id).collection("Likes").document(snapshot.getId()).delete();
                        }
                    }
                });


                firebaseFirestore.collection("Posts/").document(blog_post_id).collection("Report").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        for (DocumentSnapshot snapshot : task.getResult()) {
                            firebaseFirestore.collection("Posts/").document(blog_post_id).collection("Report").document(snapshot.getId()).delete();
                        }
                    }
                });



                final FirebaseFirestore db =  FirebaseFirestore.getInstance();
                DocumentReference noteRef = db.collection("Posts")
                        .document(blog_post_id);
                noteRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //Toasty.success(context, "Successfully Deleted!", Toast.LENGTH_LONG, true).show();
                            pd.dismiss();

                        } else {
                            pd.dismiss();
                            //Toasty.error(context, "Error Deleting ", Toast.LENGTH_LONG, true).show();
                        }
                    }

                });
            }
        });
    }







//    private void showMoreOptions(ImageButton moreBtn, String user_id, String myUid, final String blog_post_id, final String blog_image) {
//        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);
//
//        if(user_id.equals(myUid)){
//            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
//
//        }
//
//        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                int id = menuItem.getItemId();
//                if(id==0){
//                    begainDelete(blog_post_id,blog_image);
//                }
//                return false;
//            }
//        });
//        popupMenu.show();
//
//    }
//
//    private void begainDelete(String blog_post_id, String blog_image) {
//        if(blog_image.equals("noImage")){
//            deleteWithoutImg(blog_post_id);
//        }else {
//            deleteWithImg(blog_post_id,blog_image);
//        }
//    }
//
//    private void deleteWithImg(final String blog_post_id, final String blog_image) {
//        final ProgressDialog pd = new ProgressDialog(context);
//        pd.setMessage("Deleting...");
//
//        final StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(blog_image);
//        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//
//                final FirebaseFirestore db =  FirebaseFirestore.getInstance();
//                DocumentReference noteRef = db.collection("Posts")
//                        .document(blog_post_id);
//                noteRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if(task.isSuccessful()){
//                            Toasty.success(context, "Successfully Deleted!", Toast.LENGTH_LONG, true).show();
//                            pd.dismiss();
//
//                        } else {
//                            pd.dismiss();
//                            Toasty.error(context, "Error Deleting ", Toast.LENGTH_LONG, true).show();
//                        }
//                    }
//
//                });
//            }
//        });
//    }


    private void deleteWithoutImg(String blog_post_id) {
    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private TextView name;
        private TextView descView;
        private ImageView userImage;
        private ImageView blog_post_image;
        private TextView timeStamp_object;
        private ImageView blogLikeBtn;
        private TextView blogLikeCount;
        private ImageView blogCommentBtn;
        private TextView blogCommentCount;
        private ImageView blogReportBtn;
        private ImageButton moreBtn;


        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            blogCommentCount = mView.findViewById(R.id.blog_comment_count);
            blogCommentBtn = mView.findViewById(R.id.blog_comment_icon);
            blogLikeBtn = mView.findViewById(R.id.blog_like_btn);

            moreBtn = mView.findViewById(R.id.moreBtn);

            blogReportBtn = mView.findViewById(R.id.blog_report_btn);
        }

        public void setDescText(String descText) {

            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(descText);

        }

        public void setUserDescription(String nameText, String userImageString) {

            name = mView.findViewById(R.id.blog_user_name);
            userImage = mView.findViewById(R.id.blog_user_image);
            name.setText(nameText);
            Glide.with(context)
                    .load(userImageString)
                    .into(userImage);

        }


        public void setBlogImage(String blog_image_thumb, String blog_image) {

            blog_post_image = mView.findViewById(R.id.blog_image);

            scaleImage(blog_post_image);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.image_placeholder);

            Glide.with(context)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(blog_image)
                    .thumbnail(Glide.with(context).load(blog_image_thumb))
                    .into(blog_post_image);

        }

        public void setTimeStamp() {

            timeStamp_object = mView.findViewById(R.id.blog_date);
            SimpleDateFormat date = new SimpleDateFormat("d LLLL yyyy", Locale.getDefault());
            date.setTimeZone(TimeZone.getDefault());

            if (timestamp != null) {

                String date_time = date.format(timestamp.getTime());
                timeStamp_object.setText(date_time + " (" + getTimeAgo(timestamp, context) + ") ");

            }

        }

        public void updateLikeCounts(int count) {

            blogLikeCount = mView.findViewById(R.id.blog_like_count);

            if (count == 0) {

                blogLikeCount.setText(" " + count + " Likes");

            } else if (count == 1) {

                blogLikeCount.setText(" " + count + " Like");

            } else {

                blogLikeCount.setText(" " + count + " Likes");

            }

        }

        public void updateCommentCounts(int count) {

            if (count == 0) {

                blogCommentCount.setText(" " + count + " Comments");

            } else if (count == 1) {

                blogCommentCount.setText(" " + count + " Comment");

            } else {

                blogCommentCount.setText(" " + count + " Comments");

            }

        }


    }

    private void scaleImage(ImageView view) throws NoSuchElementException {
        // Get bitmap from the the ImageView.
        Bitmap bitmap = null;

        try {
            Drawable drawing = view.getDrawable();
            bitmap = ((BitmapDrawable) drawing).getBitmap();
        } catch (NullPointerException e) {
            throw new NoSuchElementException("No drawable on given view");
        } catch (ClassCastException e) {
            // Check bitmap is Ion drawable
            bitmap = Ion.with(view).getBitmap();
        }

        // Get current dimensions AND the desired bounding box
        int width = 0;

        try {
            width = bitmap.getWidth();
        } catch (NullPointerException e) {
            throw new NoSuchElementException("Can't find bitmap on given view/drawable");
        }

        int height = bitmap.getHeight();
        int bounding = dpToPx(350);
        /*Log.i("Test", "original width = " + Integer.toString(width));
        Log.i("Test", "original height = " + Integer.toString(height));
        Log.i("Test", "bounding = " + Integer.toString(bounding));*/

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) bounding) / width;
        float yScale = ((float) bounding) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;
       /* Log.i("Test", "xScale = " + Float.toString(xScale));
        Log.i("Test", "yScale = " + Float.toString(yScale));
        Log.i("Test", "scale = " + Float.toString(scale));*/

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        width = scaledBitmap.getWidth(); // re-use
        height = scaledBitmap.getHeight(); // re-use
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);
       /* Log.i("Test", "scaled width = " + Integer.toString(width));
        Log.i("Test", "scaled height = " + Integer.toString(height));*/

        // Apply the scaled bitmap
        view.setImageDrawable(result);

        // Now change ImageView's dimensions to match the scaled image
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);

        /*Log.i("Test", "done");*/
    }

    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public static Date currentDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    public static String getTimeAgo(Date date, Context ctx) {

        if (date == null) {
            return null;
        }

        long time = date.getTime();

        Date curDate = currentDate();
        long now = curDate.getTime();
        if (time > now || time <= 0) {
            return null;
        }

        int dim = getTimeDistanceInMinutes(time);

        String timeAgo = null;

        if (dim == 0) {
            timeAgo = ctx.getResources().getString(R.string.date_util_term_less) + " " + ctx.getResources().getString(R.string.date_util_term_a) + " " + ctx.getResources().getString(R.string.date_util_unit_minute);
        } else if (dim == 1) {
            return "1 " + ctx.getResources().getString(R.string.date_util_unit_minute);
        } else if (dim >= 2 && dim <= 44) {
            timeAgo = dim + " " + ctx.getResources().getString(R.string.date_util_unit_minutes);
        } else if (dim >= 45 && dim <= 89) {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " " + ctx.getResources().getString(R.string.date_util_term_an) + " " + ctx.getResources().getString(R.string.date_util_unit_hour);
        } else if (dim >= 90 && dim <= 1439) {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " " + (Math.round(dim / 60)) + " " + ctx.getResources().getString(R.string.date_util_unit_hours);
        } else if (dim >= 1440 && dim <= 2519) {
            timeAgo = "1 " + ctx.getResources().getString(R.string.date_util_unit_day);
        } else if (dim >= 2520 && dim <= 43199) {
            timeAgo = (Math.round(dim / 1440)) + " " + ctx.getResources().getString(R.string.date_util_unit_days);
        } else if (dim >= 43200 && dim <= 86399) {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " " + ctx.getResources().getString(R.string.date_util_term_a) + " " + ctx.getResources().getString(R.string.date_util_unit_month);
        } else if (dim >= 86400 && dim <= 525599) {
            timeAgo = (Math.round(dim / 43200)) + " " + ctx.getResources().getString(R.string.date_util_unit_months);
        } else if (dim >= 525600 && dim <= 655199) {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " " + ctx.getResources().getString(R.string.date_util_term_a) + " " + ctx.getResources().getString(R.string.date_util_unit_year);
        } else if (dim >= 655200 && dim <= 914399) {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_over) + " " + ctx.getResources().getString(R.string.date_util_term_a) + " " + ctx.getResources().getString(R.string.date_util_unit_year);
        } else if (dim >= 914400 && dim <= 1051199) {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_almost) + " 2 " + ctx.getResources().getString(R.string.date_util_unit_years);
        } else {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " " + (Math.round(dim / 525600)) + " " + ctx.getResources().getString(R.string.date_util_unit_years);
        }

        return timeAgo + " " + ctx.getResources().getString(R.string.date_util_suffix);
    }

    private static int getTimeDistanceInMinutes(long time) {
        long timeDistance = currentDate().getTime() - time;
        return Math.round((Math.abs(timeDistance) / 1000) / 60);
    }

}
