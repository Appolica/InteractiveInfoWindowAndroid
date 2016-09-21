package com.appolica.sample.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;

import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.appolica.interactiveinfowindow.customview.TouchInterceptFrameLayout;
import com.appolica.sample.ItemFragment;
import com.appolica.sample.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class SampleWithMapViewActivity
        extends FragmentActivity
        implements OnMapReadyCallback,
        ItemFragment.OnFragmentInteractionListener {

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

        googleMap.addMarker(new MarkerOptions().position(new LatLng(5, 5)).title("Marker 1"));
        googleMap.addMarker(new MarkerOptions().position(new LatLng(1, 1)).title("Marker 2"));

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                final InfoWindow.MarkerSpecification markerSpec =
                        new InfoWindow.MarkerSpecification(20, 90);
                final ItemFragment fragment = ItemFragment.newInstance("test", "test");

                final InfoWindow infoWindow = new InfoWindow(marker, markerSpec, fragment);

                infoWindowManager.toggle(infoWindow, true);

                return true;
            }
        });
    }

    @Override
    public void onFragmentInteraction(String id) {

    }
}
