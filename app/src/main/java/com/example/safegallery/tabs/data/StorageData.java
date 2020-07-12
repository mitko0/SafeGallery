package com.example.safegallery.tabs.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.*;

public class StorageData {

    private static List<DataPath> getCursorData(@NonNull Cursor cursor) {
        int dataColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        int mimeTypeColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE);

        List<DataPath> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            DataPath dataPath = new DataPath(cursor.getString(dataColumnIndex), cursor.getString(mimeTypeColumnIndex));
            result.add(dataPath);
        }

        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static List<DataPath> loadDataPathsForMedia(ContentResolver contentResolver, DataType type) {

        List<DataPath> result = new ArrayList<>();

        Cursor cursor;
        String[] projection = new String[]{MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.MIME_TYPE};
        String orderBy = MediaStore.MediaColumns.DATE_TAKEN;
        Uri uri;

        switch (type) {
            case Audio:
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                cursor = contentResolver.query(uri, projection, null, null, orderBy + " DESC");
                if (cursor != null) {
                    result = getCursorData(cursor);
                }
                break;
            case Gallery:
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                cursor = contentResolver.query(uri, projection, null, null, orderBy + " DESC");
                if (cursor != null) {
                    result = getCursorData(cursor);
                }

                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                cursor = contentResolver.query(uri, projection, null, null, orderBy + " DESC");
                if (cursor != null) {
                    result.addAll(getCursorData(cursor));
                }
                break;
            /*case Files:
                uri = MediaStore.Files.getContentUri("external");
                cursor = contentResolver.query(uri, projection, null, null, orderBy + " DESC");
                if (cursor != null) {
                    result = getCursorData(cursor);
                }
                break;*/
        }

        return result;
    }
}
