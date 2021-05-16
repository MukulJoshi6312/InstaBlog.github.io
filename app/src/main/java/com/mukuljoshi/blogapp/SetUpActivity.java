package com.mukuljoshi.blogapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetUpActivity extends AppCompatActivity {

    private CircleImageView circleImageView;
    private EditText username;
    private Button saveBtn;
    private FirebaseAuth auth;
    private StorageReference storageReference;
    private FirebaseFirestore firestore;
    private String Uid;
    private Uri mImageUri;
    private Toolbar setupToolbar;
    private ProgressBar progressBar;
    private Uri downloadUri = null;
    private boolean isPhotoSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);
        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.dark_blue));

        circleImageView = findViewById(R.id.userprofile);
        username = findViewById(R.id.username);
        saveBtn = findViewById(R.id.savebutton);
        setupToolbar = findViewById(R.id.setup_toolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Profile");
        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        Uid = auth.getCurrentUser().getUid();


        firestore.collection("Users").document(Uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                //progressBar.setVisibility(View.VISIBLE);
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        progressBar.setVisibility(View.INVISIBLE);
                        String name = task.getResult().getString("name");
                        String imageUrl = task.getResult().getString("image");
                        username.setText(name);
                        mImageUri = Uri.parse(imageUrl);
                        //Glide.with(SetUpActivity.this).load(imageUri).into(circleImageView);
                        Picasso.get().load(imageUrl).into(circleImageView);
                    }
                }
            }
        });


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String name = username.getText().toString();
                StorageReference imageRef =
                        storageReference.child("Profile_pics").child(Uid + ".jpg");
                if(isPhotoSelected) {
                    if (!name.isEmpty() && mImageUri != null) {
                        imageRef.putFile(mImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    saveToFireStore(task, name, imageRef);
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(SetUpActivity.this, "Profile setting save",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(SetUpActivity.this, task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(SetUpActivity.this, "Please select picture and enter the your " +
                                        "name",
                                Toast.LENGTH_SHORT).show();
                    }
                }else {
                    saveToFireStore(null,name,imageRef);
                }
            }
        });
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(SetUpActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(SetUpActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1, 1)
                                .start(SetUpActivity.this);
                    }
                }
//
            }
        });
    }

    private void saveToFireStore(Task<UploadTask.TaskSnapshot> task, String name, StorageReference imageRef) {
      if (task != null){
//          imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//              @Override
//              public void onSuccess(Uri uri) {
//                  downloadUri = uri;
//              }
//          });

      }else {
          downloadUri = mImageUri;
      }
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                downloadUri = uri;
                HashMap<String, Object> map = new HashMap<>();
                map.put("name", name);
                map.put("image", downloadUri.toString());
                firestore.collection("Users").document(Uid).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.INVISIBLE);
                            startActivity(new Intent(SetUpActivity.this, MainActivity.class));
                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(SetUpActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("name", name);
//        map.put("image", downloadUri.toString());
//        firestore.collection("Users").document(Uid).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()) {
//                    progressBar.setVisibility(View.INVISIBLE);
//                    startActivity(new Intent(SetUpActivity.this, MainActivity.class));
//                } else {
//                    progressBar.setVisibility(View.INVISIBLE);
//                    Toast.makeText(SetUpActivity.this, task.getException().getMessage(),
//                            Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                circleImageView.setImageURI(mImageUri);
                isPhotoSelected = true;

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                Toast.makeText(this, result.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}