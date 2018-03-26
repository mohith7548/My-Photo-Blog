package com.kune.photoblog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private CircleImageView setupImage;
    private TextInputLayout setupName;
    private Button setupButton;
    private ProgressBar setupProgressbar;
    private boolean isChanged = false;

    private StorageReference mStorageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFrebaseFirestore;

    private String user_id;
    private Uri mainImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Toolbar setupToolbar = findViewById(R.id.setup_toolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Setup");

        setupImage = findViewById(R.id.setup_image);
        setupName = findViewById(R.id.setup_name);
        setupButton = findViewById(R.id.setup_save_btn);
        setupProgressbar = findViewById(R.id.setup_progress_bar);

        setupProgressbar.setVisibility(View.VISIBLE);
        setupButton.setEnabled(false);

        mAuth = FirebaseAuth.getInstance();
        mFrebaseFirestore = FirebaseFirestore.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        user_id = mAuth.getCurrentUser().getUid();
        mFrebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {
                        // Data exists in the given path, then ...
                        Toast.makeText(SetupActivity.this, "Data exists!", Toast.LENGTH_SHORT).show();

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        // Initially mainImageUri is set to null, when FirebaseFirestore retrieves data, the retrieved image is parsed and set to mainImageUri
                        mainImageUri = Uri.parse(image);

                        setupName.getEditText().setText(name);

                        // Glide doesn't provide option for imageViewHolder....
                        RequestOptions placeHolderRequest = new RequestOptions();
                        placeHolderRequest.placeholder(R.drawable.profile);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeHolderRequest).load(image).into(setupImage);

                    } else {
                        // Data doesn't exist in the given path, then...

                        Toast.makeText(SetupActivity.this, "Data doesn't exists!", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    String e = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "FIRESTORE Retrieve Error: " + e, Toast.LENGTH_SHORT).show();
                }

                setupProgressbar.setVisibility(View.INVISIBLE);
                setupButton.setEnabled(true);
            }
        });

        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT  >=  Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED ) {

                        Toast.makeText(SetupActivity.this, "Permission Denied!", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    } else {
                        // At first time , it goes to the above if condition, and later after granting permission, it loads into else condition
                        StartCropImageActivity();
                    }
                } else {
                    // No need, since the permission are already granted in playstore
                    StartCropImageActivity();
                }
            }
        });

        setupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String userName = setupName.getEditText().getText().toString();

                if (!TextUtils.isEmpty(userName) && mainImageUri != null) {

                    setupProgressbar.setVisibility(View.VISIBLE);

                    if (isChanged) {

                        //Image is Changed, overwrite the previous image!

                        user_id = mAuth.getCurrentUser().getUid();

                        StorageReference image_path = mStorageReference.child("profile_images").child(user_id + ".jpg");

                        image_path.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if (task.isSuccessful()) {

                                    storeFireStore(task, userName);

                                } else {
                                    setupProgressbar.setVisibility(View.INVISIBLE);

                                    String e = task.getException().getMessage();

                                    Toast.makeText(SetupActivity.this, "Error: " + e, Toast.LENGTH_LONG).show();
                                }


                            }
                        });

                    } else {
                        // Image is not Changed (which is false by default)
                        storeFireStore(null, userName);
                    }
                } else {

                    Toast.makeText(SetupActivity.this, "Username or image is empty!", Toast.LENGTH_SHORT).show();



                }
            }
        });

    }

    private void storeFireStore(Task<UploadTask.TaskSnapshot> task, String userName) {

        Uri download_uri;

        if (task != null) {
            download_uri = task.getResult().getDownloadUrl();
        } else {
            download_uri = mainImageUri;
        }

        //Toast.makeText(SetupActivity.this, "Upload Complete !", Toast.LENGTH_SHORT).show();
        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", userName);
        userMap.put("image", download_uri.toString());

        mFrebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    Toast.makeText(SetupActivity.this, "Upload Complete !", Toast.LENGTH_SHORT).show();
                    // Forward user to the MainActivity
                    Intent intent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    String e = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "FIRESTORE Error: " + e, Toast.LENGTH_SHORT).show();

                }

                setupProgressbar.setVisibility(View.INVISIBLE);
            }
        });


    }

    private void StartCropImageActivity() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SetupActivity.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri = result.getUri();

                setupImage.setImageURI(mainImageUri);

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
