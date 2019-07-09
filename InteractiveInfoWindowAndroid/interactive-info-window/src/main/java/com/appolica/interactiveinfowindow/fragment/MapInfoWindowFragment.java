/*
 * Copyright (c) 2016 Appolica Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appolica.interactiveinfowindow.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.appolica.interactiveinfowindow.customview.TouchInterceptFrameLayout;
import com.appolica.mapanimations.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class MapInfoWindowFragment extends Fragment {

    private static final String TAG = "MapInfoWindowFragment";

    private GoogleMap googleMap;
    private InfoWindowManager infoWindowManager;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_map_infowindow, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        infoWindowManager = new InfoWindowManager(getChildFragmentManager());
        infoWindowManager.onParentViewCreated((TouchInterceptFrameLayout) view, savedInstanceState);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (googleMap == null) {
            // Try to obtain the map from the SupportMapFragment.

            final SupportMapFragment mapFragment =
                    (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);

            mapFragment
                    .getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            MapInfoWindowFragment.this.googleMap = googleMap;
                            setUpMap();
                        }
                    });
        }
    }

    private void setUpMap() {
        infoWindowManager.onMapReady(googleMap);
    }

    /**
     * Get the {@link InfoWindowManager}, used for showing/hiding and positioning the
     * {@link InfoWindow}.
     *
     * @return The {@link InfoWindowManager}
     */
    public InfoWindowManager infoWindowManager() {
        return infoWindowManager;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        infoWindowManager.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        infoWindowManager.onDestroy();
    }

    /**
     * Use this method to get the {@link GoogleMap} object asynchronously from our fragment.
     *
     * @param onMapReadyCallback The callback that will be called providing you the GoogleMap
     *                           object.
     */
    public void getMapAsync(OnMapReadyCallback onMapReadyCallback) {
        final SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);

        mapFragment.getMapAsync(onMapReadyCallback);
    }
}
