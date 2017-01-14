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
     * @param markerSpec Provides the marker's offsetX and offsetY.
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
     * @param markerSpec Provides the marker's offsetX and offsetY.
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

    public static class MarkerSpecification implements Serializable {
        private int offsetX;
        private int offsetY;

        private boolean centerByX = true;
        private boolean centerByY = false;

        /**
         * Create marker specification by providing InfoWindow's x and y offsets from marker's
         * screen location.
         *
         * <p>
         *    Note: By default offsetX will be ignored, so in order for it to take effect, you
         *    must call setCenterByX(false).
         *
         *    Also if you want to use dp, you should convert the values to px by yourself.
         * </p>
         *
         * @param offsetX InfoWindow's offset by x from marker's screen location.
         *                Value must be in px.
         * @param offsetY InfoWindow's offset by y from marker's screen location.
         *                Value must be in px.
         *
         * @see #setCenterByX(boolean)
         * @see #setCenterByY(boolean)
         */
        public MarkerSpecification(int offsetX, int offsetY) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        public int getOffsetX() {
            return offsetX;
        }

        public void setOffsetX(int offsetX) {
            this.offsetX = offsetX;
        }

        public int getOffsetY() {
            return offsetY;
        }

        public void setOffsetY(int offsetY) {
            this.offsetY = offsetY;
        }

        public boolean centerByX() {
            return centerByX;
        }

        /**
         * Set whether the InfoWindow's center by x should be the same as the marker's
         * screen x coordinate.
         * If false, offsetX will be used. Default value is true.
         *
         * @param centerByX Pass true if you want InfoWindow's x center to be the
         *                  same as the marker's screen x coordinate. Pass false if you want
         *                  offsetX to be used instead.
         *
         * @see com.appolica.interactiveinfowindow.InfoWindowManager#centerInfoWindow(InfoWindow)
         */
        public void setCenterByX(boolean centerByX) {
            this.centerByX = centerByX;
        }

        public boolean centerByY() {
            return centerByY;
        }

        /**
         * Set whether the InfoWindow's center by y should be the same as the marker's
         * screen y coordinate.
         * If false, offsetY will be used. Default value is false.
         *
         * @param centerByY Pass true if you want InfoWindow's y center to be the
         *                  same as the marker's screen y coordinate. Pass false if you want
         *                  offsetX to be used instead.
         *
         * @see com.appolica.interactiveinfowindow.InfoWindowManager#centerInfoWindow(InfoWindow)
         */
        public void setCenterByY(boolean centerByY) {
            this.centerByY = centerByY;
        }

        @Override
        public boolean equals(Object o) {

            if (o instanceof MarkerSpecification) {
                final MarkerSpecification markerSpecification = (MarkerSpecification) o;

                final boolean offsetCheck = markerSpecification.getOffsetY() == offsetY;

                return offsetCheck;
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
