package com.inhatc.plogging;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface RunRecordDao {
    @Insert
    void insert(RunRecord record);

    @Query("SELECT * FROM run_records ORDER BY timestamp DESC")
    List<RunRecord> getAll();
}
