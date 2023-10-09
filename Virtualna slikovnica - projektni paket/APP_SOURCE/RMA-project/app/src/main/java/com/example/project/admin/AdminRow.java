package com.example.project.admin;

import android.graphics.Bitmap;

public class AdminRow {
    private String id;
    private String title;
    private Bitmap firstPage;
    private String authorName;
    private String status;


    public AdminRow(String id, String title, Bitmap image, String author, String status) {
        this.id = id;
        this.title = title;
        this.firstPage = image;
        this.authorName = author;
        this.status = status;
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

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
