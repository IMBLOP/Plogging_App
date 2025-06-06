package com.inhatc.plogging;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DetectionResult.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DetectionResultDao detectionResultDao();
}