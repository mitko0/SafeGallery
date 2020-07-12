package com.example.safegallery.recycler_views.interfaces;

import java.io.File;

public interface ClickListener {
    void onClick(File file, String mimeType);

    void onClick(File file);
}
