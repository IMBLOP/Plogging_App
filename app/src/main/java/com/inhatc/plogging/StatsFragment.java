package com.inhatc.plogging;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class StatsFragment extends Fragment {

    private Button btnRecord, btnPlogging;
    private LinearLayout layoutRunRecords, gridPhotos;
    private GridLayout photoGrid;
    private RecyclerView rvRunRecords;
    private RunRecordAdapter runAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        btnRecord = view.findViewById(R.id.btn_record);
        btnPlogging = view.findViewById(R.id.btn_plogging);
        layoutRunRecords = view.findViewById(R.id.layout_run_records); // RecyclerView 포함 LinearLayout
        gridPhotos = view.findViewById(R.id.grid_photos);
        photoGrid = view.findViewById(R.id.photo_grid);
        rvRunRecords = view.findViewById(R.id.rv_run_records);
        runAdapter = new RunRecordAdapter(new ArrayList<>());
        rvRunRecords.setAdapter(runAdapter);

        // 초기화: 기록 리스트 보이기, 플로깅 그리드는 숨김
        layoutRunRecords.setVisibility(View.VISIBLE);
        gridPhotos.setVisibility(View.GONE);

        // RecyclerView 세팅
        rvRunRecords.setLayoutManager(new LinearLayoutManager(getContext()));
        runAdapter = new RunRecordAdapter(new ArrayList<>());
        rvRunRecords.setAdapter(runAdapter);

        // "기록" 버튼 → 기록(RecyclerView) 보이기
        btnRecord.setOnClickListener(v -> {
            layoutRunRecords.setVisibility(View.VISIBLE);
            gridPhotos.setVisibility(View.GONE);
            loadRunRecords();
        });

        // "플로깅" 버튼 → 플로깅(사진 그리드) 보이기
        btnPlogging.setOnClickListener(v -> {
            layoutRunRecords.setVisibility(View.GONE);
            gridPhotos.setVisibility(View.VISIBLE);
            showPhotoLayout();
        });

        runAdapter.setOnItemLongClickListener((record, pos) -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("삭제 확인")
                    .setMessage("이 조깅 기록을 삭제할까요?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        new Thread(() -> {
                            AppDatabase db = Room.databaseBuilder(
                                    requireContext().getApplicationContext(),
                                    AppDatabase.class, "app_db"
                            ).build();
                            db.runRecordDao().deleteById(record.id);

                            // 경로 이미지 파일도 삭제
                            File imgFile = new File(record.routeImagePath);
                            if (imgFile.exists()) imgFile.delete();

                            requireActivity().runOnUiThread(() -> {
                                // adapter에서 바로 삭제(새로고침)
                                runAdapter.removeAt(pos); // removeAt 메서드 구현 필요
                            });
                        }).start();
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        // 시작 시 기록 리스트 로드
        loadRunRecords();

        return view;
    }

    private void loadRunRecords() {
        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(requireContext().getApplicationContext(),
                    AppDatabase.class, "app_db").build();
            List<RunRecord> runList = db.runRecordDao().getAll();
            requireActivity().runOnUiThread(() -> runAdapter.setRunRecords(runList));
        }).start();
    }

    private void showPhotoLayout() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        photoGrid.removeAllViews();

        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(
                    requireContext().getApplicationContext(),
                    AppDatabase.class, "app_db"
            ).build();
            List<DetectionResult> results = db.detectionResultDao().getAll();

            requireActivity().runOnUiThread(() -> {
                for (DetectionResult result : results) {
                    LinearLayout itemLayout = new LinearLayout(getContext());
                    itemLayout.setOrientation(LinearLayout.VERTICAL);

                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = 0;
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                    itemLayout.setLayoutParams(params);

                    ImageView imageView = new ImageView(getContext());
                    imageView.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 600
                    ));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    Bitmap bitmap = BitmapFactory.decodeFile(result.imagePath);
                    if (bitmap != null) imageView.setImageBitmap(bitmap);

                    itemLayout.setOnLongClickListener(v -> {
                        new AlertDialog.Builder(getContext())
                                .setTitle("삭제 확인")
                                .setMessage("이 사진 기록을 삭제할까요?")
                                .setPositiveButton("삭제", (dialog, which) -> {
                                    new Thread(() -> {
                                        db.detectionResultDao().deleteById(result.id);
                                        File imgFile = new File(result.imagePath);
                                        if (imgFile.exists()) imgFile.delete();
                                        requireActivity().runOnUiThread(this::showPhotoLayout);
                                    }).start();
                                })
                                .setNegativeButton("취소", null)
                                .show();
                        return true;
                    });

                    TextView textView = new TextView(getContext());
                    textView.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    ));
                    String formattedTime = sdf.format(new Date(result.timestamp));
                    String labelText = "감지 결과: " + result.labels + "\n촬영시간: " + formattedTime;
                    textView.setText(labelText);
                    textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    textView.setTextColor(Color.BLACK);
                    textView.setTextSize(16);

                    itemLayout.addView(imageView);
                    itemLayout.addView(textView);
                    photoGrid.addView(itemLayout);
                }
            });
        }).start();
    }
}
