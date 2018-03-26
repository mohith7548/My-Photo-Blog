package com.kune.photoblog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    private Toolbar newPostToolbar;
    private ImageView newPostImage;
    private EditText newPostDesc;
    private Button newPostBtn;
    private Uri postImageUri;
    private ProgressBar newPostProgressbar;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        newPostToolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        newPostImage = findViewById(R.id.new_post_image);
        newPostDesc = findViewById(R.id.new_post_description);
        newPostBtn = findViewById(R.id.new_post_btn);
        newPostProgressbar = findViewById(R.id.new_post_progressbar);

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(2,1)
                        .start(NewPostActivity.this);
            }
        });

        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String description = newPostDesc.getText().toString();

                if (!TextUtils.isEmpty(description)  &&  postImageUri != null) {

                    newPostProgressbar.setVisibility(View.VISIBLE);
                    final String randomName = UUID.randomUUID().toString();

                    StorageReference filePath = storageReference.child("post_images").child(randomName + ".jpg");
                    filePath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            final String downloadUri = task.getResult().getDownloadUrl().toString();

                            if (task.isSuccessful()) {

                                File actualImageFile = new File(postImageUri.getPath());

                                try {
                                    compressedImageFile = new Compressor(NewPostActivity.this)
                                            .setMaxHeight(200)
                                            .setMaxWidth(200)
                                            .setQuality(10)
                                            .compressToBitmap(actualImageFile);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] data = baos.toByteArray();

                                UploadTask uploadTask = storageReference.child("post_images/thumbs").child(randomName + ".jpg").putBytes(data);

                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(NewPostActivity.this, "ThumbImage upload Successfull", Toast.LENGTH_SHORT).show();

                                            String downlaodThumbImageUri = task.getResult().getDownloadUrl().toString();

                                            Map<String, Object> postMap = new HashMap<>();
                                            postMap.put("image_url", downloadUri);
                                            postMap.put("thumb_image_url", downlaodThumbImageUri);
                                            postMap.put("description", description);
                                            postMap.put("user_id", currentUserId);
                                            postMap.put("timestamp", FieldValue.serverTimestamp());

                                            firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                    if (task.isSuccessful()) {

                                                        newPostProgressbar.setVisibility(View.INVISIBLE);

                                                        Toast.makeText(NewPostActivity.this, "Posted Successfully !", Toast.LENGTH_SHORT).show();
                                                        // Send to Main Page
                                                        Intent intent = new Intent(NewPostActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                        finish();


                                                    } else {
                                                        newPostProgressbar.setVisibility(View.INVISIBLE);
                                                        String e = task.getException().getMessage();
                                                        Toast.makeText(NewPostActivity.this, "FIRESTORE Error: " + e, Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                        } else {
                                            String e = task.getException().getMessage();
                                            Toast.makeText(NewPostActivity.this, "THUMBIMAGE store Error: " + e, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });




                            } else {
                                newPostProgressbar.setVisibility(View.INVISIBLE);
                                String e = task.getException().getMessage();
                                Toast.makeText(NewPostActivity.this, "STORAGE Error: " + e, Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();
                newPostImage.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}
