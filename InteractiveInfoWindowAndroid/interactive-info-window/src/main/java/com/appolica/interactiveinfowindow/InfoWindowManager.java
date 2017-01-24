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

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.appolica.interactiveinfowindow.animation.SimpleAnimationListener;
import com.appolica.interactiveinfowindow.customview.TouchInterceptFrameLayout;
import com.appolica.mapanimations.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;

/**
 * This is where all the magic happens. Use this class to show your interactive {@link InfoWindow}
 * above your {@link com.google.android.gms.maps.model.Marker}.
 */
public class InfoWindowManager
        implements GoogleMap.OnCameraIdleListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnMapClickListener {

    public static final String FRAGMENT_TAG_INFO = "InfoWindow";

    private static final String TAG = "InfoWindowManager";

    public static final int DURATION_WINDOW_ANIMATION = 200;
    public static final int DURATION_CAMERA_ENSURE_VISIBLE_ANIMATION = 500;

    private GoogleMap googleMap;

    private FragmentManager fragmentManager;

    private InfoWindow currentWindow;
    private ViewGroup parent;
    private View currentContainer;

    private ContainerSpecification containerSpec;

    private FragmentContainerIdProvider idProvider;

    private GoogleMap.OnMapClickListener onMapClickListener;

    private GoogleMap.OnCameraIdleListener onCameraIdleListener;
    private GoogleMap.OnCameraMoveStartedListener onCameraMoveStartedListener;
    private GoogleMap.OnCameraMoveListener onCameraMoveListener;
    private GoogleMap.OnCameraMoveCanceledListener onCameraMoveCanceledListener;

    private Animation showAnimation;
    private Animation hideAnimation;

    private WindowShowListener windowShowListener;

    private boolean hideOnFling = false;

    public InfoWindowManager(@NonNull final FragmentManager fragmentManager) {

        this.fragmentManager = fragmentManager;
    }

    /**
     * Call this method if you are not using
     * {@link com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment}. If you are calling
     * it from a Fragment we suggest you to call it in {@link Fragment#onViewCreated(View, Bundle)}
     * and if you are calling it from an Activity you should call it in
     * {@link android.app.Activity#onCreate(Bundle)}.
     *
     * @param parent             The parent of your {@link com.google.android.gms.maps.MapView} or
     *                           {@link com.google.android.gms.maps.SupportMapFragment}.
     * @param savedInstanceState The saved state Bundle from your Fragment/Activity.
     */
    public void onParentViewCreated(
            @NonNull final TouchInterceptFrameLayout parent,
            @Nullable final Bundle savedInstanceState) {

        this.parent = parent;
        this.idProvider = new FragmentContainerIdProvider(savedInstanceState);
        this.containerSpec = generateDefaultContainerSpecs(parent.getContext());

        parent.setDetector(
                new GestureDetector(
                        parent.getContext(),
                        new GestureDetector.SimpleOnGestureListener() {

                            @Override
                            public boolean onScroll(
                                    MotionEvent e1, MotionEvent e2,
                                    float distanceX, float distanceY) {

                                if (isOpen()) {
                                    centerInfoWindow(currentWindow, currentContainer);
                                }

                                return true;
                            }

                            @Override
                            public boolean onFling(
                                    MotionEvent e1, MotionEvent e2,
                                    float velocityX, float velocityY) {

                                if (isOpen()) {
                                    if (hideOnFling) {
                                        hide(currentWindow);
                                    } else {
                                        centerInfoWindow(currentWindow, currentContainer);
                                    }
                                }

                                return true;
                            }

                            @Override
                            public boolean onDoubleTap(MotionEvent e) {

                                if (isOpen()) {
                                    centerInfoWindow(currentWindow, currentContainer);
                                }

                                return true;
                            }
                        }));

        currentContainer = parent.findViewById(idProvider.currentId);

        if (currentContainer == null) {
            currentContainer = createContainer(parent);

            parent.addView(currentContainer);
        }

        final Fragment oldFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG_INFO);
        if (oldFragment != null) {
            fragmentManager.beginTransaction()
                    .remove(oldFragment)
                    .commitNow();
        }

    }

    private View createContainer(@NonNull final ViewGroup parent) {
        final LinearLayout container = new LinearLayout(parent.getContext());

        container.setLayoutParams(generateDefaultLayoutParams());
        container.setId(idProvider.getNewId());
        container.setVisibility(View.INVISIBLE);

        return container;
    }

    private FrameLayout.LayoutParams generateDefaultLayoutParams() {

        return generateLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private FrameLayout.LayoutParams generateLayoutParams(
            final int infoWindowWidth, final int infoWindowHeight) {

        return new FrameLayout.LayoutParams(infoWindowWidth, infoWindowHeight);
    }

    /**
     * Same as calling <code>toggle(currentWindow, true);</code>
     *
     * @param infoWindow The {@link InfoWindow} that is to be shown/hidden.
     * @see #toggle(InfoWindow, boolean)
     */
    public void toggle(@NonNull final InfoWindow infoWindow) {
        toggle(infoWindow, true);
    }

    /**
     * Open/hide the given {@link InfoWindow}.
     *
     * @param infoWindow The {@link InfoWindow} that is to be shown/hidden.
     * @param animated   <code>true</code> if you want to toggle it with animation,
     *                   <code>false</code> otherwise.
     */
    public void toggle(@NonNull final InfoWindow infoWindow, final boolean animated) {

        if (isOpen()) {
            // If the toggled window is tha same as the already opened one, close it.
            // Otherwise close the currently opened window and open the new one.
            if (infoWindow.equals(currentWindow)) {
                hide(infoWindow, animated);
            } else {
                show(infoWindow, animated);
            }

        } else {
            show(infoWindow, animated);
        }

    }

    /**
     * Same as calling <code>show(currentWindow, true);</code>
     *
     * @param infoWindow The {@link InfoWindow} that is to be shown.
     * @see #show(InfoWindow, boolean)
     */
    public void show(@NonNull final InfoWindow infoWindow) {
        show(infoWindow, true);
    }

    /**
     * Show the given {@link InfoWindow}. Pass <code>true</code> if you want this action
     * to be animated, <code>false</code> otherwise. If another window has been already opened
     * it will be closed while opening the new one.
     *
     * @param window   The {@link InfoWindow} that is to be shown.
     * @param animated <code>true</code> if you want to show it with animation,
     *                 <code>false</code> otherwise.
     */
    public void show(@NonNull final InfoWindow window, final boolean animated) {
        // Check if already opened
        if (isOpen()) {

            internalHide(currentContainer, currentWindow);

            currentContainer = createContainer(parent);
            parent.addView(currentContainer);
        }

        setCurrentWindow(window);

        internalShow(window, currentContainer, animated);
    }

    private void internalShow(@NonNull final InfoWindow infoWindow,
                              @NonNull final View container,
                              final boolean animated) {

        addFragment(infoWindow.getFragment(), container);
        prepareView(container, infoWindow);

        if (animated) {

            animateWindowOpen(infoWindow, container);

        } else {

            container.setVisibility(View.VISIBLE);

        }
    }

    private void prepareView(final View view, final InfoWindow infoWindow) {

        updateWithContainerSpec(view);

        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {

                centerInfoWindow(infoWindow, view);
                ensureVisible(view);

                view.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
    }

    private void updateWithContainerSpec(final View view) {
        ViewCompat.setBackground(view, containerSpec.background);
    }

    private void animateWindowOpen(@NonNull final InfoWindow infoWindow,
                                   @NonNull final View container) {

        final SimpleAnimationListener animationListener = new SimpleAnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

                container.setVisibility(View.VISIBLE);
                propagateShowEvent(infoWindow, InfoWindow.State.SHOWING);

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                propagateShowEvent(infoWindow, InfoWindow.State.SHOWN);
                setCurrentWindow(infoWindow);

            }
        };

        if (showAnimation == null) {

            container.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {

                        @Override
                        public boolean onPreDraw() {
                            final int containerWidth = container.getWidth();
                            final int containerHeight = container.getHeight();

                            final float pivotX = container.getX() + containerWidth / 2;
                            final float pivotY = container.getY() + containerHeight;

                            final ScaleAnimation scaleAnimation = new ScaleAnimation(
                                    0f, 1f,
                                    0f, 1f,
                                    pivotX, pivotY);

                            scaleAnimation.setDuration(DURATION_WINDOW_ANIMATION);
                            scaleAnimation.setInterpolator(new DecelerateInterpolator());
                            scaleAnimation.setAnimationListener(animationListener);

                            container.startAnimation(scaleAnimation);

                            container.getViewTreeObserver().removeOnPreDrawListener(this);
                            return true;
                        }
                    });
        } else {
            showAnimation.setAnimationListener(animationListener);
            container.startAnimation(showAnimation);
        }
    }

    /**
     * Same as calling <code>hide(currentWindow, true);</code>
     *
     * @param infoWindow The {@link InfoWindow} that is to be hidden.
     * @see #hide(InfoWindow, boolean)
     */
    public void hide(@NonNull final InfoWindow infoWindow) {
        hide(infoWindow, true);
    }

    /**
     * Hides the given {@link InfoWindow}. Pass <code>true</code> if you want this action
     * to be animated, <code>false</code> otherwise.
     *
     * @param infoWindow The {@link InfoWindow} that is to be hidden.
     * @param animated   <code>true</code> if you want to hide it with animation,
     *                   <code>false</code> otherwise.
     */
    public void hide(@NonNull final InfoWindow infoWindow, final boolean animated) {
        internalHide(currentContainer, infoWindow, animated);
    }

    private void internalHide(@NonNull final View container, @NonNull final InfoWindow infoWindow) {
        internalHide(container, infoWindow, true);
    }

    private void internalHide(
            @NonNull final View container,
            @NonNull final InfoWindow toHideWindow,
            final boolean animated) {

        if (animated) {

            final Animation animation;

            if (hideAnimation == null) {

                final int containerWidth = container.getWidth();
                final int containerHeight = container.getHeight();

                final float pivotX = container.getX() + containerWidth / 2;
                final float pivotY = container.getY() + containerHeight;

                animation = new ScaleAnimation(
                        1f, 0f,
                        1f, 0f,
                        pivotX, pivotY);

                animation.setDuration(DURATION_WINDOW_ANIMATION);
                animation.setInterpolator(new DecelerateInterpolator());


            } else {
                animation = hideAnimation;
            }

            animation.setAnimationListener(new SimpleAnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    toHideWindow.setState(InfoWindow.State.HIDING);
                    propagateShowEvent(toHideWindow, InfoWindow.State.HIDING);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    removeWindow(toHideWindow, container);

                    if (container.getId() != InfoWindowManager.this.currentContainer.getId()) {
                        parent.removeView(container);
                    }

                    toHideWindow.setState(InfoWindow.State.HIDDEN);
                    propagateShowEvent(toHideWindow, InfoWindow.State.HIDDEN);
                }
            });

            this.currentContainer.startAnimation(animation);

        } else {

            removeWindow(toHideWindow, container);
            propagateShowEvent(toHideWindow, InfoWindow.State.HIDDEN);

        }
    }

    private void propagateShowEvent(
            @NonNull final InfoWindow infoWindow,
            @NonNull final InfoWindow.State state) {

        if (windowShowListener != null) {
            switch (state) {
                case SHOWING:

                    windowShowListener.onWindowShowStarted(infoWindow);

                    break;
                case SHOWN:

                    windowShowListener.onWindowShown(infoWindow);

                    break;
                case HIDING:

                    windowShowListener.onWindowHideStarted(infoWindow);

                    break;
                case HIDDEN:

                    windowShowListener.onWindowHidden(infoWindow);

                    break;
            }
        }
    }

    private void centerInfoWindow(@NonNull final InfoWindow infoWindow,
                                  @NonNull final View container) {
        final InfoWindow.MarkerSpecification markerSpec = infoWindow.getMarkerSpec();
        final Projection projection = googleMap.getProjection();

        final Point windowScreenLocation = projection.toScreenLocation(infoWindow.getPosition());

        final int containerWidth = container.getWidth();
        final int containerHeight = container.getHeight();

        final int x;
        if (markerSpec.centerByX()) {
            x = windowScreenLocation.x - containerWidth / 2;
        } else {
            x = windowScreenLocation.x + markerSpec.getOffsetX();
        }

        final int y;
        if (markerSpec.centerByY()) {
            y = windowScreenLocation.y - containerHeight / 2;
        } else {
            y = windowScreenLocation.y - containerHeight - markerSpec.getOffsetY();
        }

        final int pivotX = containerWidth / 2;
        final int pivotY = containerHeight;

        container.setPivotX(pivotX);
        container.setPivotY(pivotY);

        container.setX(x);
        container.setY(y);
    }

    private boolean ensureVisible(@NonNull final View infoWindowContainer) {

        final int[] infoWindowLocation = new int[2];
        infoWindowContainer.getLocationOnScreen(infoWindowLocation);

        final boolean visible = true;
        final Rect infoWindowRect = new Rect();
        infoWindowContainer.getHitRect(infoWindowRect);

        final int[] parentPosition = new int[2];
        parent.getLocationOnScreen(parentPosition);

        final Rect parentRect = new Rect();
        parent.getGlobalVisibleRect(parentRect);

        infoWindowContainer.getGlobalVisibleRect(infoWindowRect);

        final int visibleWidth = infoWindowRect.width();
        final int actualWidth = infoWindowContainer.getWidth();

        final int visibleHeight = infoWindowRect.height();
        final int actualHeight = infoWindowContainer.getHeight();

        int scrollX = (visibleWidth - actualWidth);
        int scrollY = (visibleHeight - actualHeight);

        if (scrollX != 0) {
            if (infoWindowRect.left == parentRect.left) {
                scrollX = -Math.abs(scrollX);
            } else {
                scrollX = Math.abs(scrollX);
            }
        }

        if (scrollY != 0) {
            if (infoWindowRect.top < parentRect.top) {
                scrollY = Math.abs(scrollY);
            } else {
                scrollY = -Math.abs(scrollY);
            }
        }

        final CameraUpdate cameraUpdate = CameraUpdateFactory.scrollBy(scrollX, scrollY);
        googleMap.animateCamera(cameraUpdate, DURATION_CAMERA_ENSURE_VISIBLE_ANIMATION, null);

        return visible;
    }

    private void removeWindow(@NonNull final InfoWindow window, @NonNull final View container) {

        container.setVisibility(View.INVISIBLE);
        container.setScaleY(1f);
        container.setScaleX(1f);
        container.clearAnimation();

        removeWindowFragment(window.getFragment());
    }

    private void addFragment(@NonNull final Fragment fragment, @NonNull final View container) {
        fragmentManager.beginTransaction()
                .replace(container.getId(), fragment, FRAGMENT_TAG_INFO)
                .commitNow();
    }

    private void removeWindowFragment(final Fragment windowFragment) {
        fragmentManager.beginTransaction()
                .remove(windowFragment)
                .commitNow();
    }

    /**
     * Generate default {@link ContainerSpecification} for the container view.
     *
     * @return New instance of the generated default container specs.
     */
    public ContainerSpecification generateDefaultContainerSpecs(Context context) {
        final Drawable drawable =
                ContextCompat.getDrawable(context, R.drawable.infowindow_background);

        return new ContainerSpecification(drawable);
    }

    private boolean isOpen() {
        return currentContainer != null && currentContainer.getVisibility() == View.VISIBLE;
    }

    /**
     * Set a callback which will be invoked when an {@link InfoWindow} is changing its state.
     *
     * @param windowShowListener The callback that will run.
     * @see WindowShowListener
     */
    public void setWindowShowListener(WindowShowListener windowShowListener) {
        this.windowShowListener = windowShowListener;
    }

    private void setCurrentWindow(InfoWindow currentWindow) {
        this.currentWindow = currentWindow;
    }

    /**
     * Set the container specifications. These specifications are global for all
     * {@link InfoWindow}s.
     *
     * @param containerSpec The container specifications used for the InfoWindow container view.
     */
    public void setContainerSpec(ContainerSpecification containerSpec) {
        this.containerSpec = containerSpec;
    }

    /**
     * Get the specification of the {@link InfoWindow}'s container.
     *
     * @return {@link InfoWindow}'s container specification.
     * @see ContainerSpecification
     */
    public ContainerSpecification getContainerSpec() {
        return containerSpec;
    }

    private class FragmentContainerIdProvider {
        private final static String BUNDLE_KEY_ID = "BundleKeyFragmentContainerIdProvider";
        private int currentId;

        public FragmentContainerIdProvider(@Nullable final Bundle savedInstanceState) {
            if (savedInstanceState != null) {
                currentId = savedInstanceState.getInt(BUNDLE_KEY_ID, R.id.infoWindowContainer1);
            } else {
                currentId = R.id.infoWindowContainer1;
            }
        }

        public int getCurrentId() {
            return currentId;
        }

        public int getNewId() {
            if (currentId == R.id.infoWindowContainer1) {
                currentId = R.id.infoWindowContainer2;
            } else {
                currentId = R.id.infoWindowContainer1;
            }

            return currentId;
        }

        public void onSaveInstanceState(@NonNull final Bundle outState) {
            outState.putInt(BUNDLE_KEY_ID, currentId);
        }
    }

    /**
     * This method must be called from activity's or fragment's onSaveInstanceState(Bundle outState).
     * There is no need of calling this method if you are using
     * {@link com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment}
     *
     * @param outState Bundle from activity's of fragment's onSaveInstanceState(Bundle outState).
     */
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        idProvider.onSaveInstanceState(outState);
    }

    /**
     * This method must be called from activity's or fragment's onDestroy().
     * There is no need of calling this method if you are using
     * {@link com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment}
     */
    public void onDestroy() {

        currentContainer = null;
        parent = null;

    }

    /**
     * Call this method in your onMapReady(GoogleMap googleMap) callback if you are not using
     * {@link com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment}.
     * <p>
     * <p>Keep in mind that this method sets all camera listeners and map click listener
     * to the googleMap object and you shouldn't set them by yourself. However if you want
     * to listen for these events you can use the methods below: <br></p>
     * <p>
     * {@link #setOnCameraMoveStartedListener(GoogleMap.OnCameraMoveStartedListener)}
     * <br>
     * {@link #setOnCameraMoveCanceledListener(GoogleMap.OnCameraMoveCanceledListener)}
     * <br>
     * {@link #setOnCameraMoveListener(GoogleMap.OnCameraMoveListener)}
     * <br>
     * {@link #setOnCameraIdleListener(GoogleMap.OnCameraIdleListener)}
     *
     * @param googleMap The GoogleMap object from onMapReady callback.
     * @see #setOnMapClickListener(GoogleMap.OnMapClickListener)
     * @see #setOnCameraMoveStartedListener(GoogleMap.OnCameraMoveStartedListener)
     * @see #setOnCameraMoveCanceledListener(GoogleMap.OnCameraMoveCanceledListener)
     * @see #setOnCameraMoveListener(GoogleMap.OnCameraMoveListener)
     * @see #setOnCameraIdleListener(GoogleMap.OnCameraIdleListener)
     */
    public void onMapReady(@NonNull final GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.setOnMapClickListener(this);

        googleMap.setOnCameraIdleListener(this);
        googleMap.setOnCameraMoveStartedListener(this);
        googleMap.setOnCameraMoveListener(this);
        googleMap.setOnCameraMoveCanceledListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (onMapClickListener != null) {
            onMapClickListener.onMapClick(latLng);
        }

        if (isOpen()) {
            internalHide(currentContainer, currentWindow);
        }

    }

    @Override
    public void onCameraIdle() {
        if (onCameraIdleListener != null) {
            onCameraIdleListener.onCameraIdle();
        }
    }

    @Override
    public void onCameraMoveStarted(int i) {
        if (onCameraMoveStartedListener != null) {
            onCameraMoveStartedListener.onCameraMoveStarted(i);
        }
    }

    @Override
    public void onCameraMove() {
        if (onCameraMoveListener != null) {
            onCameraMoveListener.onCameraMove();
        }

        if (isOpen()) {
            centerInfoWindow(currentWindow, currentContainer);
        }
    }

    @Override
    public void onCameraMoveCanceled() {
        if (onCameraMoveCanceledListener != null) {
            onCameraMoveCanceledListener.onCameraMoveCanceled();
        }
    }

    /**
     * Set onMapClickListener.
     *
     * @param onMapClickListener The callback that will run.
     */
    public void setOnMapClickListener(GoogleMap.OnMapClickListener onMapClickListener) {

        this.onMapClickListener = onMapClickListener;
    }

    /**
     * Set onCameraIdleListener.
     *
     * @param onCameraIdleListener The callback that will run.
     */
    public void setOnCameraIdleListener(GoogleMap.OnCameraIdleListener onCameraIdleListener) {

        this.onCameraIdleListener = onCameraIdleListener;
    }

    /**
     * Set onCameraMoveStartedListener.
     *
     * @param onCameraMoveStartedListener The callback that will run.
     */
    public void setOnCameraMoveStartedListener(
            final GoogleMap.OnCameraMoveStartedListener onCameraMoveStartedListener) {

        this.onCameraMoveStartedListener = onCameraMoveStartedListener;
    }

    /**
     * Set onCameraMoveListener
     *
     * @param onCameraMoveListener The callback that will run.
     */
    public void setOnCameraMoveListener(final GoogleMap.OnCameraMoveListener onCameraMoveListener) {

        this.onCameraMoveListener = onCameraMoveListener;
    }

    /**
     * Set onCameraMoveCanceledListener.
     *
     * @param onCameraMoveCanceledListener The callback that will run.
     */
    public void setOnCameraMoveCanceledListener(
            final GoogleMap.OnCameraMoveCanceledListener onCameraMoveCanceledListener) {

        this.onCameraMoveCanceledListener = onCameraMoveCanceledListener;
    }

    /**
     * Provide your own animation for showing the {@link InfoWindow}.
     *
     * @param showAnimation Show animation.
     */
    public void setShowAnimation(Animation showAnimation) {
        this.showAnimation = showAnimation;
    }

    /**
     * Provide your own animation for hiding the {@link InfoWindow}.
     *
     * @param hideAnimation Hide animation.
     */
    public void setHideAnimation(Animation hideAnimation) {
        this.hideAnimation = hideAnimation;
    }

    /**
     * Determine whether your {@link InfoWindow} should be closed after the user flings the map
     * or should move with it.
     *
     * @param hideOnFling Pass <code>true</code> if you want to hide your {@link InfoWindow}
     *                    when fling event occurs, pass <code>false</code> if you want your window
     *                    to move with the map.
     */
    public void setHideOnFling(final boolean hideOnFling) {
        this.hideOnFling = hideOnFling;
    }

    /**
     * Interface definition for callbacks to be invoked when an {@link InfoWindow}'s
     * state has been changed.
     */
    public interface WindowShowListener {
        void onWindowShowStarted(@NonNull final InfoWindow infoWindow);

        void onWindowShown(@NonNull final InfoWindow infoWindow);

        void onWindowHideStarted(@NonNull final InfoWindow infoWindow);

        void onWindowHidden(@NonNull final InfoWindow infoWindow);
    }

    /**
     * Class containing {@link InfoWindow}'s container details.
     */
    public static class ContainerSpecification {
        private Drawable background;

        public ContainerSpecification(Drawable background) {
            this.background = background;
        }

        public Drawable getBackground() {
            return background;
        }

        public void setBackground(Drawable background) {
            this.background = background;
        }
    }
}
