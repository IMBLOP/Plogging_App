package com.inhatc.plogging;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
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
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.inhatc.plogging.databinding.ActivityMapsBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
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

    private ImageButton btnToggle, btnReset, btnBack, btnCamera, btnMyLocation;
    private TextView tvTime, tvSpeend, tvDistance;
    private long elapsedTime = 0L;
    private float totalDistance = 0f;
    private float avgSpeed = 0.0f;
    private Location lastLocation = null;

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

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        btnToggle = findViewById(R.id.btn_toggle);
        btnReset = findViewById(R.id.btn_reset);
        btnMyLocation = findViewById(R.id.btn_my_location);
        tvTime = findViewById(R.id.tv_time);
        tvSpeend = findViewById(R.id.tv_speed);
        tvDistance = findViewById(R.id.tv_distance);

        btnToggle.setOnClickListener(v -> toggleRunState());
        btnReset.setOnClickListener(v -> resetTracking());

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        btnCamera = findViewById(R.id.btn_camera);
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                        if (photo != null) {
                            Uri savedUri = saveBitmapToGallery(photo);
                            if (savedUri != null) {
                                Toast.makeText(this, "사진이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "이미지 저장 실패", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "사진을 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

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
    }

    private Uri saveBitmapToGallery(Bitmap bitmap) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "plogging_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Plogging"); // 갤러리 DCIM/Plogging 폴더

        ContentResolver resolver = getContentResolver();
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (uri != null) {
            try (OutputStream out = resolver.openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                return uri;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    private void resetTracking() {
        isRunning = false;
        elapsedTime = 0L;
        lastLocation = null;
        polylinePath.clear();

        tvTime.setText("00:00.00");
        tvSpeend.setText("0.00 km/h");

        handler.removeCallbacks(timerRunnable);
        btnToggle.setBackgroundResource(R.drawable.start2);

        if (polyline != null) {
            polyline.setPoints(new ArrayList<>());
        }
    }

    private void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(cameraIntent);
        } else {
            Toast.makeText(this, "카메라 실행 불가", Toast.LENGTH_SHORT).show();
        }
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

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
    }

    private void updateMap(Location location) {
        objLocation = new LatLng(location.getLatitude(), location.getLongitude());

        if (isFirstLocation || isCameraTracking) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(objLocation, 19f));
            isFirstLocation = false;
        }

        if (lastLocation != null && isRunning) {
            float distance = lastLocation.distanceTo(location);
            totalDistance += distance;
        }
        lastLocation = location;

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
        } else {
            isRunning = false;
            handler.removeCallbacks(timerRunnable);
            btnToggle.setBackgroundResource(R.drawable.start2);
        }
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
