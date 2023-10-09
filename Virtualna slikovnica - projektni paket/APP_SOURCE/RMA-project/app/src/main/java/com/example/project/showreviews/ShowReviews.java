package com.example.project.showreviews;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.R;
import com.example.project.model.DatabasePage;
import com.example.project.model.PicturebookCoverImage;
import com.example.project.model.Review;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

//activity for displaying reviews
public class ShowReviews extends AppCompatActivity {

    private String picturebooksId;
    private ImageView coverPicture;
    private TextView namePicturebook;
    private RatingBar ratingBar;
    private TextView ratingScore;
    private RecyclerView reviewRV;

    private FirebaseAuth firebaseAuth;
    DatabasePage dbPage;
    String pageId;
    StorageReference storageRef;
    final long ONE_MEGABYTE = 1024 * 1024;
    private ArrayList<Review> reviewArrayList;
    private ReviewAdapter reviewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Project);
        setContentView(R.layout.activity_show_reviews);

        coverPicture = findViewById(R.id.coverPicture);
        namePicturebook = findViewById(R.id.namePicturebook);
        ratingBar = findViewById(R.id.ratingBar2);
        ratingScore = findViewById(R.id.ratingScore);
        reviewRV = findViewById(R.id.reviewRV);


        picturebooksId = getIntent().getStringExtra("picturebookId");

        firebaseAuth = FirebaseAuth.getInstance();
        loadPicturebookDetails();
        loadReviews();
    }

    //load all reviews with ratings for that picture book
    private float ratingsSum = 0;
    private void loadReviews() {

        reviewArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/picturebooks");
        ref.child(picturebooksId).child("/ratings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                reviewArrayList.clear();
                ratingsSum = 0;

                for(DataSnapshot ds: snapshot.getChildren()){
                    float rating = Float.parseFloat(""+ds.child("ratings").getValue());

                    ratingsSum = ratingsSum + rating;

                    Review modelReview = ds.getValue(Review.class);
                    reviewArrayList.add(modelReview);
                }

                reviewAdapter = new ReviewAdapter(ShowReviews.this, reviewArrayList);
                reviewRV.setAdapter(reviewAdapter);

                long numberOfReviews = snapshot.getChildrenCount();

                if(numberOfReviews != 0){
                    float avgRating = ratingsSum/numberOfReviews;
                    ratingScore.setText(String.format("%.2f", avgRating) + " (" + numberOfReviews + ")");
                    ratingBar.setRating(avgRating);
                }else{
                    ratingScore.setText("No reviews" + " (" + numberOfReviews + ")");
                    ratingBar.setRating(0);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //load picture book details to display them above reviews
    private void loadPicturebookDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/users");
        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("/picturebooks");
        DatabaseReference ref3 = FirebaseDatabase.getInstance().getReference("/pages");
        ref.child(picturebooksId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ref2.child(picturebooksId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String picturebookNames = "" + snapshot.child("title").getValue();
                        namePicturebook.setText(picturebookNames);

                        ref3.orderByChild("picturebookId").equalTo(picturebooksId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String pageNumber = "" + snapshot.child("num").getValue();
                                if (snapshot.exists()) {
                                    for (DataSnapshot pc : snapshot.getChildren()) {
                                        dbPage = pc.getValue(DatabasePage.class);
                                        // if pages have page numbers, find first page, else - display random page as first page
                                        if (dbPage.getNum() == 1) {
                                            pageId = pc.getKey();
                                            break;
                                        }
                                        pageId = pc.getKey();
                                    }
                                    storageRef = FirebaseStorage.getInstance().getReference().child("images/pages/" + picturebooksId + "/" + pageId);
                                    storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            PicturebookCoverImage picturebookImage;
                                            picturebookImage = new PicturebookCoverImage(picturebooksId, BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                            coverPicture.setImageBitmap(picturebookImage.getFirstPage());
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            Toast.makeText(ShowReviews.this, "Failed to load page.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}