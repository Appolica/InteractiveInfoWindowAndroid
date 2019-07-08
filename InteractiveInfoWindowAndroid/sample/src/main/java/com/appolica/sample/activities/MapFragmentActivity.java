package com.appolica.sample.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment;
import com.appolica.sample.R;
import com.appolica.sample.fragments.FormFragment;
import com.appolica.sample.fragments.RecyclerViewFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragmentActivity
        extends FragmentActivity
        implements InfoWindowManager.WindowShowListener,
        GoogleMap.OnMarkerClickListener {

    private static final String RECYCLER_VIEW = "RECYCLER_VIEW_MARKER";
    private static final String FORM_VIEW = "FORM_VIEW_MARKER";

    private InfoWindow recyclerWindow;
    private InfoWindow formWindow;
    private InfoWindowManager infoWindowManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_with_map_fragment);

        final MapInfoWindowFragment mapInfoWindowFragment =
                (MapInfoWindowFragment) getSupportFragmentManager().findFragmentById(R.id.infoWindowMap);

        infoWindowManager = mapInfoWindowFragment.infoWindowManager();
        infoWindowManager.setHideOnFling(true);

        mapInfoWindowFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                final Marker marker1 = googleMap.addMarker(new MarkerOptions().position(new LatLng(5, 5)).snippet(RECYCLER_VIEW));
                final Marker marker2 = googleMap.addMarker(new MarkerOptions().position(new LatLng(1, 1)).snippet(FORM_VIEW));

                final int offsetX = (int) getResources().getDimension(R.dimen.marker_offset_x);
                final int offsetY = (int) getResources().getDimension(R.dimen.marker_offset_y);

                final InfoWindow.MarkerSpecification markerSpec =
                        new InfoWindow.MarkerSpecification(offsetX, offsetY);

                recyclerWindow = new InfoWindow(marker1, markerSpec, new RecyclerViewFragment());
                formWindow = new InfoWindow(marker2, markerSpec, new FormFragment());

                googleMap.setOnMarkerClickListener(MapFragmentActivity.this);
            }
        });

        infoWindowManager.setWindowShowListener(MapFragmentActivity.this);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        InfoWindow infoWindow = null;
        switch (marker.getSnippet()) {
            case RECYCLER_VIEW:
                infoWindow = recyclerWindow;
                break;
            case FORM_VIEW:
                infoWindow = formWindow;
                break;
        }

        if (infoWindow != null) {
            infoWindowManager.toggle(infoWindow, true);
        }

        return true;
    }

    @Override
    public void onWindowShowStarted(@NonNull InfoWindow infoWindow) {
//        Log.d("debug", "onWindowShowStarted: " + infoWindow);
    }

    @Override
    public void onWindowShown(@NonNull InfoWindow infoWindow) {
//        Log.d("debug", "onWindowShown: " + infoWindow);
    }

    @Override
    public void onWindowHideStarted(@NonNull InfoWindow infoWindow) {
//        Log.d("debug", "onWindowHideStarted: " + infoWindow);
    }

    @Override
    public void onWindowHidden(@NonNull InfoWindow infoWindow) {
//        Log.d("debug", "onWindowHidden: " + infoWindow);
    }
}
