package com.example.safegallery.tabs.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataPath {

    private String path;
    private String mimeType;
    private byte[] data;

    public DataPath (String path, String mimeType) {
        this.path = path;
        this.mimeType = mimeType;
    }
}
