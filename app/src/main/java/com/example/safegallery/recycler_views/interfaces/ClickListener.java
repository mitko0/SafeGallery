package com.example.safegallery.recycler_views.interfaces;

import com.example.safegallery.tabs.data.DataPath;

import java.io.File;

public interface ClickListener {
    void onClick(File file, String mimeType);

    void onClick(DataPath dataPath);
}
