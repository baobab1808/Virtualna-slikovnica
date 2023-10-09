package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.project.admin.PendingPicturebooks;
import com.example.project.archive.MyArchive;
import com.example.project.editpage.NewPage;
import com.example.project.explore.Explore;
import com.example.project.model.User;
import com.example.project.notificationsettings.NotificationSettings;
import com.example.project.picturebook.NewPicturebook;
import com.example.project.user_profile.Login;
import com.example.project.user_profile.Settings;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainMenu extends AppCompatActivity {


    ImageButton newPicturebook;
    ImageButton newPage;
    ImageButton archive;
    ImageButton explore;
    ImageButton pending;
    ImageView profileBtn;
    Bitmap image;
    ImageButton settings;
    boolean isAdmin = false;
    String userId;
    User user;

    FirebaseAuth auth;
    FirebaseUser loggedInUser;
    FirebaseStorage storage;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Project);
        setContentView(R.layout.activity_main_menu);

        auth = FirebaseAuth.getInstance();
        loggedInUser = auth.getCurrentUser();
        userId = loggedInUser.getUid();

        // if user is not logged in, redirect to login page
        if (loggedInUser == null) {
            Intent in = new Intent(this, Login.class);
            startActivity(in);
        }

        newPage = findViewById(R.id.buttonNewPage);
        newPicturebook = findViewById(R.id.buttonNewPicturebook);
        profileBtn = findViewById(R.id.imageButtonProfile);
        archive = findViewById(R.id.buttonArchive);
        explore = findViewById(R.id.buttonExplore);
        pending = findViewById(R.id.buttonPending);
        settings = findViewById(R.id.imageButtonNotifications);

        checkProfilePicture();


        profileBtn.setOnClickListener(view -> {
            Intent in = new Intent(view.getContext(), Settings.class);
            view.getContext().startActivity(in);
        });

        newPicturebook.setOnClickListener(view -> {
            Intent in = new Intent(view.getContext(), NewPicturebook.class);
            view.getContext().startActivity(in);
        });

        newPage.setOnClickListener(view -> {
            Intent in = new Intent(view.getContext(), NewPage.class);
            view.getContext().startActivity(in);
        });

        archive.setOnClickListener(view -> {
            Intent in = new Intent(view.getContext(), MyArchive.class);
            view.getContext().startActivity(in);
        });

        explore.setOnClickListener(view -> {
            Intent in = new Intent(view.getContext(), Explore.class);
            view.getContext().startActivity(in);
        });

        settings.setOnClickListener(view -> {
            Intent in = new Intent(view.getContext(), NotificationSettings.class);
            view.getContext().startActivity(in);
        });

        getIsAdmin(userId);
        pending.setOnClickListener(view -> {
            if(isAdmin){
                Intent in = new Intent(view.getContext(), PendingPicturebooks.class);
                view.getContext().startActivity(in);
            }
        });
    }

    private void getIsAdmin(String userId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/users");
        ref.child(userId).orderByChild("admin").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                if(user.getAdmin() == true){
                    isAdmin = true;
                    pending.setVisibility(View.VISIBLE);
                }else{
                    pending.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void checkProfilePicture() {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("images/profile_pictures/" + loggedInUser.getUid());

        final long ONE_MEGABYTE = 1024 * 1024;
        storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                image = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                profileBtn.setImageBitmap(image);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                profileBtn.setImageDrawable(getResources().getDrawable(R.drawable.profile));
            }
        });

    }
}