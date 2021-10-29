package com.boilerplate.firestore.virtualstudy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
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

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

public class SetupActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {


    private EditText setupName;
    private Button setupButton;
    private ProgressBar setupProgress;
    private EditText setupUsername;
    //private Spinner setupCourse;
    //private EditText setupYear;

    private String user_id;
    private Boolean isChanged = false;

    private CircleImageView setUpImage;
    private Uri mainImageUri = null;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private Bitmap compressedProfileImageFile;

    private boolean username_exists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        String back_arrow = getIntent().getStringExtra("Display_arrow");

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        user_id = firebaseAuth.getCurrentUser().getUid();

        //Spinner course and year

        final Spinner spinner = findViewById(R.id.setup_course);
        if (spinner != null) {
            spinner.setOnItemSelectedListener(this);
        }
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.labels_array_course, android.R.layout.simple_spinner_item);

        if (spinner != null) {
            spinner.setAdapter(adapter);
        }

        final Spinner spinner1 = findViewById(R.id.setup_year);
        if (spinner1 != null) {
            spinner1.setOnItemSelectedListener(this);
        }
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
                R.array.labels_array_year, android.R.layout.simple_spinner_item);

        if (spinner1 != null) {
            spinner1.setAdapter(adapter1);
        }



        Toolbar setupToolbar = findViewById(R.id.setupToolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Setup");

        if (back_arrow.equals("no")) {

            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        } else {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }


        setUpImage = findViewById(R.id.setup_image);
        setupButton = findViewById(R.id.setup_btn);
        setupName = findViewById(R.id.setup_name);
        setupProgress = findViewById(R.id.setup_progress);
        setupUsername = findViewById(R.id.setup_username);
        //setupCourse = findViewById(R.id.setup_course);
        //setupYear = findViewById(R.id.setup_year);

        setupProgress.setVisibility(View.VISIBLE);
        setupButton.setEnabled(false);

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {

                        String name = task.getResult().getString("name");
                        String username = task.getResult().getString("username");
                        //String course = task.getResult().getString("course");
                        //String year = task.getResult().getString("date");
                        String image = task.getResult().getString("image");

                        mainImageUri = Uri.parse(image);

                        setupName.setText(name);
                        setupUsername.setText(username);
                        spinner.getSelectedItem();
                        spinner1.getSelectedItem();

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_image);

                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setUpImage);

                    } else {

                        Toasty.warning(SetupActivity.this, "Enter your Account Details ", Toast.LENGTH_LONG, true).show();

                    }

                } else {

                    Toasty.error(SetupActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG, true).show();

                }

                setupProgress.setVisibility(View.INVISIBLE);
                setupButton.setEnabled(true);

            }
        });

        setupButton.setEnabled(true);

        setupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String name = setupName.getText().toString();
                final String username = setupUsername.getText().toString().toLowerCase().trim();
                final String course = spinner.getSelectedItem().toString();
                final String year = spinner1.getSelectedItem().toString();

                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(username) && mainImageUri != null) {

                    if (!setupUsername.getText().toString().contains(" ")) {

                        setupProgress.setVisibility(View.VISIBLE);

                        setupButton.setEnabled(false);

                        if (isChanged) {

                            user_id = firebaseAuth.getCurrentUser().getUid();

                            File newImageFile = new File(mainImageUri.getPath());

                            try {
                                compressedProfileImageFile = new Compressor(SetupActivity.this)
                                        .setMaxHeight(100)
                                        .setMaxWidth(100)
                                        .setQuality(2)
                                        .compressToBitmap(newImageFile);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            compressedProfileImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] profile_image_data = baos.toByteArray();

                            Toasty.info(SetupActivity.this, "Uploading Details", Toast.LENGTH_LONG, true).show();

                            UploadTask uploadTask = storageReference.child("profile_images").child(user_id + ".jpg").putBytes(profile_image_data);

                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    storeFireStore(taskSnapshot, name, username, course, year);

                                    setupButton.setEnabled(true);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    setupButton.setEnabled(true);

                                    Toasty.error(SetupActivity.this, "Image Upload error " + e.getMessage(), Toast.LENGTH_LONG, true).show();

                                }
                            });

                        } else {

                            setupButton.setEnabled(true);

                            storeFireStore(null, name, username, course, year);

                        }

                    } else {

                        setupButton.setEnabled(true);

                        Toasty.warning(SetupActivity.this, "Input Fields cannot contain spaces", Toast.LENGTH_LONG, true).show();

                    }
                } else {

                    setupButton.setEnabled(true);

                    Toasty.warning(SetupActivity.this, "Select profile image , enter name and username", Toast.LENGTH_LONG, true).show();
                }

            }
        });

        setUpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            + ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        Toasty.warning(SetupActivity.this, "Grant Storage Read & Write Permission ", Toast.LENGTH_LONG, true).show();

                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                    } else {

                        imagePicker();

                    }

                } else {

                    imagePicker();

                }

            }
        });

    }

    private void storeFireStore(final UploadTask.TaskSnapshot taskSnapshot, final String name, final String usernameToCompare, final String course, final String year) {

        final Uri download_uri;

        if (taskSnapshot != null) {

            download_uri = taskSnapshot.getDownloadUrl();

        } else {

            download_uri = mainImageUri;
        }

        final Map<String, String> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("username", usernameToCompare);
        userMap.put("course", course);
        userMap.put("year",year);
        userMap.put("image", download_uri.toString());

        CollectionReference allUsersRef = firebaseFirestore.collection("Users");
        allUsersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {

                    for (DocumentSnapshot documentSnapshot : task.getResult()) {

                        String username;

                        username = documentSnapshot.getString("username");

                        if (!documentSnapshot.getId().equals(firebaseAuth.getCurrentUser().getUid())) {

                            if (!TextUtils.isEmpty(username)) {

                                if (username.equals(usernameToCompare)) {

                                    Toasty.warning(SetupActivity.this, "Username Already Exists ", Toast.LENGTH_LONG, true).show();

                                    setupProgress.setVisibility(View.INVISIBLE);

                                    username_exists = true;

                                    return;


                                } else {

                                    username_exists = false;

                                }

                            }

                        }

                    }

                    if (!username_exists) {

                        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    Toasty.success(SetupActivity.this, "The user settings are updated ", Toast.LENGTH_LONG, true).show();

                                    Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                                    startActivity(mainIntent);
                                    finish();

                                } else {

                                    String error = task.getException().getMessage();

                                    Toasty.error(SetupActivity.this, error, Toast.LENGTH_LONG, true).show();

                                }

                                setupProgress.setVisibility(View.INVISIBLE);

                            }
                        });

                    } else {


                        setupProgress.setVisibility(View.INVISIBLE);

                        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                if (task.isSuccessful()) {

                                    if (!name.equals(task.getResult().getString("name")) || taskSnapshot != null) {


                                        String username = task.getResult().getString("username");

                                        final Map<String, Object> userMapWithoutUsername = new HashMap<>();
                                        userMapWithoutUsername.put("name", name);
                                        userMapWithoutUsername.put("username",username);
                                        userMapWithoutUsername.put("course",course);
                                        userMapWithoutUsername.put("year",year);
                                        userMapWithoutUsername.put("image", download_uri.toString());

                                        firebaseFirestore.collection("Users").document(user_id).set(userMapWithoutUsername).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    setupProgress.setVisibility(View.INVISIBLE);

                                                    Toasty.success(SetupActivity.this, "The user settings are updated ", Toast.LENGTH_LONG, true).show();

                                                    Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                                                    startActivity(mainIntent);
                                                    finish();

                                                } else {

                                                    setupProgress.setVisibility(View.INVISIBLE);

                                                    String error = task.getException().getMessage();

                                                    Toasty.error(SetupActivity.this, error, Toast.LENGTH_LONG, true).show();

                                                }


                                            }
                                        });

                                    } else {

                                        setupProgress.setVisibility(View.INVISIBLE);

                                        Toasty.warning(SetupActivity.this, "Change Fields and save settings", Toast.LENGTH_LONG, true).show();


                                    }

                                } else {

                                    setupProgress.setVisibility(View.INVISIBLE);

                                    String error = task.getException().getMessage();

                                    Toasty.error(SetupActivity.this, error, Toast.LENGTH_LONG, true).show();

                                }

                            }

                        });


                    }


                } else {

                    Toasty.error(SetupActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG, true).show();

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

                mainImageUri = result.getUri();

                setUpImage.setImageURI(mainImageUri);

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

                Toasty.error(SetupActivity.this, error.getMessage(), Toast.LENGTH_LONG, true).show();

            }
        }
    }

    private void imagePicker() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(SetupActivity.this);

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String message = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
