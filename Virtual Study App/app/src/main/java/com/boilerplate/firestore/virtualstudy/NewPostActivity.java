package com.boilerplate.firestore.virtualstudy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    private Toolbar newPostToolbar;

    private ImageView newPostImage;
    private EditText newPostDesc;
    private Button postBtn;
    private ProgressBar postProgressBar;
    private ImageView uploadIcon;
    private TextView character_limit;

    private Uri postImageUri = null;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;

    private Bitmap compressedImageFile;

    String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        current_user_id = mAuth.getCurrentUser().getUid();

        newPostToolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newPostImage = findViewById(R.id.new_post_image);
        newPostDesc = findViewById(R.id.new_post_desc);
        postBtn = findViewById(R.id.post_btn);
        postProgressBar = findViewById(R.id.post_progress);
        uploadIcon = findViewById(R.id.upload_icon);
        character_limit = findViewById(R.id.character_limit);


        newPostDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                character_limit.setText(newPostDesc.getText().length()+"/900");

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(4, 3)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .start(NewPostActivity.this);

            }
        });

        postBtn.setEnabled(true);

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String desc = newPostDesc.getText().toString();

                if (!TextUtils.isEmpty(desc) && postImageUri != null) {

                    postProgressBar.setVisibility(View.VISIBLE);

                    postBtn.setEnabled(false);

                    final String randomName = UUID.randomUUID().toString();

                    Toasty.info(NewPostActivity.this, "Uploading Question", Toast.LENGTH_LONG, true).show();

                    StorageReference file_path = storageReference.child("post_images").child(randomName + ".jpg");
                    file_path.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                            if (task.getResult() != null) {

                                final String downloadUri = task.getResult().getDownloadUrl().toString();

                                if (task.isSuccessful()) {

                                    File newImageFile = new File(postImageUri.getPath());

                                    try {
                                        compressedImageFile = new Compressor(NewPostActivity.this)
                                                .setMaxHeight(100)
                                                .setMaxWidth(100)
                                                .setQuality(2)
                                                .compressToBitmap(newImageFile);

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                    byte[] thumb_data = baos.toByteArray();

                                    UploadTask uploadTask = storageReference.child("post_images/thumbs")
                                            .child(randomName + ".jpg").putBytes(thumb_data);

                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            String downloathumbUri = taskSnapshot.getDownloadUrl().toString();

                                            Map<String, Object> postMap = new HashMap<>();
                                            postMap.put("image_url", downloadUri);
                                            postMap.put("image_thumb", downloathumbUri);
                                            postMap.put("desc", desc);
                                            postMap.put("user_id", current_user_id);
                                            postMap.put("timestamp", FieldValue.serverTimestamp());

                                            firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentReference> task) {

                                                    if (task.isSuccessful()) {

                                                        Toasty.success(NewPostActivity.this, "Post Successfully Added", Toast.LENGTH_LONG, true).show();

                                                        Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                                                        startActivity(mainIntent);
                                                        finish();

                                                    } else {

                                                        postBtn.setEnabled(true);

                                                        String error = task.getException().getMessage();

                                                        Toasty.error(NewPostActivity.this, error, Toast.LENGTH_LONG, true).show();

                                                    }

                                                    postProgressBar.setVisibility(View.INVISIBLE);

                                                }

                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();

                                                    StorageReference ImageRef = storageRef.child("post_images").child(randomName + ".jpg");

                                                    ImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            Log.i("delImgPostnotAdded", randomName + ".jpg removed");

                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {

                                                            Log.i("delImgPostnotAdded", randomName + ".jpg not Found");

                                                        }
                                                    });

                                                    StorageReference ThumbImageRef = storageRef.child("post_images/thumbs")
                                                            .child(randomName + ".jpg");

                                                    ThumbImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            Log.i("delThumbImgPostnotAdded", randomName + ".jpg removed");

                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {

                                                            Log.i("delThumbImgPostnotAdded", randomName + ".jpg not Found");

                                                        }
                                                    });

                                                }
                                            });

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();

                                            StorageReference ImageRef = storageRef.child("post_images").child(randomName + ".jpg");

                                            ImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    Log.i("delImgPostnotAdded", randomName + ".jpg removed");

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Log.i("delImgPostnotAdded", randomName + ".jpg not Found");

                                                }
                                            });
                                            ;

                                            StorageReference ThumbImageRef = storageRef.child("post_images/thumbs")
                                                    .child(randomName + ".jpg");

                                            ThumbImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    Log.i("delThumbImgPostnotAdded", randomName + ".jpg removed");

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Log.i("delThumbImgPostnotAdded", randomName + ".jpg not Found");

                                                }
                                            });
                                            ;

                                            postBtn.setEnabled(true);


                                            Toasty.error(NewPostActivity.this, e.getMessage(), Toast.LENGTH_LONG, true).show();

                                        }
                                    });


                                } else {

                                    postBtn.setEnabled(true);

                                    postProgressBar.setVisibility(View.INVISIBLE);

                                    String error = task.getException().getMessage();

                                    Toasty.error(NewPostActivity.this, error, Toast.LENGTH_LONG, true).show();

                                }

                            } else {

                                postBtn.setEnabled(true);

                                postProgressBar.setVisibility(View.INVISIBLE);

                                String error = task.getException().getMessage();

                                Toasty.error(NewPostActivity.this, error, Toast.LENGTH_LONG, true).show();

                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();

                            StorageReference ImageRef = storageRef.child("post_images").child(randomName + ".jpg");

                            ImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Log.i("delImgPostnotAdded", randomName + ".jpg removed");

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Log.i("delImgPostnotAdded", randomName + ".jpg not Found");

                                }
                            });

                            StorageReference ThumbImageRef = storageRef.child("post_images/thumbs")
                                    .child(randomName + ".jpg");

                            ThumbImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Log.i("delThumbImgPostnotAdded", randomName + ".jpg removed");

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Log.i("delThumbImgPostnotAdded", randomName + ".jpg not Found");

                                }
                            });

                            Toasty.error(NewPostActivity.this, e.getMessage(), Toast.LENGTH_LONG, true).show();

                        }
                    });
                } else {

                    Toasty.warning(NewPostActivity.this, "Please select image & Add some Description", Toast.LENGTH_LONG, true).show();

                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();

                uploadIcon.setVisibility(View.GONE);

                newPostImage.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

                Toasty.error(NewPostActivity.this, error.getMessage(), Toast.LENGTH_LONG, true).show();

            }
        }
    }
}
