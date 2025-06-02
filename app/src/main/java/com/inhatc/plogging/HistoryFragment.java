package com.inhatc.plogging;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import com.inhatc.plogging.SharedViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryFragment extends Fragment {

    private SharedViewModel viewModel;
    private Button btnAddEvent;
    private String selectedDate = null; // 원래 배열로 돼 있었는데 일반 변수로 교체
    private ListView listEvents;
    private TextView tvSelectedDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        CalendarView calendarView = view.findViewById(R.id.calendar_view);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        listEvents = view.findViewById(R.id.list_events);
        btnAddEvent = view.findViewById(R.id.btn_add_event);


        viewModel.getEventMap().observe(getViewLifecycleOwner(), updatedMap -> {
            if (selectedDate != null) {
                List<String> events = updatedMap.getOrDefault(selectedDate, Collections.singletonList("일정 없음"));
                listEvents.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, events));
            }
        });

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            tvSelectedDate.setText("선택된 날짜: " + selectedDate);

            Map<String, List<String>> currentMap = viewModel.getEventMap().getValue();
            List<String> events = currentMap != null ? currentMap.getOrDefault(selectedDate, Collections.singletonList("일정 없음"))
                    : Collections.singletonList("일정 없음");

            listEvents.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, events));
        });

        btnAddEvent.setOnClickListener(v -> {
            if (selectedDate == null) {
                new AlertDialog.Builder(getContext())
                        .setMessage("먼저 날짜를 선택하세요.")
                        .setPositiveButton("확인", null)
                        .show();
                return;
            }

            EditText input = new EditText(getContext());
            input.setHint("예: 플로깅 30분");

            new AlertDialog.Builder(getContext())
                    .setTitle("일정 추가")
                    .setMessage(selectedDate + "에 추가할 일정을 입력하세요.")
                    .setView(input)
                    .setPositiveButton("추가", (dialog, which) -> {
                        String newEvent = input.getText().toString().trim();
                        if (!newEvent.isEmpty()) {
                            viewModel.addEvent(selectedDate, newEvent); // ✅ ViewModel로 저장
                        }
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        return view;
    }

}