package com.inhatc.plogging;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "run_records")
public class RunRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String routeImagePath;
    public int steps;
    public float avgSpeed;
    public float calories;
    public float distance;
    public long duration; // ms
    public long timestamp; // 기록 시각
}
