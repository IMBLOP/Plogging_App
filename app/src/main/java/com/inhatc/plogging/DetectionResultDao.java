package com.inhatc.plogging;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DetectionResultDao {
    @Insert
    void insert(DetectionResult result);

    @Query("SELECT * FROM detection_results ORDER BY timestamp DESC")
    List<DetectionResult> getAll();

    @Query("DELETE FROM detection_results WHERE id = :id")
    void deleteById(int id);
}

