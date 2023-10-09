package com.example.project.model;

import android.graphics.Bitmap;

public class ExploreRow implements Comparable<ExploreRow> {
    private String id;
    private String title;
    private Bitmap firstPage;
    private String authorName;
    private boolean following;

    public ExploreRow(String id, String title, Bitmap image, String author, boolean following) {
        this.id = id;
        this.title = title;
        this.firstPage = image;
        this.authorName = author;
        this.following = following;
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

    public boolean getFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    @Override
    public int compareTo(ExploreRow exploreRow) {
        if (this.following) {
            return -1;
        }
        if (exploreRow.following) {
            return 1;
        }
        return this.getId().compareTo(exploreRow.getId());
    }
}
