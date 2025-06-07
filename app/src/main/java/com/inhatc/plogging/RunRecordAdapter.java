package com.inhatc.plogging;

import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Date;

public class RunRecordAdapter extends RecyclerView.Adapter<RunRecordAdapter.RunViewHolder> {
    private List<RunRecord> runRecords;

    public RunRecordAdapter(List<RunRecord> records) {
        this.runRecords = records;
    }

    public void setRunRecords(List<RunRecord> records) {
        this.runRecords = records;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RunViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_run_record, parent, false);
        return new RunViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RunViewHolder holder, int position) {
        RunRecord record = runRecords.get(position);

        // 경로 이미지
        if (record.routeImagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(record.routeImagePath);
            if (bitmap != null) holder.imgRoute.setImageBitmap(bitmap);
        }
        // 날짜
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(record.timestamp)));
        // 거리
        holder.tvDistance.setText(String.format(Locale.getDefault(), "거리: %.2fkm", record.distance / 1000f));
        // 시간
        long min = record.duration / 1000 / 60;
        long sec = (record.duration / 1000) % 60;
        holder.tvDuration.setText(String.format(Locale.getDefault(), "시간: %02d:%02d", min, sec));
        // 속도
        holder.tvSpeed.setText(String.format(Locale.getDefault(), "평균 속도: %.1fkm/h", record.avgSpeed));
        // 칼로리
        holder.tvCalories.setText(String.format(Locale.getDefault(), "칼로리: %.0f kcal", record.calories));
    }

    @Override
    public int getItemCount() {
        return runRecords != null ? runRecords.size() : 0;
    }

    static class RunViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRoute;
        TextView tvDate, tvDistance, tvDuration, tvSpeed, tvCalories;

        RunViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRoute = itemView.findViewById(R.id.img_route);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvDistance = itemView.findViewById(R.id.tv_distance);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvSpeed = itemView.findViewById(R.id.tv_speed);
            tvCalories = itemView.findViewById(R.id.tv_calories);
        }
    }
}
