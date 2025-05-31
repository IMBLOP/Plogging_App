package com.inhatc.plogging;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.inhatc.plogging.databinding.ActivityMapsBinding;

import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private LatLng objLocation;
    private boolean isRunning = false;
    private long startTime = 0L;
    private Handler handler = new Handler();
    private Runnable timerRunnable;

    private ImageButton btnToggle, btnReset, btnBack, btnCamera;
    private TextView tvTime;
    private long pausedTime = 0L;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        btnToggle = findViewById(R.id.btn_toggle);
        btnReset = findViewById(R.id.btn_reset);
        tvTime = findViewById(R.id.tv_time);

        btnToggle.setOnClickListener(v -> toggleRunState());

        btnReset.setOnClickListener(v -> {
            isRunning = false;
            pausedTime = 0L;
            tvTime.setText("00:00.00");
            handler.removeCallbacks(timerRunnable);
            btnToggle.setBackgroundResource(R.drawable.start);
        });

        // 뒤로가기 버튼
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        btnCamera = findViewById(R.id.btn_camera);
        btnCamera.setOnClickListener(v -> {
            // 권한 체크
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, 200);
                return;
            }

            // 카메라 인텐트 실행
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(cameraIntent);
            } else {
                Toast.makeText(this, "카메라 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        long minTime = 1000;
        float minDistance = 1;

        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location){
                mUpdateMap(location);
            }
            public void onStatusChanged(String provider, int status, Bundle extras){
                mAlertStatus(provider);
            }
            public void onProviderEnabled(String provider){
                mAlertProvider(provider);
            }
            public void onProviderDisabled(String provider){
                if (LocationManager.NETWORK_PROVIDER.equals(provider)) {
                    return;
                }
                mCheckProvider(provider);
            }
        };
        LocationManager locationManager;
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }
        String strLocationProvider = LocationManager.GPS_PROVIDER;
        locationManager.requestLocationUpdates(strLocationProvider, minTime, minDistance, locationListener);
        strLocationProvider = LocationManager.NETWORK_PROVIDER;
        locationManager.requestLocationUpdates(strLocationProvider, minTime, minDistance, locationListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // 기본 뒤로가기 동작 수행
    }

    public void mUpdateMap(Location location){
        double dLatitude = location.getLatitude();
        double dLongitude = location.getLongitude();
        objLocation = new LatLng(dLatitude, dLongitude);

        Marker objMK = mMap.addMarker(new MarkerOptions().position(objLocation).title("Current Position"));

        objMK.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(objLocation, 15f));
    }
    public void mCheckProvider(String strProvider){
        Toast.makeText(this, strProvider + ": Location service turn off ..." + "Please Turn on location service ...", Toast.LENGTH_SHORT).show();
        Intent objIntent;
        objIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(objIntent);
    }
    public void mAlertProvider(String strProvider){
        Toast.makeText(this, strProvider + "Starting location service!", Toast.LENGTH_LONG).show();
    }
    public void mAlertStatus(String strProvider){
        Toast.makeText(this, "Changing location service: " + strProvider, Toast.LENGTH_LONG).show();
    }


    private void toggleRunState() {
        if (!isRunning) {
            // 시작
            isRunning = true;
            startTime = System.currentTimeMillis() - pausedTime; // 일시 정지 시간 고려
            btnToggle.setBackgroundResource(R.drawable.stop);

            timerRunnable = new Runnable() {
                @Override
                public void run() {
                    long elapsed = System.currentTimeMillis() - startTime;
                    pausedTime = elapsed;

                    int minutes = (int) (elapsed / 1000) / 60;
                    int seconds = (int) (elapsed / 1000) % 60;
                    int hundredths = (int) (elapsed % 1000) / 10; // 1/100초 단위

                    String time = String.format(Locale.getDefault(), "%02d:%02d.%02d", minutes, seconds, hundredths);
                    tvTime.setText(time);

                    handler.postDelayed(this, 10); // 10ms 단위로 갱신
                }
            };
            handler.post(timerRunnable);

        } else {
            // 정지 (시간 유지)
            isRunning = false;
            handler.removeCallbacks(timerRunnable);
            btnToggle.setBackgroundResource(R.drawable.start);
        }
    }


}