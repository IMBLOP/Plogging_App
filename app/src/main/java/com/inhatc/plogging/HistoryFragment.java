package com.inhatc.plogging;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        CalendarView calendarView = view.findViewById(R.id.calendar_view);
        TextView tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        ListView listEvents = view.findViewById(R.id.list_events);

        // 임시 일정 데이터
        Map<String, List<String>> eventMap = new HashMap<>();
        eventMap.put("2025-06-13", Arrays.asList("플로깅 30분", "스트레칭", "물 1.5L"));
        eventMap.put("2025-06-14", Arrays.asList("걷기 7,000보", "근력 운동", "물 2L"));
        eventMap.put("2025-06-15", Arrays.asList("휴식일", "요가 20분", "과일 섭취"));
        eventMap.put("2025-06-16", Arrays.asList("플로깅 1시간", "걷기 5,000보", "물 1.5L"));
        eventMap.put("2025-06-17", Arrays.asList("스트레칭", "코어 운동", "단백질 보충"));
        eventMap.put("2025-06-18", Arrays.asList("걷기 6,000보", "계단 오르기", "물 2L"));
        eventMap.put("2025-06-19", Arrays.asList("플로깅 45분", "스트레칭", "야채 섭취"));
        eventMap.put("2025-06-20", Arrays.asList("요가 30분", "걷기 8,000보", "물 1.8L"));
        eventMap.put("2025-06-21", Arrays.asList("플로깅 1시간", "근력 운동", "과일 주스 1잔"));


        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String dateStr = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            tvSelectedDate.setText("선택된 날짜: " + dateStr);

            List<String> events = eventMap.getOrDefault(dateStr, Collections.singletonList("일정 없음"));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    events
            );
            listEvents.setAdapter(adapter);
        });

        return view;
    }
}