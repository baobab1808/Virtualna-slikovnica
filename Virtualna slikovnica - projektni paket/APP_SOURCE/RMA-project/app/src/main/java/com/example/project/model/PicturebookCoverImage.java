package com.example.project.model;

import android.graphics.Bitmap;

public class PicturebookCoverImage {
    private String id;
    private Bitmap firstPage;

    public PicturebookCoverImage(String id, Bitmap image) {
        this.id = id;
        this.firstPage = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Bitmap getFirstPage() {
        return firstPage;
    }

    public void setFirstPage(Bitmap firstPage) {
        this.firstPage = firstPage;
    }
}
