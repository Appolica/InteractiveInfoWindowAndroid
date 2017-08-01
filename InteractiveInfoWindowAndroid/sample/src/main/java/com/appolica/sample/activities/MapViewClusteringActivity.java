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
import com.appolica.sample.model.MyItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.maps.android.clustering.ClusterManager;

public class MapViewClusteringActivity
        extends FragmentActivity
        implements OnMapReadyCallback{

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
        // Create the cluster manager with the googleMap instance
        final ClusterManager<MyItem> clusterManager = new ClusterManager<>(this, googleMap);
        infoWindowManager.onMapReady(googleMap);

        final int offsetX = (int) getResources().getDimension(R.dimen.marker_offset_x);
        final int offsetY = (int) getResources().getDimension(R.dimen.marker_offset_y);

        final InfoWindow.MarkerSpecification markerSpec =
                new InfoWindow.MarkerSpecification(offsetX, offsetY);

        // Create the markers from a class inheriting ClusterItem
        final MyItem item1 = new MyItem(5, 5);
        final MyItem item2 = new MyItem(4.9, 4.9);
        final MyItem item3 = new MyItem(4.8, 4.8);
        final MyItem item4 = new MyItem(4.7, 4.7);
        final MyItem item5 = new MyItem(4.6, 4.6);

        // You can use whatevere property you like for differentiating the markers (enum,strings, etc.)
        item1.setType(RECYCLER_VIEW);
        item2.setType(FORM_VIEW);
        item3.setType(RECYCLER_VIEW);
        item4.setType(FORM_VIEW);
        item5.setType(RECYCLER_VIEW);

        // Add the markers to the cluster manager
        clusterManager.addItem(item1);
        clusterManager.addItem(item2);
        clusterManager.addItem(item3);
        clusterManager.addItem(item4);
        clusterManager.addItem(item5);

        // Set an onClusterItemClick listener which is called when a single marker(not a cluster is called)
        // You can set a onClusterClick listener if you want to add info windows to clusters
        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
            @Override
            public boolean onClusterItemClick(MyItem myItem) {
                onItemClick(myItem);
                return false;
            }
        });

        // Create the info windows as usual
        recyclerWindow = new InfoWindow(item1.getPosition(), markerSpec, new RecyclerViewFragment());
        formWindow = new InfoWindow(item2.getPosition(), markerSpec, new FormFragment());

        // Add the cluster manager as an on camera idle listener. Needed for cluster manager to work!
        infoWindowManager.setOnCameraIdleListener(clusterManager);

        // Add the cluster manager as an on marker click listener. Needed if you want to receive the
        // onClusterItemClick/onClusterClick events
        googleMap.setOnMarkerClickListener(clusterManager);
    }

    public boolean onItemClick(MyItem marker) {
        InfoWindow infoWindow = null;
        switch (marker.getType()) {
            case RECYCLER_VIEW:
                infoWindow = recyclerWindow;
                break;
            case FORM_VIEW:
                infoWindow = formWindow;
                break;
        }

        if (infoWindow != null) {
            infoWindow.setPosition(marker.getPosition());
            infoWindowManager.toggle(infoWindow, true);
        }

        return true;
    }
}

