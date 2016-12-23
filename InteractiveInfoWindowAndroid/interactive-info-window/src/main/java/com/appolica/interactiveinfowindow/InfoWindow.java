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

package com.appolica.interactiveinfowindow;

import android.support.v4.app.Fragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.Serializable;

/**
 * This class contains everything needed for an InfoWindow to be shown.
 * It also provides a state that shows whether the window is already shown/hidden
 * or is in the middle of showing/hiding.
 */
public class InfoWindow {
    private LatLng position;
    private MarkerSpecification markerSpec;

    private Fragment windowFragment;

    private WindowState windowState = WindowState.HIDDEN;

    /**
     * @param marker The marker which determines the window's position on the screen.
     * @param markerSpec Provides the marker's width and height.
     * @param windowFragment The actual window that is displayed on the screen.
     */
    public InfoWindow(
            Marker marker,
            MarkerSpecification markerSpec,
            Fragment windowFragment) {

        this(marker.getPosition(), markerSpec, windowFragment);
    }

    /**
     * @param position The {@link com.google.android.gms.maps.model.LatLng} which determines the window's position on the screen.
     * @param markerSpec Provides the marker's width and height.
     * @param windowFragment The actual window that is displayed on the screen.
     */
    public InfoWindow(
            LatLng position,
            MarkerSpecification markerSpec,
            Fragment windowFragment) {

        this.position = position;
        this.markerSpec = markerSpec;
        this.windowFragment = windowFragment;
    }

    public LatLng getPosition() { return position; }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public MarkerSpecification getMarkerSpec() {
        return markerSpec;
    }

    public void setMarkerSpec(MarkerSpecification markerSpec) {
        this.markerSpec = markerSpec;
    }

    public Fragment getWindowFragment() {
        return windowFragment;
    }

    public void setWindowFragment(Fragment windowFragment) {
        this.windowFragment = windowFragment;
    }

    /**
     * Get window's state which could be one of the following:
     * <br>
     * {@link WindowState#SHOWING}, {@link WindowState#SHOWN},
     * {@link WindowState#HIDING}, {@link WindowState#HIDDEN}
     *
     * @return The InfoWindow's state.
     */
    public WindowState getWindowState() {
        return windowState;
    }

    public void setWindowState(WindowState windowState) {
        this.windowState = windowState;
    }

    public enum WindowState {
        SHOWING, SHOWN, HIDING, HIDDEN
    }

    /**
     * Holds the width and height of the marker.
     */
    public static class MarkerSpecification implements Serializable {
        private int width;
        private int height;

        public MarkerSpecification(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        @Override
        public boolean equals(Object o) {

            if (o instanceof MarkerSpecification) {
                final MarkerSpecification markerSpecification = (MarkerSpecification) o;

                final boolean widthCheck = markerSpecification.getWidth() == width;
                final boolean heightCheck = markerSpecification.getHeight() == height;

                return widthCheck && heightCheck;
            }

            return super.equals(o);
        }
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof InfoWindow) {
            final boolean markerCheck = ((InfoWindow) o).getPosition().equals(position);
            final boolean specCheck = ((InfoWindow) o).getMarkerSpec().equals(markerSpec);

            return markerCheck && specCheck;
        }

        return super.equals(o);
    }

}
