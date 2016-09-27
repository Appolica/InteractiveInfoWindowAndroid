package com.appolica.sample.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

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

public class SampleWithMapFragmentActivity extends FragmentActivity
        implements InfoWindowManager.WindowShowListener {

    private static final String RECYCLER_VIEW = "RECYCLER_VIEW_MARKER";
    private static final String FORM_VIEW = "FORM_VIEW_MARKER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_with_map_fragment);

        final MapInfoWindowFragment mapInfoWindowFragment =
                (MapInfoWindowFragment) getSupportFragmentManager().findFragmentById(R.id.infoWindowMap);

        final InfoWindowManager infoWindowManager = mapInfoWindowFragment.infoWindowManager();

        mapInfoWindowFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {

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
        });

        infoWindowManager.setWindowShowListener(new InfoWindowManager.WindowShowListener() {
            @Override
            public void onWindowShowStarted(@NonNull InfoWindow infoWindow) {

            }

            @Override
            public void onWindowShown(@NonNull InfoWindow infoWindow) {

            }

            @Override
            public void onWindowHideStarted(@NonNull InfoWindow infoWindow) {

            }

            @Override
            public void onWindowHidden(@NonNull InfoWindow infoWindow) {

            }
        });

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
