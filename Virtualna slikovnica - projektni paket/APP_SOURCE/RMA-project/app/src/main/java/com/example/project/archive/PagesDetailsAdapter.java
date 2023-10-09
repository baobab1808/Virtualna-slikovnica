package com.example.project.archive;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.model.Page;

import java.util.ArrayList;
import java.util.List;

public class PagesDetailsAdapter extends RecyclerView.Adapter<PagesDetailsAdapter.MyView> {

    private List<Page> pages;
    private LayoutInflater mInflater;
    private int numPages = 0;

    public void setPages(ArrayList<Page> pages) {
        this.pages = pages;
    }

    public void setNumPages(int num) { numPages = num; }

    public PagesDetailsAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public class MyView extends RecyclerView.ViewHolder {

        ImageView image;
        EditText caption;
        RadioGroup rg;
        RadioButton[] btns = new RadioButton[5];

        public MyView(@NonNull View view) {
            super(view);
            image = view.findViewById(R.id.imageViewSinglePage);
            caption = view.findViewById(R.id.editTextCaption);
            rg = view.findViewById(R.id.radioGroupPages);

            for (int i = 0; i < rg.getChildCount(); ++i) {
                View child = rg.getChildAt(i);
                btns[i] = (RadioButton) child;
            }

            caption.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {               }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {               }

                @Override
                public void afterTextChanged(Editable editable) {
                    Page page = pages.get(getAbsoluteAdapterPosition());
                    page.setCaption(editable.toString());
                    pages.set(getAbsoluteAdapterPosition(), page);
                }
            });

            rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    RadioButton radioButton = rg.findViewById(i);
                    int index = rg.indexOfChild(radioButton);
                    Page page = pages.get(getAbsoluteAdapterPosition());
                    boolean flag = false;
                    // check if some other page already has selected page number
                    for (Page anotherPage : pages) {
                        if (anotherPage.getNum() == index + 1 && !anotherPage.getId().equals(page.getId())) {
                            flag = true;
                        }
                    }
                    if (flag) {
                        Toast.makeText(view.getContext(), "Two pages can't have the same page number!", Toast.LENGTH_SHORT).show();
                        page.setNum(0);
                        radioButton.setChecked(false);
                    } else {
                        radioButton.setChecked(true);
                        page.setNum(index + 1);
                    }
                    pages.set(getAbsoluteAdapterPosition(), page);
                }
            });
        }
    }

    @NonNull
    @Override
    public PagesDetailsAdapter.MyView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.edit_pages_row_item,
                        parent,
                        false);

        return new PagesDetailsAdapter.MyView(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PagesDetailsAdapter.MyView holder, int position) {
        if (pages != null) {
            holder.image.setImageBitmap(pages.get(position).getImage());
            holder.caption.setText(pages.get(position).getCaption());
            // hide buttons if picture book has less than 5 pages
            for (int i = 0; i <= 4; ++i) {
                if (i >= numPages) {
                    holder.btns[i].setVisibility(View.GONE);
                }
                if (pages.get(position).getNum() - 1 == i) {
                    if (!holder.btns[i].isChecked()) {
                        holder.btns[i].setChecked(true);
                    }
                } else {
                    if (holder.btns[i].isChecked()) {
                        holder.btns[i].setChecked(false);
                    }
                }
            }
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
