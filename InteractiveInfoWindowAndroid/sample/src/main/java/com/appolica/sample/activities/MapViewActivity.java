package com.appolica.sample.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
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
        implements OnMapReadyCallback {

    private static final String RECYCLER_VIEW = "RECYCLER_VIEW_MARKER";
    private static final String FORM_VIEW = "FORM_VIEW_MARKER";


    private MapView mapView;

    private InfoWindowManager infoWindowManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_with_map_view);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);

        infoWindowManager = new InfoWindowManager(getSupportFragmentManager());
        infoWindowManager.onParentViewCreated(
                (TouchInterceptFrameLayout) findViewById(R.id.mapViewContainer), savedInstanceState);
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
    public void onMapReady(GoogleMap googleMap) {
        infoWindowManager.onMapReady(googleMap);

        googleMap.addMarker(new MarkerOptions().position(new LatLng(5, 5)).snippet(RECYCLER_VIEW));
        googleMap.addMarker(new MarkerOptions().position(new LatLng(1, 1)).snippet(FORM_VIEW));

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                final InfoWindow.MarkerSpecification markerSpec =
                        new InfoWindow.MarkerSpecification(20, 90);

                Fragment fragment = null;

                switch (marker.getSnippet()) {
                    case RECYCLER_VIEW:
                        fragment = new RecyclerViewFragment();
                        break;
                    case FORM_VIEW:
                        fragment = new FormFragment();
                        break;
                }

                if (fragment != null) {
                    final InfoWindow infoWindow = new InfoWindow(marker, markerSpec, fragment);
                    infoWindowManager.toggle(infoWindow, true);
                }


                return true;
            }
        });
    }
}
