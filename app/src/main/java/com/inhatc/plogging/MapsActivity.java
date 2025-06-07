package com.inhatc.plogging;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.inhatc.plogging.databinding.ActivityMapsBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private LatLng objLocation;
    private boolean isRunning = false;
    private long startTime = 0L;
    private Handler handler = new Handler();
    private Runnable timerRunnable;

    private ImageButton btnToggle, btnBack, btnCamera, btnMyLocation;
    private TextView tvTime, tvSpeend, tvDistance;
    private long elapsedTime = 0L;
    private float totalDistance = 0f;
    private float avgSpeed = 0.0f;
    private Location lastLocation = null;
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private SensorEventListener stepListener;
    private int initialStepCount = -1; // 최초 걸음수 기억
    private int sessionStepCount = 0; // 세션별 걸음수
    private TextView tvStepCount; // 걸음수 표시용
    private static final float MIN_ACCURACY = 25f;    // 정확도(미터)
    private static final float MIN_DISTANCE = 3f;     // 이동 최소거리(미터)
    private static final float MAX_DISTANCE = 50f;    // 이동 최대거리(미터)

    private Detector detector;
    private Bitmap lastCapturedBitmap = null;

    private Uri photoUri; // 촬영한 이미지가 저장될 경로
    private File photoFile; // 저장 파일 객체


    private PolylineOptions polylineOptions = new PolylineOptions()
            .width(12f)
            .color(Color.parseColor("#853BF0"))
            .geodesic(true)
            .startCap(new RoundCap())
            .endCap(new RoundCap())
            .jointType(JointType.ROUND);

    private List<LatLng> polylinePath = new ArrayList<>();
    private Polyline polyline;

    private boolean isFirstLocation = true;
    private boolean isCameraTracking = true;

    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 201);
            }
        }

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        detector = new Detector(
                this,
                "best_float32.tflite",
                "labels.txt",
                new Detector.DetectorListener() {
                    @Override
                    public void onEmptyDetect() {
                        runOnUiThread(() -> showResultDialog(null, new ArrayList<>()));
                    }
                    @Override
                    public void onDetect(List<BoundingBox> boxes, long inferenceTime) {
                        List<String> labels = new ArrayList<>();
                        for (BoundingBox box : boxes) {
                            if (!labels.contains(box.getClsName())) {
                                labels.add(box.getClsName());
                            }
                        }
                        runOnUiThread(() -> showResultDialog(lastCapturedBitmap, labels));
                    }
                }
        );
        try {
            detector.setup();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "모델 로드 실패", Toast.LENGTH_SHORT).show();
        }

        btnToggle = findViewById(R.id.btn_toggle);
        btnMyLocation = findViewById(R.id.btn_my_location);
        tvTime = findViewById(R.id.tv_time);
        tvSpeend = findViewById(R.id.tv_speed);
        tvDistance = findViewById(R.id.tv_distance);

        btnToggle.setOnClickListener(v -> toggleRunState());

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (photoUri != null) {
                            try {
                                Bitmap photo = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
                                // 비트맵 크기가 너무 크면 리사이즈 (예: 640x640 유지)
                                Bitmap resized = Bitmap.createScaledBitmap(photo, 640, 640, true);
                                lastCapturedBitmap = resized;
                                detector.detect(resized);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );


        btnCamera = findViewById(R.id.btn_camera);
        btnCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 200);
            } else {
                launchCamera();
            }
        });

        btnMyLocation.setOnClickListener(v -> {
            isCameraTracking = true;
            checkAndMoveToCurrentLocation();
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        tvStepCount = findViewById(R.id.tv_step_count);

        // 센서 초기화
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        stepListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (initialStepCount == -1) {
                    initialStepCount = (int) event.values[0];
                }
                // '달리기 중'일 때만 세션 걸음수 업데이트 및 표시
                if (isRunning) {
                    sessionStepCount = (int) event.values[0] - initialStepCount;
                    tvStepCount.setText(String.valueOf(sessionStepCount));
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) { }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stepCounterSensor != null) {
            sensorManager.registerListener(stepListener, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    // onPause에서 리스너 해제
    @Override
    protected void onPause() {
        super.onPause();
        if (stepCounterSensor != null) {
            sensorManager.unregisterListener(stepListener);
        }
    }

    private void showResultDialog(Bitmap photo, List<String> labels) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_detection_result, null);
        ImageView imageView = dialogView.findViewById(R.id.result_image);
        TextView textView = dialogView.findViewById(R.id.result_labels);

        if (photo != null) imageView.setImageBitmap(photo);

        if (labels == null || labels.isEmpty()) {
            textView.setText("쓰레기를 못 찾겠어요");
        } else {
            String text = "" + android.text.TextUtils.join(", ", labels);
            textView.setText(text);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("발견한 쓰레기")
                .setView(dialogView)
                .setPositiveButton("확인", (dialog, which) -> {
                    // "확인"을 눌렀을 때만 저장
                    if (photo != null && labels != null && !labels.isEmpty()) {
                        saveDetectionResult(photo, labels);
                    }
                })
                .setNegativeButton("취소", null);  // "취소"를 누르면 아무 동작 없이 닫힘

        builder.show();
    }

    // 테스트 함수
    public Bitmap textToBitmap(String text, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(40);
        paint.setAntiAlias(true);

        canvas.drawText(text, 50, height / 2, paint);

        return bitmap;
    }

    private void saveDetectionResult(Bitmap photo, List<String> labels) {
        // 1. Bitmap -> 내부 저장소에 파일로 저장
        String fileName = "result_" + System.currentTimeMillis() + ".png";
        File file = new File(getFilesDir(), fileName);
        try (FileOutputStream out = new FileOutputStream(file)) {
            photo.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 2. Room에 저장
        DetectionResult result = new DetectionResult();
        result.imagePath = file.getAbsolutePath();
        result.labels = TextUtils.join(", ", labels);
        result.timestamp = System.currentTimeMillis();

        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "app_db").build();
            db.detectionResultDao().insert(result);
        }).start();
    }



    private void resetTracking() {
        elapsedTime = 0L;
        lastLocation = null;
        totalDistance = 0f;
        avgSpeed = 0f;
        initialStepCount = -1;
        sessionStepCount = 0;

        polylinePath.clear();
        tvTime.setText("00:00.00");
        tvSpeend.setText("0.00 km/h");
        tvDistance.setText("0.00 km");
        tvStepCount.setText("0");

        if (polyline != null) {
            polyline.setPoints(new ArrayList<>());
        }
    }


    private void launchCamera() {
        try {
            // 임시 이미지 파일 생성
            photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".provider",
                        photoFile
                );
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                cameraLauncher.launch(cameraIntent);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            Toast.makeText(this, "이미지 파일 생성 실패", Toast.LENGTH_SHORT).show();
        }
    }

    // 임시 저장 파일 생성 함수
    private File createImageFile() throws IOException {
        String fileName = "plogging_photo_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(fileName, ".jpg", storageDir);
    }


    private void checkAndMoveToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (location != null) {
            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 19f));
        } else {
            Toast.makeText(this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                isCameraTracking = false;
            }
        });

        polyline = mMap.addPolyline(polylineOptions);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateMap(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                alertStatus(provider);
            }

            @Override
            public void onProviderEnabled(String provider) {
                alertProvider(provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                if (LocationManager.NETWORK_PROVIDER.equals(provider)) return;
                checkProvider(provider);
            }
        };

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 5, locationListener);
    }

    private void updateMap(Location location) {
        // 1. 정확도 기준 필터링
        if (location.getAccuracy() > MIN_ACCURACY) {
            return; // 정확도가 너무 낮으면 무시
        }

        // 2. 거리 변화 기준 필터링
        if (lastLocation != null) {
            float distance = lastLocation.distanceTo(location);

            if (distance < MIN_DISTANCE) {
                return; // GPS 미세 흔들림(노이즈) 무시
            }

            if (distance > MAX_DISTANCE) {
                return; // 튀는 값 무시 (예: 지하철, 터널, 일시적 GPS 오류 등)
            }
            totalDistance += distance;
        }
        lastLocation = location;

        objLocation = new LatLng(location.getLatitude(), location.getLongitude());

        if (isFirstLocation || isCameraTracking) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(objLocation, 19f));
            isFirstLocation = false;
        }

        if (isRunning) {
            avgSpeed = (elapsedTime > 0) ? (totalDistance / (elapsedTime / 1000f)) * 3.6f : 0f;
            polylinePath.add(objLocation);
            polyline.setPoints(polylinePath);
        } else {
            avgSpeed = 0f;
        }

        tvSpeend.setText(String.format(Locale.getDefault(), "%.2f km/h", avgSpeed));
        tvDistance.setText(String.format(Locale.getDefault(), "%.2f km", totalDistance / 1000f));
    }


    private void checkProvider(String provider) {
        Toast.makeText(this, provider + ": 위치 서비스가 꺼져 있습니다.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    private void alertProvider(String provider) {
        Toast.makeText(this, provider + " 위치 서비스가 시작되었습니다.", Toast.LENGTH_LONG).show();
    }

    private void alertStatus(String provider) {
        Toast.makeText(this, "위치 서비스 상태 변경: " + provider, Toast.LENGTH_LONG).show();
    }

    private void toggleRunState() {
        if (!isRunning) {
            new AlertDialog.Builder(this)
                    .setTitle("Start Running!")
                    .setMessage("충분히 몸을 풀고\n달리기를 시작하세요!")
                    .setPositiveButton("시작", (dialog, which) -> {
                        startRunning();
                    })
                    .setNegativeButton("취소", null)
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Stop Running")
                    .setMessage("달리기를 종료할까요?\n기록이 저장됩니다.")
                    .setPositiveButton("종료", (dialog, which) -> {
                        stopRunningAndSave();
                    })
                    .setNegativeButton("계속", null)
                    .show();
        }
    }

    private void startRunning() {
        isRunning = true;
        startTime = System.currentTimeMillis() - elapsedTime;
        btnToggle.setBackgroundResource(R.drawable.stop2);
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                elapsedTime = System.currentTimeMillis() - startTime;

                int minutes = (int) (elapsedTime / 1000) / 60;
                int seconds = (int) (elapsedTime / 1000) % 60;
                int hundredths = (int) (elapsedTime % 1000) / 10;

                String time = String.format(Locale.getDefault(), "%02d:%02d.%02d", minutes, seconds, hundredths);
                tvTime.setText(time);

                handler.postDelayed(this, 10);
            }
        };
        handler.post(timerRunnable);
    }

    private void stopRunningAndSave() {
        isRunning = false;
        handler.removeCallbacks(timerRunnable);
        btnToggle.setBackgroundResource(R.drawable.start2);

        if (polylinePath.size() > 1) {
            saveRouteImage(polylinePath, mMap, new SaveImageCallback() {
                @Override
                public void onSave(String imagePath) {
                    if (imagePath != null) {
                        saveRunRecord(imagePath);
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(MapsActivity.this, "경로 이미지 저장 실패, 기록이 저장되지 않습니다.", Toast.LENGTH_SHORT).show()
                        );
                    }
                    runOnUiThread(() -> resetTracking());
                }
            });
        } else {
            Toast.makeText(this, "이동한 경로가 없어 기록이 저장되지 않습니다.", Toast.LENGTH_SHORT).show();
            resetTracking();
        }
    }


    private void saveRouteImage(List<LatLng> polylinePath, GoogleMap mMap, SaveImageCallback callback) {
        if (polylinePath == null || polylinePath.isEmpty() || mMap == null) {
            callback.onSave(null);
            return;
        }

        // 1. 전체 경로가 들어가도록 LatLngBounds 구함
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : polylinePath) {
            builder.include(latLng);
        }
        LatLngBounds bounds = builder.build();

        // 2. 카메라를 전체 경로가 다 나오도록 이동 (패딩 100px)
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

        // 3. 지도 캡처 (snapshot은 비동기이므로 콜백)
        mMap.snapshot(bitmap -> {
            if (bitmap == null) {
                callback.onSave(null);
                return;
            }
            // 4. 비트맵을 파일로 저장
            String fileName = "route_" + System.currentTimeMillis() + ".png";
            File file = new File(getFilesDir(), fileName);
            try (FileOutputStream out = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                Log.d("MapsActivity", "경로 이미지 저장됨: " + file.getAbsolutePath());
                callback.onSave(file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                callback.onSave(null);
            }
        });
    }

    // 이미지 저장 후 결과를 받기 위한 콜백 인터페이스
    public interface SaveImageCallback {
        void onSave(String imagePath);
    }

    private void saveRunRecord(String imagePath) {
        RunRecord record = new RunRecord();
        record.routeImagePath = imagePath;
        record.steps = sessionStepCount;
        record.avgSpeed = avgSpeed;
        record.calories = calculateCalories(totalDistance, elapsedTime);
        record.distance = totalDistance;
        record.duration = elapsedTime;
        record.timestamp = System.currentTimeMillis();

        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "app_db").build();
            db.runRecordDao().insert(record);
            Log.d("MapsActivity", "RunRecord 저장 완료");
        }).start();
    }

    private float calculateCalories(float distanceMeters, long durationMs) {
        float weightKg = SharedPrefUtils.getWeight(this); // 프로필에서 읽어옴
        float met = 8.0f; // MET 값(달리기 8~10, 빠른 걷기 4~5)
        float hours = durationMs / 1000f / 3600f;
        return met * weightKg * hours;
    }

    private Uri saveBitmapAndGetUri(Bitmap bitmap) {
        File folder = getExternalFilesDir("pictures");
        if (folder == null) return null;
        if (!folder.exists()) folder.mkdirs();

        String filename = "plogging_" + System.currentTimeMillis() + ".png";
        File file = new File(folder, filename);

        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            return Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 300);
            }
        }

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) onMapReady(mMap);
            } else {
                Toast.makeText(this, "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 200) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(this, "카메라 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
