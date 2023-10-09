package com.example.project.explore;

import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.example.project.R;
import com.example.project.model.Page;

import java.util.ArrayList;
import java.util.List;

public class ExplorePagesAdapter extends RecyclerView.Adapter<ExplorePagesAdapter.MyView> {

    private List<Page> pages;
    private LayoutInflater mInflater;
    boolean newPicturebook = false;

    public void setImages(ArrayList<Page> pages) {
        this.pages = pages;
    }

    public void setNewPicturebook(boolean np) { newPicturebook = np; }

    public class MyView extends RecyclerView.ViewHolder {

        ImageView image;
        CardView cv;
        TextView textView;

        public MyView(View view) {
            super(view);
            image = view.findViewById(R.id.imageViewPage);
            cv = view.findViewById(R.id.card_view);
            textView = view.findViewById(R.id.textCaption);
        }

    }

    public ExplorePagesAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ExplorePagesAdapter.MyView onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.picturebook_row_item_explore,
                        parent,
                        false);

        return new ExplorePagesAdapter.MyView(itemView);
    }

    @Override
    public void onBindViewHolder(final ExplorePagesAdapter.MyView holder, final int position) {
        if (pages != null) {
            holder.image.setImageBitmap(pages.get(position).getImage());
            holder.textView.setText(pages.get(position).getCaption());
        }
        if (newPicturebook || (pages.get(position).getId() == null)) {
            holder.cv.setOnLongClickListener(view -> {
                pages.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, pages.size());
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        if (pages != null) {
            return pages.size();
        } else {
            return 0;
        }

    }

}
