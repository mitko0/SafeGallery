package com.example.safegallery.tabs.data.intefaces;

import com.example.safegallery.tabs.data.DataPath;

import java.util.List;
import java.util.Map;

public interface DataSetter {
    void setDataMaps(Map<String, List<DataPath>> data);
}
