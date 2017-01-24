package com.appolica.sample.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;

import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.appolica.interactiveinfowindow.customview.TouchInterceptFrameLayout;
import com.appolica.sample.R;
import com.appolica.sample.fragments.FormFragment;
import com.appolica.sample.fragments.RecyclerViewFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapViewActivity
        extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private static final String RECYCLER_VIEW = "RECYCLER_VIEW_MARKER";
    private static final String FORM_VIEW = "FORM_VIEW_MARKER";

    private MapView mapView;

    private InfoWindowManager infoWindowManager;

    private InfoWindow recyclerWindow;
    private InfoWindow formWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_with_map_view);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        final TouchInterceptFrameLayout mapViewContainer =
                (TouchInterceptFrameLayout) findViewById(R.id.mapViewContainer);

        mapView.getMapAsync(this);

        infoWindowManager = new InfoWindowManager(getSupportFragmentManager());
        infoWindowManager.onParentViewCreated(mapViewContainer, savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
        infoWindowManager.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        infoWindowManager.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        infoWindowManager.onMapReady(googleMap);

        final Marker marker1 = googleMap.addMarker(new MarkerOptions().position(new LatLng(5, 5)).snippet(RECYCLER_VIEW));
        final Marker marker2 = googleMap.addMarker(new MarkerOptions().position(new LatLng(1, 1)).snippet(FORM_VIEW));

        final int offsetX = (int) getResources().getDimension(R.dimen.marker_offset_x);
        final int offsetY = (int) getResources().getDimension(R.dimen.marker_offset_y);

        final InfoWindow.MarkerSpecification markerSpec =
                new InfoWindow.MarkerSpecification(offsetX, offsetY);

        recyclerWindow = new InfoWindow(marker1, markerSpec, new RecyclerViewFragment());
        formWindow = new InfoWindow(marker2, markerSpec, new FormFragment());

        googleMap.setOnMarkerClickListener(this);
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
}
