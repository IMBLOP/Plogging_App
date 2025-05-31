package com.inhatc.plogging;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;

public class StatsFragment extends Fragment {

    private Button btnRecord;
    private Button btnPlogging;
    private View layoutDistance;
    private View gridPhotos;

    private PieChart pieChartDaily, pieChartWeekly, pieChartMonthly;

    public StatsFragment() {
        // 기본 생성자
    }

    public static StatsFragment newInstance(String param1, String param2) {
        StatsFragment fragment = new StatsFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        // 뷰 초기화
        btnRecord = view.findViewById(R.id.btn_record);
        btnPlogging = view.findViewById(R.id.btn_plogging);
        layoutDistance = view.findViewById(R.id.layout_distance_stats);
        gridPhotos = view.findViewById(R.id.grid_photos);
        photoGrid = view.findViewById(R.id.photo_grid);

        pieChartDaily = view.findViewById(R.id.pie_chart_daily);
        pieChartWeekly = view.findViewById(R.id.pie_chart_weekly);
        pieChartMonthly = view.findViewById(R.id.pie_chart_monthly);

        // 초기 상태: 거리 통계 보여주기
        layoutDistance.setVisibility(View.VISIBLE);
        gridPhotos.setVisibility(View.GONE);

        // 버튼 클릭 리스너 설정
        btnRecord.setOnClickListener(v -> showDistanceLayout());
        btnPlogging.setOnClickListener(v -> showPhotoLayout());

        // 원형 그래프 초기화 및 샘플 데이터 세팅
        setupPieChart(pieChartDaily);
        setupPieChart(pieChartWeekly);
        setupPieChart(pieChartMonthly);

        setPieChartDataByValues(pieChartDaily, 5f, 3f);
        setPieChartDataByValues(pieChartWeekly, 35f, 25f);
        setPieChartDataByValues(pieChartMonthly, 150f, 120f);

        return view;
    }

    private void showDistanceLayout() {
        layoutDistance.setVisibility(View.VISIBLE);
        gridPhotos.setVisibility(View.GONE);
    }

    private GridLayout photoGrid;

    // 이미지 표시구간
    private int[] imageList = {
            R.drawable.plog1,
            R.drawable.plog2,
    };

    private void showPhotoLayout() {
        layoutDistance.setVisibility(View.GONE);
        gridPhotos.setVisibility(View.VISIBLE);
        photoGrid.removeAllViews();

        for (int resId : imageList) {
            ImageView imageView = new ImageView(getContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 600;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(resId);
            photoGrid.addView(imageView);
        }
    }


    private void setupPieChart(PieChart chart) {
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setHoleRadius(58f);
        chart.setTransparentCircleRadius(61f);
        chart.setRotationEnabled(false);
        chart.setHighlightPerTapEnabled(true);

        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setEnabled(true);

        chart.setEntryLabelColor(Color.TRANSPARENT);
        chart.setDrawEntryLabels(false);
    }

    private void setPieChartData(PieChart chart, float[] values, String[] labels) {
        List<PieEntry> entries = new ArrayList<>();
        float total = 0f;
        for (float v : values) total += v;

        for (int i = 0; i < values.length; i++) {
            entries.add(new PieEntry(values[i], labels[i]));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        int colorMoving = Color.parseColor("#954AE4"); // 보라색
        int colorGoal = Color.parseColor("#BFFD9F");   // 연두색
        dataSet.setColors(colorMoving, colorGoal);

        PieData data = new PieData(dataSet);
        data.setDrawValues(false);  // 숫자 텍스트 숨김

        chart.setData(data);

        // 예: 첫번째 값이 진행도라고 가정
        int progressPercent = Math.round(values[0] / total * 100);
        chart.setCenterText(progressPercent + "%");
        chart.setCenterTextSize(24f);
        chart.setCenterTextColor(Color.BLACK);

        chart.invalidate();
    }

    private void setPieChartDataByValues(PieChart chart, float target, float achieved) {
        float total = target > 0 ? target : achieved;
        float percentAchieved = (achieved / total) * 100f;
        float percentRemaining = 100f - percentAchieved;

        // labels 순서: 도달 거리, 목표 거리(남은 거리)
        setPieChartData(chart, new float[]{percentAchieved, percentRemaining}, new String[]{"도달 거리", "목표 거리"});
    }


}
