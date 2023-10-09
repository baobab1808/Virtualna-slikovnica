package com.example.project.archive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import com.example.project.R;
import com.example.project.model.ArchiveRow;
import com.example.project.model.DatabasePage;
import com.example.project.model.Picturebook;
import com.example.project.user_profile.Login;
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

import java.util.ArrayList;

public class MyArchive extends AppCompatActivity {

    ArrayList<ArchiveRow> rows;
    ArrayList<Picturebook> picturebooks;
    Picturebook picturebook;
    ArchiveRow row;
    RecyclerView rv;
    PicturebookAdapter pAdapter;
    DatabasePage dbPage;
    String pageId;

    FirebaseAuth auth;
    FirebaseUser loggedInUser;
    DatabaseReference database;
    FirebaseDatabase databaseIns;
    FirebaseStorage storageIns;
    StorageReference storageRef;

    final long ONE_MEGABYTE = 1024 * 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Project);
        setContentView(R.layout.activity_my_archive);

        rows = new ArrayList<>();
        picturebooks = new ArrayList<>();
        rv = findViewById(R.id.recyclerViewArhive);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        loggedInUser = auth.getCurrentUser();
        databaseIns = FirebaseDatabase.getInstance();
        storageIns = FirebaseStorage.getInstance();

        // if user is not logged in, redirect to login page
        if (loggedInUser == null) {
            Intent in = new Intent(this, Login.class);
            startActivity(in);
        }

        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        pAdapter = new PicturebookAdapter(this);
        rv.setAdapter(pAdapter);
        pAdapter.setPicturebooks(rows);

        database = databaseIns.getReference("/picturebooks");
        database.orderByChild("userId").equalTo(loggedInUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    rows.clear();
                    for (DataSnapshot pc : dataSnapshot.getChildren()) {
                        picturebook = pc.getValue(Picturebook.class);
                        picturebook.setId(pc.getKey());
                        picturebooks.add(picturebook);
                    }
                    getFirstPage(picturebooks);
                }
                database.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MyArchive.this, "Failed to read picturebooks." + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // for each picture book get first page
    void getFirstPage(ArrayList<Picturebook> picturebooks) {

        for (Picturebook pc : picturebooks) {
            database = databaseIns.getReference("/pages");
            database.orderByChild("picturebookId").equalTo(pc.getId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        for (DataSnapshot pc : dataSnapshot.getChildren()) {
                            dbPage = pc.getValue(DatabasePage.class);
                            // if pages have page numbers, find first page, else - display random page as first page
                            if (dbPage.getNum() == 1) {
                                pageId = pc.getKey();
                                break;
                            }
                            pageId = pc.getKey();
                        }
                        storageRef = storageIns.getReference().child("images/pages/" + pc.getId() + "/" + pageId);
                        storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                row = new ArchiveRow(pc.getId(), pc.getTitle(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                rows.add(row);
                                pAdapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Toast.makeText(MyArchive.this, "Failed to load page.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    database.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MyArchive.this, "Failed to load picturebook.", Toast.LENGTH_SHORT).show();
                }

            });
        }
    }
}