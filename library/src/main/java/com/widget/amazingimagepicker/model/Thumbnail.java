package com.widget.amazingimagepicker.model;

public class Thumbnail {
    private final long id;
    private final String path;

    public Thumbnail(long id, String path) {
        this.id = id;
        this.path = path;
    }

    public long getId() {
        return id;
    }

    public String getPath() {
        return path;
    }
}