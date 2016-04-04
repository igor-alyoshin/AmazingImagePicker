package com.widget.amazingimagepicker.model;

import android.net.Uri;
import android.text.TextUtils;

public class Content {

    private static final String SCHEME = "file://";

    private Bucket bucket;
    private long date;
    private String id;
    private String path;
    private Thumbnail thumbnail;
    private int duration;
    private int width = -1;
    private int height = -1;
    private Type type;

    public Content(Bucket bucket, long date, String id, String path) {
        this.bucket = bucket;
        this.date = date;
        this.id = id;
        this.path = path;
        this.type = Type.IMAGE;
    }

    public Content(Bucket bucket, long date, String id, String path, int duration, String resolution, Thumbnail thumbnail) {
        this.bucket = bucket;
        this.date = date;
        this.id = id;
        this.path = path;
        this.duration = duration;
        this.thumbnail = thumbnail;
        this.type = Type.VIDEO;
        if (!TextUtils.isEmpty(resolution) && resolution.split("x").length == 2) {
            String[] sizes = resolution.split("x");
            height = Integer.valueOf(sizes[0]);
            width = Integer.valueOf(sizes[1]);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDuration() {
        return duration;
    }

    public Type getType() {
        return type;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public Bucket getBucket() {
        return bucket;
    }

    public long getDate() {
        return date;
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public Uri getImageUri() {
        Uri imageURI;
        switch (getType()) {
            case IMAGE:
                imageURI = Uri.parse(SCHEME + getPath());
                break;
            case VIDEO:
                Thumbnail thumbnail = getThumbnail();
                if (thumbnail == null) {
                    imageURI = Uri.parse(SCHEME + getPath());
                } else {
                    imageURI = Uri.parse(SCHEME + thumbnail.getPath());
                }
                break;
            default:
                imageURI = null;
                break;
        }
        return imageURI;
    }

    public Uri getContentUri() {
        Uri imageURI;
        switch (getType()) {
            case IMAGE:
                imageURI = Uri.parse(SCHEME + getPath());
                break;
            case VIDEO:
                imageURI = Uri.parse(SCHEME + getPath());
                break;
            default:
                imageURI = null;
                break;
        }
        return imageURI;
    }

    public enum Type {
        IMAGE, VIDEO
    }
}