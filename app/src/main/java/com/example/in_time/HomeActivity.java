package com.example.in_time;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 스플래시 화면 설정
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 지도 초기화
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.smf_map);
        mapFragment.getMapAsync(this);

        Places.initialize(getApplicationContext(), BuildConfig.google_api_key);

        // 자동완성 Fragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.asf_search);
        autocompleteFragment.getView().setBackgroundResource(R.drawable.bg_round);
        autocompleteFragment.setTypeFilter(TypeFilter.ESTABLISHMENT);
        autocompleteFragment.setCountries("KR");
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                String placeName = place.getName();
                String placeAddress = place.getAddress();
                LatLng placeLatLng = place.getLatLng();

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title(placeName);
                markerOptions.snippet(placeAddress);
                markerOptions.position(placeLatLng);
                map.addMarker(markerOptions);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, 15));
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        // 버튼 클릭 시 AlertDialog 생성
        Button btn_plus = findViewById(R.id.btn_plus);
        btn_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 다이얼로그 생성
                createAlertDialog();
            }
        });
    }

    // 지도가 준비되면 호출
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        // 위치 권한을 가지고 있는지 체크
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // 현재 위치 표시 활성화
            map.setMyLocationEnabled(true);

            // 현재 위치로 카메라 이동
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                            }
                        }
                    });
        } else {
            // 권한이 없는 경우 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    // 권한에 대해 응답하면 호출
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 권한이 승인되면 다시 지도 초기화
            mapFragment.getMapAsync(this);
        } else {
            // 권한을 거부 시 메시지 전달
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();

            // 설정으로 이동하는 Intent 생성
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));

            // 설정 화면으로 이동
            startActivity(intent);

            // 앱 종료
            finish();
        }
    }

    // 다이얼로그 생성
    private void createAlertDialog() {
        // AlertDialog를 생성
        AlertDialog.Builder ad = new AlertDialog.Builder(HomeActivity.this);
        ad.setTitle("카테고리 추가");

        final EditText et = new EditText(HomeActivity.this);
        et.setSingleLine(true);

        // 다이얼로그에 EditText 생성
        ad.setView(et);

        // 다이얼로그의 "확인" 버튼을 눌렀을 때
        ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                // 다이얼로그에서 입력된 값 가져오기
                String inputEt = et.getText().toString();

                // 입력 값으로 버튼 생성
                createButtonWithInput(inputEt);
            }
        });

        // 다이얼로그의 "취소" 버튼을 눌렀을 때 동작 정의
        ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 다이얼로그 닫기
                dialogInterface.cancel();
            }
        });

        // AlertDialog를 생성하고 보여주기
        AlertDialog alertDialog = ad.create();
        alertDialog.show();
    }

    // 버튼 생성
    private void createButtonWithInput(String inputEt) {
        LinearLayout titleList = findViewById(R.id.ll_wrapper_footer_title_list);

        Button button = new Button(this);
        button.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        button.setText((inputEt));

        // 리스트에 버튼 추가
        titleList.addView(button);

        // 생성된 버튼에 클릭 이벤트 추가
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼 클릭하면 마커 목록 표시하기

//                return true;
            }
        });

        // 버튼을 길게 누르면 버튼 삭제
        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // AlertDialog를 생성
                AlertDialog.Builder ad = new AlertDialog.Builder(HomeActivity.this);
                ad.setTitle("카테고리 삭제");

                // 다이얼로그의 "확인" 버튼을 눌렀을 때
                ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        // 버튼 삭제
                        titleList.removeView(button);
                        dialogInterface.dismiss();
                    }
                });

                // 다이얼로그의 "취소" 버튼을 눌렀을 때 동작 정의
                ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 다이얼로그 닫기
                        dialogInterface.cancel();
                    }
                });

                // AlertDialog를 생성하고 보여주기
                AlertDialog alertDialog = ad.create();
                alertDialog.show();


                // 데이터도 삭제하기

                return true;
            }
        });
    }
}