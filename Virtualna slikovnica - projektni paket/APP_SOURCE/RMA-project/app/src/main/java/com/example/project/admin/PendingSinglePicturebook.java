package com.example.project.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.project.Constants;
import com.example.project.R;
import com.example.project.explore.ExplorePagesAdapter;
import com.example.project.model.DatabasePage;
import com.example.project.model.Page;
import com.example.project.model.Picturebook;
import com.example.project.model.Status;
import com.example.project.model.User;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PendingSinglePicturebook extends AppCompatActivity {

    TextView title;
    TextView summary;
    TextView name;
    TextView caption;
    RecyclerView rv;
    ExplorePagesAdapter pAdapter;
    ArrayList<Page> pages;
    Picturebook picturebook;
    User user;
    String picturebookId;
    DatabasePage dbPage;
    ArrayList<DatabasePage> dbPages;
    String authorName;
    String picturebookAuthorId;
    Button btnApprove;
    Button btnReject;
    TextView picturebookStatus;
    boolean isAdmin = false;
    String adminId;
    String idUser;

    FirebaseAuth auth;
    FirebaseUser loggedInUser;
    DatabaseReference database;
    DatabaseReference database2;
    FirebaseDatabase databaseIns;
    FirebaseStorage storageIns;
    StorageReference storageRef;

    final long ONE_MEGABYTE = 1024 * 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Project);
        setContentView(R.layout.activity_pending_single_picturebook);

        title = findViewById(R.id.textViewTitleAdmin2);
        summary = findViewById(R.id.textViewSummaryAdmin);
        name = findViewById(R.id.textViewStatusAdmin);
        picturebookStatus = findViewById(R.id.textViewStatusAdmin2);
        caption = findViewById(R.id.textCaption);
        btnApprove = findViewById(R.id.approveButton);
        btnReject = findViewById(R.id.rejectButton);
        rv = findViewById(R.id.recyclerViewPagesAdmin);
        pages = new ArrayList<>();
        dbPages = new ArrayList<>();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        loggedInUser = auth.getCurrentUser();
        idUser = loggedInUser.getUid();
        databaseIns = FirebaseDatabase.getInstance();
        storageIns = FirebaseStorage.getInstance();


        // if user is not logged in, redirect to login page
        if (loggedInUser == null) {
            Intent in = new Intent(this, Login.class);
            startActivity(in);
        }

        getIsAdmin(idUser);

        init();

        picturebookId = getIntent().getStringExtra("picturebookId");

        btnApprove.setOnClickListener(view -> {
            approvePicturebook();
        });

        btnReject.setOnClickListener(view -> {
            rejectPicturebook();
        });

    }


    void init() {

        picturebookId = getIntent().getStringExtra("picturebookId");
        database2 = databaseIns.getReference("/users");

        //get picture book author Id
        database = databaseIns.getReference("/picturebooks");
        database.child(picturebookId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                picturebook = dataSnapshot.getValue(Picturebook.class);
                picturebookAuthorId = picturebook.getUserId();

                //get author's full name
                database2.child(picturebookAuthorId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                        authorName = user.getFirstName() + ' ' + user.getLastName();
                        name.setText(authorName);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(PendingSinglePicturebook.this, "Failed to read value." + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                title.setText(picturebook.getTitle());
                summary.setText(picturebook.getSummary());
                picturebookStatus.setText(picturebook.getStatus().toString());

                database.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(PendingSinglePicturebook.this, "Failed to read value." + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        pAdapter = new ExplorePagesAdapter(this);
        rv.setAdapter(pAdapter);
        pAdapter.setImages(pages);

        //get picture book's pages from database
        database = databaseIns.getReference("/pages");
        database.orderByChild("picturebookId").equalTo(picturebookId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    pages.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        dbPage = ds.getValue(DatabasePage.class);
                        dbPage.setId(ds.getKey());
                        dbPages.add(dbPage);
                    }
                }
                getPages();
                database.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(PendingSinglePicturebook.this, "Failed to read value." + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    //get picture book's pages from Firebase Storage database
    void getPages() {
        for (DatabasePage page : dbPages) {
            storageRef = storageIns.getReference().child("images/pages/" + picturebookId + "/" + page.getId());
            storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    if (page.getNum() != 0) {
                        pages.add(new Page(page.getId(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length), page.getCaption(), page.getNum()));
                    } else {
                        pages.add(new Page(page.getId(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length), page.getCaption()));
                    }
                    //sort pages by their number
                    Collections.sort(pages);
                    pAdapter.notifyItemRangeChanged(0, pages.size() - 1);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(PendingSinglePicturebook.this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //function for approving a picture book
    public void approvePicturebook() {
        database = databaseIns.getReference("/picturebooks");
        database.child("/" + picturebookId).child("status").setValue(Status.PUBLISHED);
        picturebookStatus.setText("Published");
        btnApprove.setEnabled(false);
        btnReject.setEnabled(false);

        String message = "Picturebook is approved and published";
        Toast.makeText(PendingSinglePicturebook.this, message, Toast.LENGTH_SHORT).show();
        prepareNotificationMessage(picturebookId, message);
    }

    //function for rejecting a picture book
    public void rejectPicturebook() {
        database = databaseIns.getReference("/picturebooks");
        database.child("/" + picturebookId).child("status").setValue(Status.REJECTED);
        picturebookStatus.setText("Rejected");
        btnReject.setEnabled(false);
        btnApprove.setEnabled(false);

        String message = "Picturebook is rejected.";
        Toast.makeText(PendingSinglePicturebook.this, message, Toast.LENGTH_SHORT).show();
        prepareNotificationMessage(picturebookId, message);
    }

    //function to check if user is admin and get it's Id
    private void getIsAdmin(String userId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/users");
        ref.child(userId).orderByChild("admin").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                if(user.getAdmin() == true){
                    isAdmin = true;
                    adminId = userId;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //function for preparing push notification's message
    private void prepareNotificationMessage(String picturebookId, String message){

        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE = "Your picturebook "+ picturebook.getTitle();
        String NOTIFICATION_MESSAGE = ""+ message;
        String NOTIFICATION_TYPE = "PicturebookStatusChanged";

        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("userId", picturebookAuthorId);
            notificationBodyJo.put("adminId", adminId);
            notificationBodyJo.put("picturebookId", picturebookId);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);

            notificationJo.put("to", NOTIFICATION_TOPIC);
            notificationJo.put("data", notificationBodyJo);


        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        sendFcmNotification(notificationJo);

    }

    //function for sending push notification messages
    private void sendFcmNotification(JSONObject notificationJo) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=" + Constants.FCM_KEY);

                return headers;
            }
        };

        //setting up and starting a request queue
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

}