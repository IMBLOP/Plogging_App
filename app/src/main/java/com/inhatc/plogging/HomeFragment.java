package com.inhatc.plogging;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
    private TextView tvUserName, tvUserInfo;
    private LinearLayout profileLayout;

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

        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserInfo = view.findViewById(R.id.tv_user_info);
        profileLayout = view.findViewById(R.id.profile_layout);

        updateProfileViews();

        profileLayout.setOnClickListener(v -> showProfileEditDialog());

        BarChart barChart = view.findViewById(R.id.bar_chart);
        LinearLayout profileLayout = view.findViewById(R.id.profile_layout); // or img_user 등
        profileLayout.setOnClickListener(v -> showProfileEditDialog());

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 2300)); // 월
        entries.add(new BarEntry(1, 1750)); // 화
        entries.add(new BarEntry(2, 0)); // 수
        entries.add(new BarEntry(3, 0)); // 목
        entries.add(new BarEntry(4, 0)); // 금
        entries.add(new BarEntry(5, 0)); // 토
        entries.add(new BarEntry(6, 0)); // 일

        BarDataSet dataSet = new BarDataSet(entries, "소비 칼로리 (kcal)");
        dataSet.setColor(getResources().getColor(R.color.green_500, null));
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

    private void showProfileEditDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile, null);
        EditText etName = dialogView.findViewById(R.id.et_name);
        EditText etHeight = dialogView.findViewById(R.id.et_height);
        EditText etWeight = dialogView.findViewById(R.id.et_weight);

        // 현재 저장된 값 세팅
        etName.setText(SharedPrefUtils.getName(getContext()));
        etHeight.setText(String.valueOf(SharedPrefUtils.getHeight(getContext())));
        etWeight.setText(String.valueOf(SharedPrefUtils.getWeight(getContext())));

        new AlertDialog.Builder(getContext())
                .setTitle("프로필 수정")
                .setView(dialogView)
                .setPositiveButton("저장", (dialog, which) -> {
                    String name = etName.getText().toString();
                    float height = parseFloat(etHeight.getText().toString(), 170f);
                    float weight = parseFloat(etWeight.getText().toString(), 65f);

                    SharedPrefUtils.setName(getContext(), name);
                    SharedPrefUtils.setHeight(getContext(), height);
                    SharedPrefUtils.setWeight(getContext(), weight);

                    updateProfileViews(); // 뷰 갱신
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private float parseFloat(String s, float defaultVal) {
        try { return Float.parseFloat(s); }
        catch (Exception e) { return defaultVal; }
    }

    // SharedPreferences 값 → 화면 표시 함수
    private void updateProfileViews() {
        if(tvUserName != null && tvUserInfo != null) {
            String name = SharedPrefUtils.getName(getContext());
            float height = SharedPrefUtils.getHeight(getContext());
            float weight = SharedPrefUtils.getWeight(getContext());
            tvUserName.setText(name);
            tvUserInfo.setText("키 " + height + "cm  몸무게 " + weight + "kg");
        }
    }

}
