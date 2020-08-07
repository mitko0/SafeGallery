package com.example.safegallery.tabs.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import androidx.annotation.NonNull;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class StorageData {

    public static String APP_SAFE_DATA_PATH = "/storage/emulated/0/.SafeGallery/";
    public static String APP_DATA_PATH = "/storage/emulated/0/SafeGallery/";
    public static String TMP_FOLDER = "/storage/emulated/0/.SafeGallery/Tmp/";
    public static String TMP_FILE_NAME = "tmp.";
    public static String TMP_FILE_NAME_2 = "tmp2.";

    public static List<DataPath> loadDataPathsForMedia(ContentResolver contentResolver, DataType type) {

        List<DataPath> result = new ArrayList<>();

        Cursor cursor;
        String[] projection = new String[]{MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.MIME_TYPE};
        Uri uri;

        switch (type) {
            case Audio:
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                cursor = contentResolver.query(uri, projection, null, null, null);
                if (cursor != null) {
                    result = getCursorData(cursor);
                }
                break;
            case Gallery:
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                cursor = contentResolver.query(uri, projection, null, null, null);
                if (cursor != null) {
                    result = getCursorData(cursor);
                }

                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                cursor = contentResolver.query(uri, projection, null, null, null);
                if (cursor != null) {
                    result.addAll(getCursorData(cursor));
                }
                break;
           /* case Files:
                uri = MediaStore.Files.getContentUri("external");
                cursor = contentResolver.query(uri, projection, null, null, null);
                if (cursor != null) {
                    result = getCursorData(cursor);
                }
                break;*/
        }

        return result;
    }

    public static List<DataPath> loadDataPathsForPath(String path) {

        List<DataPath> result = new ArrayList<>();

        File parentFile = new File(path);
        if (parentFile.isDirectory()) {
            for (File file : Objects.requireNonNull(parentFile.listFiles())) {
                if (file.isFile()) {
                    try {
                        String extension = FilenameUtils.getExtension(file.getAbsolutePath());
                        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                        byte[] data = DataEncryptor.decrypt(Files.readAllBytes(file.toPath()));
                        DataPath dataPath = new DataPath(file.getAbsolutePath(), mimeType, data);

                        result.add(dataPath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else
                    result.addAll(loadDataPathsForPath(file.getAbsolutePath()));
            }
        }
        return result;
    }

    private static List<DataPath> getCursorData(@NonNull Cursor cursor) {
        int dataColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        int mimeTypeColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE);

        List<DataPath> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            String mimeType = cursor.getString(mimeTypeColumnIndex);
            if (mimeType == null)
                continue;
            DataPath dataPath = new DataPath(cursor.getString(dataColumnIndex), mimeType);
            result.add(dataPath);
        }

        return result;
    }
}
