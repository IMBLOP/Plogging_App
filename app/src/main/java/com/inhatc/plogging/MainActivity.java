package com.inhatc.plogging;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;
    private Uri photoUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);

        // 앱 실행 시 첫 화면
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();

        // 메뉴 클릭 시 Fragment 변경
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            String tag = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selected = new HomeFragment();
            } else if (itemId == R.id.nav_stats) {
                selected = new StatsFragment();
                tag = "stats_fragment_tag";
            } else if (itemId == R.id.nav_history) {
                selected = new HistoryFragment();
            } else if (itemId == R.id.nav_info) {
                selected = new InfoFragment();
            }
            if (selected != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selected, tag)
                        .commit();
            }
            return true;
        });


        // Google Map 여기 삽입
        FloatingActionButton fab = findViewById(R.id.btn_start_workout);
        fab.setOnClickListener(v -> {
            Log.d("FAB_Click", "운동 시작 버튼 클릭됨");
            Log.d("FAB_Click", "구글 맵 실행부분");
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        });

    }


}
