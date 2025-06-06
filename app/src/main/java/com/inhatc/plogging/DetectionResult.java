package com.inhatc.plogging;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "detection_results")
public class DetectionResult {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String imagePath; // 내부 저장소 경로
    public String labels;    // 감지된 라벨 문자열 (쉼표 구분)
    public long timestamp;   // 저장 시각
}
