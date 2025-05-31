package com.inhatc.plogging;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

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
}