package com.inhatc.plogging;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private LinearLayout layoutActivity;
    private LinearLayout layoutNutrition;
    private Button btnActivity;
    private Button btnNutrition;
    private LinearLayout cardBreakfast, cardLunch, cardDinner;
    private TextView tvBreakfastCal, tvLunchCal, tvDinnerCal;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        BarChart barChart = view.findViewById(R.id.bar_chart);

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 2300)); // 월
        entries.add(new BarEntry(1, 1750)); // 화
        entries.add(new BarEntry(2, 0)); // 수
        entries.add(new BarEntry(3, 0)); // 목
        entries.add(new BarEntry(4, 0)); // 금
        entries.add(new BarEntry(5, 0)); // 토
        entries.add(new BarEntry(6, 0)); // 일

        BarDataSet dataSet = new BarDataSet(entries, "소비 칼로리 (kcal)");
        dataSet.setColor(getResources().getColor(R.color.purple_500, null));
        dataSet.setColor(Color.argb(128, 103, 58, 183));
        dataSet.setValueTextSize(12f);
        dataSet.setDrawValues(false);
        BarData data = new BarData(dataSet);  // 여기서 선언 및 초기화
        String[] days = new String[]{"월", "화", "수", "목", "금", "토", "일"};
        dataSet.setValueTextColor(Color.DKGRAY);
        dataSet.setValueFormatter(new LargeValueFormatter()); // 큰 수치 깔끔하게 표시


        barChart.setData(data);
        barChart.getBarData().setBarWidth(0.6f); // 막대 폭 조절

        // X축 설정
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.DKGRAY);
        xAxis.setTextSize(14f);
        xAxis.setLabelCount(days.length);
        xAxis.setAvoidFirstLastClipping(true);

        // Y 축 설정
        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisLeft().setGridColor(Color.LTGRAY);
        barChart.getAxisLeft().setTextColor(Color.DKGRAY);
        barChart.getAxisLeft().setTextSize(14f);
        barChart.getAxisLeft().setAxisMinimum(0f); // 0부터 시작

        barChart.getAxisRight().setEnabled(false);

        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        barChart.animateY(1200);
        barChart.setDrawGridBackground(false);
        barChart.setExtraBottomOffset(10f);
        barChart.invalidate();


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutActivity = view.findViewById(R.id.layout_activity);
        layoutNutrition = view.findViewById(R.id.layout_nutrition);
        btnActivity = view.findViewById(R.id.btn_activity);
        btnNutrition = view.findViewById(R.id.btn_nutrition);

        cardBreakfast = view.findViewById(R.id.card_breakfast);
        cardLunch = view.findViewById(R.id.card_lunch);
        cardDinner = view.findViewById(R.id.card_dinner);

        tvBreakfastCal = view.findViewById(R.id.tv_breakfast_cal);
        tvLunchCal = view.findViewById(R.id.tv_lunch_cal);
        tvDinnerCal = view.findViewById(R.id.tv_dinner_cal);

        // 버튼 토글
        btnActivity.setOnClickListener(v -> {
            layoutActivity.setVisibility(View.VISIBLE);
            layoutNutrition.setVisibility(View.GONE);
        });

        btnNutrition.setOnClickListener(v -> {
            layoutActivity.setVisibility(View.GONE);
            layoutNutrition.setVisibility(View.VISIBLE);
        });

        // 카드 접기/펼치기 기본은 펼친 상태
        cardBreakfast.setOnClickListener(v -> toggleCard(cardBreakfast, tvBreakfastCal));
        cardLunch.setOnClickListener(v -> toggleCard(cardLunch, tvLunchCal));
        cardDinner.setOnClickListener(v -> toggleCard(cardDinner, tvDinnerCal));

        // 초기 화면은 활동
        layoutActivity.setVisibility(View.VISIBLE);
        layoutNutrition.setVisibility(View.GONE);
    }

    private void toggleCard(LinearLayout card, TextView calText) {
        if (calText.getVisibility() == View.VISIBLE) {
            calText.setVisibility(View.GONE);
        } else {
            calText.setVisibility(View.VISIBLE);
        }
    }
}
