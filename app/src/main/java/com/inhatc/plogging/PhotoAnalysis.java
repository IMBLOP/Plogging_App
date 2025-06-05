package com.inhatc.plogging;

import android.net.Uri;

//촬영한 사진 데이터 클래스
public class PhotoAnalysis {
    public Uri uri;
    public String label;

    public PhotoAnalysis(Uri uri, String label) {
        this.uri = uri;
        this.label = label;
    }
}
