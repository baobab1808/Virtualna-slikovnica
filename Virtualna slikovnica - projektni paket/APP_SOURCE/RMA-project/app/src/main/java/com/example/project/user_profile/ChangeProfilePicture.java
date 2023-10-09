package com.example.project.user_profile;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.project.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ChangeProfilePicture extends AppCompatActivity {

    Button choosePictureBtn;
    Button savePictureBtn;
    Button deletePictureBtn;
    Button takePictureBtn;
    ImageView profilePicture;
    Bitmap image;

    Uri filePath;
    Bitmap selectedImageBitmap;

    FirebaseAuth auth;
    FirebaseUser loggedInUser;
    FirebaseStorage storage;
    StorageReference storageRef;

    boolean takenWithCamera;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Project);
        setContentView(R.layout.activity_change_profile_picture);

        choosePictureBtn = findViewById(R.id.buttonChoosePicture);
        savePictureBtn = findViewById(R.id.buttonSavePicture);
        deletePictureBtn = findViewById(R.id.buttonDeletePicture);
        takePictureBtn = findViewById(R.id.buttonTakePicture);
        profilePicture = findViewById(R.id.imageProfilePicture);
        takenWithCamera = false;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        loggedInUser = auth.getCurrentUser();

        // if user is not logged in, redirect to login page
        if (loggedInUser == null) {
            Intent in = new Intent(this, Login.class);
            startActivity(in);
        }

        storage = FirebaseStorage.getInstance();

        // if camera is not available, hide button for taking profile picture
        if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            takePictureBtn.setVisibility(View.GONE);
        }

        checkProfilePicture();

        takePictureBtn.setOnClickListener(view -> {
            takeProfilePicture();
        });

        choosePictureBtn.setOnClickListener(view -> {
            chooseProfilePicture();
        });

        savePictureBtn.setOnClickListener(view -> {
            if (selectedImageBitmap == null && !takenWithCamera) {
                Toast.makeText(ChangeProfilePicture.this, "Please choose picture.", Toast.LENGTH_SHORT).show();
            } else {
                uploadProfilePicture();
            }
        });

        deletePictureBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.delete_profile_picture)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            deleteProfilePicture();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // do nothing
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();

        });
    }

    // open camera
    private void takeProfilePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    // after taking photo, set it in profile picture image view
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            profilePicture.setImageBitmap(imageBitmap);
            takenWithCamera = true;
        }
    }

    // choose profile picture from gallery
    void chooseProfilePicture() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        launchSomeActivity.launch(i);
    }

    // get result from gallery and display image
    ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        filePath = data.getData();
                        try {
                            selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                                    this.getContentResolver(), filePath
                            );
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                        profilePicture.setImageBitmap(selectedImageBitmap);
                    }
                }
            });

    // save profile picture in storage
    void uploadProfilePicture() {
        storageRef = storage.getReference();
        storageRef = storageRef.child("images/profile_pictures/"+ loggedInUser.getUid());

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        BitmapDrawable drawable = (BitmapDrawable) profilePicture.getDrawable();
        Bitmap bm = drawable.getBitmap();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 10, bytes);
        byte[] reducedImage = bytes.toByteArray();
        storageRef.putBytes(reducedImage)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Toast.makeText(ChangeProfilePicture.this, "Profile picture successfully uploaded!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ChangeProfilePicture.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                .getTotalByteCount());
                        progressDialog.setMessage("Uploaded "+(int)progress+"%");
                    }
                });
    }

    void checkProfilePicture() {
        storageRef = storage.getReference().child("images/profile_pictures/" + loggedInUser.getUid());

        final long ONE_MEGABYTE = 1024 * 1024;
        storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                image = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                profilePicture.setImageBitmap(image);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                profilePicture.setImageDrawable(getResources().getDrawable(R.drawable.profile));
            }
        });
    }

    void deleteProfilePicture() {
        storageRef = storage.getReference().child("images/profile_pictures/" + loggedInUser.getUid());

        storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                profilePicture.setImageDrawable(getResources().getDrawable(R.drawable.profile));
                Toast.makeText(ChangeProfilePicture.this, "Profile picture deleted!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(ChangeProfilePicture.this, "Unsuccessful!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}