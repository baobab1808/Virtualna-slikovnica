package com.example.project.explore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.model.ExploreRow;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ExplorePicturebookAdapter extends RecyclerView.Adapter<ExplorePicturebookAdapter.MyView> {
    private List<ExploreRow> rows;
    private LayoutInflater mInflater;

    public void setPicturebooks(ArrayList<ExploreRow> rows) {
        this.rows = rows;
    }

    public void setFilteredList(List<ExploreRow> filteredList){
        this.rows = filteredList;
        notifyDataSetChanged();
    }

    public class MyView extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView image;
        TextView title;
        CardView cv;
        TextView author;
        RatingBar ratingBar;

        public MyView(View view) {
            super(view);
            image = view.findViewById(R.id.imageViewExplore);
            title = view.findViewById(R.id.textViewTitleExplore);
            author = view.findViewById(R.id.authorName);
            ratingBar = view.findViewById(R.id.ratingBar5);
            cv = view.findViewById(R.id.card_view_explore);
            cv.setOnClickListener(this);
        }

        @SuppressLint("NonConstantResourceId")
        public void onClick(View v) {
            int position = getBindingAdapterPosition();
            ExploreRow row = rows.get(position);
            Intent in = new Intent(v.getContext(), ExploreSinglePicturebook.class);
            in.putExtra("picturebookId", row.getId());
            v.getContext().startActivity(in);
        }

    }

    public ExplorePicturebookAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ExplorePicturebookAdapter.MyView onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.explore_row_item,
                        parent,
                        false);

        return new ExplorePicturebookAdapter.MyView(itemView);
    }

    @Override
    public void onBindViewHolder(final ExplorePicturebookAdapter.MyView holder, final int position) {

        ExploreRow row = rows.get(position);
        if (rows != null) {
            loadReviews(row, holder);
            holder.title.setText(rows.get(position).getTitle());
            holder.image.setImageBitmap(rows.get(position).getFirstPage());
            holder.author.setText(rows.get(position).getAuthorName());
        }
    }

    //get average ratings and load it in rating bar
    private float ratingsSum = 0;
    private void loadReviews(ExploreRow row, MyView holder) {

        String picturebookId = row.getId();

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

                holder.ratingBar.setRating(avgRating);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        if (rows != null) {
            return rows.size();
        } else {
            return 0;
        }
    }

}

