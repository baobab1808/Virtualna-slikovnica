package com.example.project.explore;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.project.Constants;
import com.example.project.R;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.project.model.DatabasePage;
import com.example.project.model.Page;
import com.example.project.model.Picturebook;
import com.example.project.model.User;
import com.example.project.review.WriteReview;
import com.example.project.showreviews.ShowReviews;
import com.example.project.user_profile.Login;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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

public class ExploreSinglePicturebook extends AppCompatActivity {

    TextView title;
    TextView summary;
    TextView status;
    TextView caption;
    Button follow;
    RecyclerView rv;
    ExplorePagesAdapter pAdapter;
    ArrayList<Page> pages;
    Picturebook picturebook;
    User user;
    String picturebookId;
    DatabasePage dbPage;
    ArrayList<DatabasePage> dbPages;
    boolean following;
    String authorName;
    String picturebookAuthorId;
    ImageButton reviewBtn;
    ImageButton viewReviewsBtn;
    RatingBar ratingBar;
    String fullUserName;

    String followingId;

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
        setContentView(R.layout.activity_explore_single_picturebook);

        title = findViewById(R.id.textViewTitleExplore);
        summary = findViewById(R.id.textViewSummaryExplore);
        status = findViewById(R.id.textViewStatusExplore);
        caption = findViewById(R.id.textCaption);
        follow = findViewById(R.id.follow_button);
        rv = findViewById(R.id.recyclerViewPagesExplore);
        reviewBtn = findViewById(R.id.addReviews);
        viewReviewsBtn = findViewById(R.id.reviewBtn);
        ratingBar =findViewById(R.id.ratingBar4);
        pages = new ArrayList<>();
        dbPages = new ArrayList<>();
        following = false;

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

        getUserFullName(loggedInUser.getUid());

        init();

        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                follow();

            }
        });

        picturebookId = getIntent().getStringExtra("picturebookId");

        reviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExploreSinglePicturebook.this, WriteReview.class);
                intent.putExtra("picturebooksId", picturebookId);
                startActivity(intent);
            }
        });

        viewReviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExploreSinglePicturebook.this, ShowReviews.class);
                intent.putExtra("picturebookId", picturebookId);
                startActivity(intent);
            }
        });

        loadReviews();

    }

    //get logged in user's full name from database
    private void getUserFullName(String uid) {
        database = databaseIns.getReference("/users");
        database.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                fullUserName = user.getFirstName() + ' ' + user.getLastName();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ExploreSinglePicturebook.this, "Failed to read value." + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    //get average ratings and load it in rating bar
    private float ratingsSum = 0;
    private void loadReviews() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/picturebooks");
        ref.child(picturebookId).child("/ratings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ratingsSum = 0;

                for(DataSnapshot ds: snapshot.getChildren()){
                    float rating = Float.parseFloat(""+ds.child("ratings").getValue());

                    ratingsSum = ratingsSum + rating;
                }

                long numberOfReviews = snapshot.getChildrenCount();
                float avgRating = ratingsSum/numberOfReviews;

                ratingBar.setRating(avgRating);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    void init() {
        //get author's full name
        picturebookId = getIntent().getStringExtra("picturebookId");
        database2 = databaseIns.getReference("/users");

        database = databaseIns.getReference("/picturebooks");
        database.child(picturebookId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                picturebook = dataSnapshot.getValue(Picturebook.class);
                picturebookAuthorId = picturebook.getUserId();
                // user can't follow himself
                if (loggedInUser.getUid().equals(picturebookAuthorId)) {
                    follow.setVisibility(View.GONE);
                }
                database2.child(picturebookAuthorId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                        authorName = user.getFirstName() + ' ' + user.getLastName();
                        status.setText(authorName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ExploreSinglePicturebook.this, "Failed to read value." + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                title.setText(picturebook.getTitle());
                summary.setText(picturebook.getSummary());

                database.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ExploreSinglePicturebook.this, "Failed to read value." + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        pAdapter = new ExplorePagesAdapter(this);
        rv.setAdapter(pAdapter);
        pAdapter.setImages(pages);

        //get picture book's pages
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
                Toast.makeText(ExploreSinglePicturebook.this, "Failed to read value." + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //follow/unfollow user
        database = databaseIns.getReference("/users/" + loggedInUser.getUid() + "/following");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.getValue().equals(picturebookAuthorId)) {
                            followingId = ds.getKey();
                            following = true;
                            follow.setText("Unfollow");
                        }
                    }
                }
                if (!following) {
                    follow.setText("Follow");
                }
                database.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {            }
        });
    }

    //get picturebook pages from firebase storage database
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
                    Collections.sort(pages);
                    pAdapter.notifyItemRangeChanged(0, pages.size() - 1);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(ExploreSinglePicturebook.this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //put in database if user is followinf another user/remove if it unfollowed that user (who it was following)
    void follow() {
        if (!following) {
            database = databaseIns.getReference("/users/" + loggedInUser.getUid() + "/following");
            database.push().setValue(picturebookAuthorId);
            database.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                    followingId = dataSnapshot.getKey();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {                }

                });
            following = true;
            follow.setText("Unfollow");
            prepareNotificationMessage(picturebookAuthorId);
        } else {
            database = databaseIns.getReference("/users/" + loggedInUser.getUid() + "/following");
            database.child(followingId).removeValue();
            following = false;
            follow.setText("Follow");
        }
    }

    //prepare notification message for when someone start's following you (it is send to author)
    private void prepareNotificationMessage(String authorId){

        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE = "New follow ";
        String NOTIFICATION_MESSAGE = "User " + fullUserName + " started following you!";
        String NOTIFICATION_TYPE = "NewFollow";

        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("userId", loggedInUser.getUid());
            notificationBodyJo.put("authorId", picturebookAuthorId);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);

            notificationJo.put("to", NOTIFICATION_TOPIC);
            notificationJo.put("data", notificationBodyJo);


        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        sendFcmNotification(notificationJo);

    }

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

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}