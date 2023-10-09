package com.example.project.admin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;

import java.util.ArrayList;
import java.util.List;


public class PendingPicturebooksAdapter extends RecyclerView.Adapter<PendingPicturebooksAdapter.MyView>{
    private List<AdminRow> rows;
    private LayoutInflater mInflater;

    public void setPicturebooks(ArrayList<AdminRow> rows) {
        this.rows = rows;
    }

    //filtered list for searchView
    public void setFilteredList(List<AdminRow> filteredList){
        this.rows = filteredList;
        notifyDataSetChanged();
    }

    public class MyView extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView image;
        TextView title;
        CardView cv;
        TextView author;
        TextView status;


        public MyView(View view) {
            super(view);
            image = view.findViewById(R.id.imageViewAdmin);
            title = view.findViewById(R.id.textViewTitleAdmin);
            author = view.findViewById(R.id.authorNameAdmin);
            status = view.findViewById(R.id.statusAdmin);
            cv = view.findViewById(R.id.card_view_admin);
            cv.setOnClickListener(this);
        }

        @SuppressLint("NonConstantResourceId")
        public void onClick(View v) {
            int position = getBindingAdapterPosition();
            AdminRow row = rows.get(position);
            Intent in = new Intent(v.getContext(), PendingSinglePicturebook.class);
            in.putExtra("picturebookId", row.getId());
            v.getContext().startActivity(in);
        }

    }

    public PendingPicturebooksAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public PendingPicturebooksAdapter.MyView onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.admin_row_item,
                        parent,
                        false);

        return new PendingPicturebooksAdapter.MyView(itemView);
    }


    @Override
    public void onBindViewHolder(final PendingPicturebooksAdapter.MyView holder, final int position) {
        if (rows != null) {
            holder.title.setText(rows.get(position).getTitle());
            holder.image.setImageBitmap(rows.get(position).getFirstPage());
            holder.author.setText(rows.get(position).getAuthorName());
            holder.status.setText(rows.get(position).getStatus());
        }
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
