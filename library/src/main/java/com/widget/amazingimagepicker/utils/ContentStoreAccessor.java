package com.widget.amazingimagepicker.utils;

import android.content.Context;
import android.database.Cursor;
import android.media.RemoteController;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.widget.amazingimagepicker.model.Bucket;
import com.widget.amazingimagepicker.model.Content;
import com.widget.amazingimagepicker.model.Thumbnail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentStoreAccessor {

    private static Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private static Uri VIDEO_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    private static Uri VIDEO_THUMB_URI = MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI;

    public static List<Content> getAllVideos(Context context) {
        Map<String, Thumbnail> thumbnails = getThumbnails(context);
        String[] projection = {MediaStore.Video.VideoColumns.BUCKET_ID, MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME, MediaStore.Video.VideoColumns.DATE_MODIFIED, MediaStore.Video.VideoColumns._ID, MediaStore.Video.VideoColumns.DATA, MediaStore.Video.VideoColumns.DURATION, MediaStore.Video.VideoColumns.RESOLUTION};
        String ORDER_BY = MediaStore.Video.Media.DATE_MODIFIED + " DESC";
        Cursor c = context.getContentResolver().query(VIDEO_URI, projection, null, null, ORDER_BY);
        ArrayList<Content> contents = new ArrayList<>();
        Content content;
        Bucket bucket;

        if (c == null) {
            return contents;
        }

        if (c.moveToFirst()) {
            do {
                bucket = new Bucket(c.getString(c.getColumnIndex(MediaStore.Video.VideoColumns.BUCKET_ID)), c.getString(c.getColumnIndex(MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME)));
                content = new Content(bucket, c.getLong(c.getColumnIndex(MediaStore.Video.VideoColumns.DATE_MODIFIED)), c.getString(c.getColumnIndex(MediaStore.Video.VideoColumns._ID)), c.getString(c.getColumnIndex(MediaStore.Video.VideoColumns.DATA)), c.getInt(c.getColumnIndex(MediaStore.Video.VideoColumns.DURATION)), c.getString(c.getColumnIndex(MediaStore.Video.VideoColumns.RESOLUTION)), thumbnails.get(c.getString(c.getColumnIndex(MediaStore.Video.VideoColumns._ID))));
                contents.add(content);
            } while (c.moveToNext());
        }
        c.close();
        return contents;
    }

    private static Map<String, Thumbnail> getThumbnails(Context context) {
        Map<String, Thumbnail> result = new HashMap<>();
        String[] projection = {MediaStore.Video.Thumbnails._ID, MediaStore.Video.Thumbnails.VIDEO_ID, MediaStore.Video.Thumbnails.DATA};
        Cursor c = context.getContentResolver().query(VIDEO_THUMB_URI, projection, null, null, null);

        if (c == null) {
            return result;
        }

        if (c.moveToFirst()) {
            do {
                result.put(c.getString(c.getColumnIndex(MediaStore.Video.Thumbnails.VIDEO_ID)), new Thumbnail(c.getLong(c.getColumnIndex(MediaStore.Video.Thumbnails._ID)), c.getString(c.getColumnIndex(MediaStore.Video.Thumbnails.DATA))));
            } while (c.moveToNext());
        }
        c.close();
        return result;
    }

    public static List<Content> getAllImages(Context context) {
        String[] projection = {MediaStore.Images.ImageColumns.BUCKET_ID, MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_MODIFIED, MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA};
        String ORDER_BY = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
        Cursor c = context.getContentResolver().query(IMAGE_URI, projection, null, null, ORDER_BY);
        ArrayList<Content> contents = new ArrayList<>();
        Content content;
        Bucket bucket;

        if (c == null) {
            return contents;
        }

        if (c.moveToFirst()) {
            do {
                bucket = new Bucket(c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID)), c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)));
                content = new Content(bucket, c.getLong(c.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED)), c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns._ID)), c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns.DATA)));
                contents.add(content);
            } while (c.moveToNext());
        }
        c.close();
        return contents;
    }
}