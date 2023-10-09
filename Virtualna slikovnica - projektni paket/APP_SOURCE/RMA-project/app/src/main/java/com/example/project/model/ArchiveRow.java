package com.example.project.model;

import android.graphics.Bitmap;

public class ArchiveRow {

    private String id;
    private String title;
    private Bitmap firstPage;

    public ArchiveRow(String id, String title, Bitmap image) {
        this.id = id;
        this.title = title;
        this.firstPage = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) { this.id = id; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Bitmap getFirstPage() { return firstPage; }

    public void setFirstPage(Bitmap image) { firstPage = image; }
}
