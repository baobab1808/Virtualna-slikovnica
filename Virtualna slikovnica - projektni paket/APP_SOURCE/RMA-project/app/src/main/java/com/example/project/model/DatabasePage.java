package com.example.project.model;

public class DatabasePage {

    private String id;
    private String picturebookId;
    private String caption;
    private int num;

    public DatabasePage() {
        // Default constructor required for calls to DataSnapshot.getValue(DatabasePage.class)
    }

    public DatabasePage(String id, String picturebookId) {
        this.picturebookId = picturebookId;
    }

    public DatabasePage(String picturebookId, String caption, int num) {
        this.picturebookId = picturebookId;
        this.caption = caption;
        this.num = num;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getPicturebookId() { return picturebookId; }

    public void setPicturebookId(String id) { picturebookId = id; }

    public String getCaption() { return caption; }

    public void setCaption(String caption) { this.caption = caption; }

    public int getNum() { return num; }

    public void setNum(int num) { this.num = num; }
}
