package com.example.myrecycleplication;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.KakaoMapSdk;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.camera.CameraAnimation;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelLayerOptions;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.route.RouteLine;
import com.kakao.vectormap.route.RouteLineManager;
import com.kakao.vectormap.route.RouteLineOptions;
import com.kakao.vectormap.route.RouteLinePattern;
import com.kakao.vectormap.route.RouteLineSegment;
import com.kakao.vectormap.route.RouteLineStyle;
import com.kakao.vectormap.route.RouteLineStyles;
import com.kakao.vectormap.route.RouteLineStylesSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity implements LocationAccess.PermissionListener {

    private MapView mapView;    // 지도 화면 (findViewById)
    private KakaoMap kakaoMap;  // 카메라 이동, 마커조작 등
    private Label currentLocationLabel; // 마커(라벨)
    private boolean isInitialLocationSet = false; // 앱 실행 후 내 위치 떳는지 확인
    private LocationAccess locationAccess;  // 위치 권한 체크
    private SearchView searchView;  // 주소 검색창
    private RecyclerView recyclerView;  // 검색 결과 목록
    private LocationAdapter locationAdapter;    //RecyclerView 데이터 연결

    // --- 데이터 관련 변수 (서버에서 받아온 데이터를 저장) ---
    private final List<LocationData> allLocations = new ArrayList<>();
    private CardView addressBox; // CardView 변수 선언
    private TextView markerAddressTextView;
    private IconsManager iconsManager;

    // *** Firebase 변수 변경 (Realtime Database -> Cloud Firestore) ***
    private FirebaseFirestore db;

    private LabelLayer currentLocationLayer;

    private RouteLineManager routeLineManager;
    private RouteLine currentRouteLine;
    private LocationData selectedLocationData;

    private static final double SEARCH_RADIUS_METERS = 2000;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // SDK 초기화
        KakaoMapSdk.init(this, "f74b1223cc505a613aeb568c3277ff52");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        db = FirebaseFirestore.getInstance();

        // xml 연결
        mapView = findViewById(R.id.map_view);
        searchView = findViewById(R.id.search_view);
        recyclerView = findViewById(R.id.recycler_view);
        markerAddressTextView = findViewById(R.id.tv_marker_address); // TextView 초기화

        addressBox = findViewById(R.id.address_box); // xml에 정의된 ID로 가정
        markerAddressTextView = findViewById(R.id.tv_marker_address);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 위치 권한 확인
        locationAccess = new LocationAccess(this, this);
        locationAccess.checkLocationPermission();
    }

    @Override
    public void onPermissionGranted() {
        Toast.makeText(this, "위치 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show();
        startMap();
    }

    @Override
    public void onPermissionDenied() {
        Log.d("Permission", "위치 정보 권한이 거부되었습니다.");
    }

    // 시스템 콜백, LocationAccess에 전달
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationAccess.handleRequestPermissionsResult(requestCode, grantResults);
    }

    // 지도 시작 (초기화)
    private void startMap() {
        mapView.start(new MapLifeCycleCallback() {
            @Override
            public void onMapDestroy() {
            }   // 파괴

            @Override
            public void onMapError(Exception error) {
                Log.e("KakaoMap", "지도 초기화 오류: ", error);
            }
        }, new KakaoMapReadyCallback() {
            @Override
            public void onMapReady(KakaoMap map) {
                kakaoMap = map;
                // *** RouteLineManager 초기화 ***
                routeLineManager = kakaoMap.getRouteLineManager();
                currentLocationLayer = kakaoMap.getLabelManager().addLayer(LabelLayerOptions.from("currentLocationLayer"));

                iconsManager = new IconsManager(kakaoMap, addressBox, markerAddressTextView);

                // *** IconsManager에 클릭 리스너를 설정하여 선택된 위치 정보를 MainActivity로 전달 ***
                iconsManager.setOnMarkerClickListener(locationData -> {
                    selectedLocationData = locationData;
                    markerAddressTextView.setText(locationData.address);
                    addressBox.setVisibility(View.VISIBLE);
                });

                kakaoMap.setOnCameraMoveEndListener((kakaoMap1, cameraPosition, gestureType) -> {
                    // 지도 이동이 완료된 시점의 지도 중심 좌표를 가져옵니다.
                    LatLng mapCenter = cameraPosition.getPosition();
                    Log.d("CameraMove", "지도 이동 완료. 새 중심: " + mapCenter.toString());
                    // 새로운 중심 좌표로 마커를 필터링하고 표시합니다.
                    filterAndDisplayMarkers(mapCenter);
                });

                fetchDataFromFirestore();
                setupMyLocationButton();
                setupRecyclerView();
                setupSearchView();
                startLocationUpdates();
                setupMapClickListener();
                setUpRouteLineManager();
            }
        });

    }


    public void setUpRouteLineManager() {
        Button search_button = findViewById(R.id.search_button);
        search_button.setOnClickListener(v -> {
            // 현재 위치와 선택된 마커 위치가 모두 있어야 경로를 그릴 수 있음
            if (currentLocationLabel != null && currentLocationLabel.getPosition() != null && selectedLocationData != null) {
                // 기존에 그려진 경로선이 있다면 제거
                if (currentRouteLine != null) {
                    routeLineManager.remove(currentRouteLine);
                }

                // 경로선 스타일 설정
                RouteLineStyles styles3 = RouteLineStyles.from(RouteLineStyle.from(16, Color.BLUE)
                        .setPattern(RouteLinePattern.from(R.drawable.route_pattern_arrow, 12)));

                RouteLineStylesSet stylesSet = RouteLineStylesSet.from(styles3);

                List<LatLng> routePoints = new ArrayList<>(Arrays.asList(
                        currentLocationLabel.getPosition(),
                        LatLng.from(selectedLocationData.latitude, selectedLocationData.longitude)
                ));

                // RouteLineSegment 생성
                RouteLineSegment segment = RouteLineSegment.from(routePoints, stylesSet.getStyles("blue_style"));

                // RouteLineOptions 생성 및 경로선 추가
                RouteLineOptions options = RouteLineOptions.from(segment)
                        .setStylesSet(stylesSet);

                currentRouteLine = routeLineManager.addLayer().addRouteLine(options);

                // 경로선이 잘 보이도록 지도 카메라 조정
                kakaoMap.moveCamera(CameraUpdateFactory.fitMapPoints(routePoints.toArray(new LatLng[0])),
                        CameraAnimation.from(500));

                // 주소 박스 숨기기
                addressBox.setVisibility(View.GONE);

            } else {
                Toast.makeText(this, "현재 위치 또는 목적지 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMapClickListener() {
        if (kakaoMap == null) return;

        kakaoMap.setOnMapClickListener((kakaoMap, latLng, pointF, poi) -> {
            // 지도의 빈 공간을 클릭했을 때, 주소 박스가 보이고 있다면 숨깁니다.
            if (addressBox.getVisibility() == View.VISIBLE) {
                addressBox.setVisibility(View.GONE);
                selectedLocationData = null;

                if (currentRouteLine != null) {
                    routeLineManager.remove(currentRouteLine);
                    currentRouteLine = null;
                }
            }
        });
    }


    private void fetchDataFromFirestore() {
        allLocations.clear();
        String[] collections = {"recycling_clothes_location", "recycling_recycle_location", "recycling_phone_location", "recycling_homemachine_location", "recycling_battery_location"};
        AtomicInteger collectionCount = new AtomicInteger(collections.length);

        for (String collectionName : collections) {
            db.collection(collectionName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                LocationData locationData = document.toObject(LocationData.class);
                                if (collectionName.contains("clothes")) {
                                    locationData.type = "clothes";
                                } else if (collectionName.contains("recycle")) {
                                    locationData.type = "recycle";
                                } else if (collectionName.contains("phone")) {
                                    locationData.type = "phone";
                                } else if (collectionName.contains("homemachine")) {
                                    locationData.type = "homemachine";
                                } else {
                                    locationData.type = "battery";
                                }

                                allLocations.add(locationData);
                            }
                        } else {
                            Log.w("Firestore", "컬렉션 가져오기 오류: ", task.getException());
                        }

                        if (collectionCount.decrementAndGet() == 0) {
                            Log.d("Firestore", "모든 데이터 로드 완료. 총 " + allLocations.size() + "개.");
                            if (currentLocationLabel != null) {
                                filterAndDisplayMarkers(currentLocationLabel.getPosition());
                            }
                            if (allLocations.isEmpty()) {
                                Toast.makeText(MainActivity.this, "데이터베이스에서 위치 정보를 가져오지 못했습니다.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    // 위치 실시간 업데이트
    private void startLocationUpdates() {
        // 위치 권한 확인
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Location", "위치 권한이 없어 업데이트를 시작할 수 없습니다.");
            return;
        }

        // 위치 요청 설정
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateIntervalMillis(1000) // 최소 업데이트 간격
                .setMinUpdateDistanceMeters(5)     // 최소 업데이트 거리
                .build();

        // 위치 콜백 정의
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        // onLocationChanged 메서드를 직접 호출하여 위치 처리
                        onLocationChanged(location);
                    }
                }
            }
        };

        // 위치 업데이트 요청
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        Log.d("Location", "FusedLocationProvider를 통해 위치 업데이트를 시작합니다.");
    }



    // 위치 바뀔 때마다 콜백
    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (kakaoMap == null) return;
        LatLng currentPosition = LatLng.from(location.getLatitude(), location.getLongitude());
        if (currentLocationLabel == null) {
            currentLocationLabel = currentLocationLayer.addLabel(LabelOptions.from(currentPosition).setStyles(R.drawable.current_location));
        } else {
            currentLocationLabel.moveTo(currentPosition);
        }

        //filterAndDisplayMarkers(currentPosition);

        if (!isInitialLocationSet) {
            kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(currentPosition, 15));
            isInitialLocationSet = true;
        }
    }

    private void filterAndDisplayMarkers(LatLng currentPosition) {
        if (iconsManager == null || allLocations.isEmpty()) {
            return; // 아직 준비되지 않았다면 실행하지 않음
        }

        List<LocationData> nearbyLocations = new ArrayList<>();
        float[] results = new float[1];

        for (LocationData data : allLocations) {
            // 안드로이드 Location 클래스를 사용하여 두 지점 간의 거리를 미터(m) 단위로 계산
            Location.distanceBetween(
                    currentPosition.latitude, currentPosition.longitude,
                    data.latitude, data.longitude,
                    results);
            float distanceInMeters = results[0];

            // 계산된 거리가 설정한 반경 내에 있는지 확인
            if (distanceInMeters <= SEARCH_RADIUS_METERS) {
                nearbyLocations.add(data);
            }
        }

        // 필터링된 목록으로 마커를 새로 추가 (IconsManager 내부에서 기존 마커는 clear됨)
        iconsManager.addMarkers(nearbyLocations);
        Log.d("MarkerFilter", "현재 위치 주변 " + (int)SEARCH_RADIUS_METERS + "m 내에 " + nearbyLocations.size() + "개의 마커를 표시합니다.");
    }


    // UI 설정
    private void setupRecyclerView() {
        locationAdapter = new LocationAdapter(new ArrayList<>());   // 초기화
        recyclerView.setLayoutManager(new LinearLayoutManager(this));   // 목록 설정
        recyclerView.setAdapter(locationAdapter);   // 어댑터 연결
    }


    // SearchView 설정
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);   // 검색 실행
                hideKeyboard(); // 키보드 내림
                return true;
            }


            // 글자 칠 때마다 호출
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);  // 목록 숨김 (글씨 없음)
                } else {
                    performSearch(newText); // 실시간 검색 (글씨 있음)
                }
                return true;
            }
        });
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            return;
        }

        List<LocationData> results = new ArrayList<>();
        for (LocationData data : allLocations) {
            if (data.address.contains(query)) {
                results.add(data);
            }
        }

        if (results.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
        } else {
            locationAdapter.updateData(results);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }



    // M버튼 설정 (내 위치로 돌아오기)
    private void setupMyLocationButton() {
        Button myLocationButton = findViewById(R.id.currentLocationButton);
        myLocationButton.setOnClickListener(v -> {
            if (kakaoMap != null && currentLocationLabel != null && currentLocationLabel.getPosition() != null) {
                // 누르면 내 위치로 이동 (줌 레벨 17)
                kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(currentLocationLabel.getPosition(), 17), CameraAnimation.from(500));
            } else {
                Toast.makeText(this, "아직 위치 정보를 받지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // 키보드 숨김
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    // 업뎃 중지
    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.pause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.d("Location", "위치 업데이트가 중지되었습니다.");
        }
    }

    // RecyclerView 위치 표시
    class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {
        private List<LocationData> items;

        LocationAdapter(List<LocationData> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location, parent, false);
            return new LocationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
            LocationData item = items.get(position);
            holder.addressTextView.setText(item.address);

            holder.itemView.setOnClickListener(v -> {   // 검색 결과 클릭 시
                LatLng targetPosition = LatLng.from(item.latitude, item.longitude);
                Log.d("ItemClick", "이동할 위치: " + targetPosition.toString());

                // 해당 위치로 이동
                kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(targetPosition, 17), CameraAnimation.from(500));

                // 선택된 위치 정보 업데이트
                selectedLocationData = item;
                markerAddressTextView.setText(item.address);

                // 주소 카드뷰를 보여줌
                addressBox.setVisibility(View.VISIBLE);

                // UI 정리
                recyclerView.setVisibility(View.GONE);
                searchView.setQuery("", false);
                searchView.clearFocus();
                hideKeyboard();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        void updateData(List<LocationData> newItems) {
            this.items.clear();
            this.items.addAll(newItems);
            notifyDataSetChanged();
        }

        class LocationViewHolder extends RecyclerView.ViewHolder {
            TextView addressTextView;
            LocationViewHolder(View itemView) {
                super(itemView);
                addressTextView = itemView.findViewById(R.id.tv_address);
            }
        }
    }
}
