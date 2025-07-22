package com.example.myrecycleplication;

import android.view.View;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelLayerOptions;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 지도 위의 데이터 마커(라벨)를 관리하는 클래스.
 */
public class IconsManager {
    private final KakaoMap kakaoMap;
    private final CardView addressBox;
    private final TextView markerAddressTextView;
    private final Map<Label, LocationData> markerDataMap = new HashMap<>();

    // *** 데이터 마커들을 위한 전용 레이어 ***
    private final LabelLayer dataMarkerLayer;

    public interface OnMarkerClickListener {
        void onMarkerClicked(LocationData locationData);
    }
    private OnMarkerClickListener markerClickListener;

    public IconsManager(KakaoMap kakaoMap, CardView addressBox, TextView markerAddressTextView) {
        this.kakaoMap = kakaoMap;
        this.addressBox = addressBox;
        this.markerAddressTextView = markerAddressTextView;
        // *** 데이터 마커 전용 레이어를 생성합니다. ***
        this.dataMarkerLayer = kakaoMap.getLabelManager().addLayer(LabelLayerOptions.from("dataMarkerLayer"));
        setupLabelClickListener();
    }

    public void setOnMarkerClickListener(OnMarkerClickListener listener) {
        this.markerClickListener = listener;
    }

    /**
     * 데이터 마커 레이어에 있는 모든 마커를 제거합니다.
     */
    public void clearMarkers() {
        if (dataMarkerLayer != null) {
            dataMarkerLayer.removeAll();
        }
        markerDataMap.clear();
    }

    /**
     * LocationData 리스트를 받아와 전용 레이어에 마커를 추가합니다.
     */
    public void addMarkers(List<LocationData> locations) {
        if (dataMarkerLayer == null || locations == null) return;
        clearMarkers();

        for (LocationData data : locations) {
            int markerResourceId;
            if ("clothes".equals(data.type)) {
                markerResourceId = R.drawable.clothes_bin;
            } else if("recycle".equals(data.type)){
                markerResourceId = R.drawable.recycle_bin;
            }  else if("phone".equals(data.type)){
                markerResourceId = R.drawable.phone_bin;
            }  else if("homemachine".equals(data.type)){
                markerResourceId = R.drawable.homemachine_bin;
            }  else {
                markerResourceId = R.drawable.battery_bin;
            }

            LatLng position = LatLng.from(data.latitude, data.longitude);
            LabelOptions options = LabelOptions.from(position)
                    .setStyles(LabelStyles.from(LabelStyle.from(markerResourceId).setZoomLevel(13)));


            // *** 전용 레이어에 마커를 추가합니다. ***
            Label addedLabel = dataMarkerLayer.addLabel(options);
            markerDataMap.put(addedLabel, data);
        }
    }

    private void setupLabelClickListener() {
        if (kakaoMap == null) return;
        kakaoMap.setOnLabelClickListener((kakaoMap, labelManager, label) -> {
            LocationData data = markerDataMap.get(label);
            if (data != null && markerClickListener != null) {
                markerClickListener.onMarkerClicked(data);
            }
            return true;
        });
    }
}