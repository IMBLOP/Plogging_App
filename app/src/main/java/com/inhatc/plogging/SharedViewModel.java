package com.inhatc.plogging;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<List<Uri>> imageUris = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Uri>> getImageUris() {
        return imageUris;
    }

    public void addImageUri(Uri uri) {
        List<Uri> currentList = imageUris.getValue();
        if (currentList != null) {
            currentList.add(uri);
            imageUris.setValue(currentList);
        }
    }

    // 일정 Map 저장용
    private final MutableLiveData<Map<String, List<String>>> eventMapLiveData = new MutableLiveData<>(new HashMap<>());

    public LiveData<Map<String, List<String>>> getEventMap() {
        return eventMapLiveData;
    }

    public void addEvent(String date, String event) {
        Map<String, List<String>> currentMap = eventMapLiveData.getValue();
        if (currentMap == null) {
            currentMap = new HashMap<>();
        }

        List<String> events = new ArrayList<>(currentMap.getOrDefault(date, new ArrayList<>()));
        if (events.contains("일정 없음")) events.remove("일정 없음");
        events.add(event);
        currentMap.put(date, events);
        eventMapLiveData.setValue(currentMap);
    }
}
