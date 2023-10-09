package com.example.project.editpage.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.editpage.interfaces.ViewOnClick;
import com.example.project.R;

public class ToolsViewHolder extends RecyclerView.ViewHolder {
    public ImageView icone;
    public TextView name;

    private ViewOnClick viewOnClick;

    public void setViewOnClick(ViewOnClick viewOnClick) {
        this.viewOnClick = viewOnClick;
    }

    public ToolsViewHolder(@NonNull View itemView) {
        super(itemView);

        icone = itemView.findViewById(R.id.tools_icone);
        name = itemView.findViewById(R.id.tools_name);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewOnClick.onClick(getAdapterPosition());
            }
        });
    }
}
