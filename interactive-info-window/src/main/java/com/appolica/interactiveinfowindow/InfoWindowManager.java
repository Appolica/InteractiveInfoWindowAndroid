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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;

public class InfoWindowManager
        implements GoogleMap.OnCameraIdleListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnMapClickListener {

    public static final String FRAGMENT_TAG_INFO = "InfoWindow";
    public static final int WINDOW_ANIMATION_DURATION = 300;

    private GoogleMap googleMap;

    private FragmentManager fragmentManager;

    private InfoWindow infoWindow;
    private ViewGroup parent;
    private View currentWindowContainer;

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

    public InfoWindowManager(@NonNull final FragmentManager fragmentManager) {

        this.fragmentManager = fragmentManager;
    }

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
                                    centerInfoWindow(infoWindow);
                                }

                                return true;
                            }

                            @Override
                            public boolean onFling(
                                    MotionEvent e1, MotionEvent e2,
                                    float velocityX, float velocityY) {

                                if (isOpen()) {
                                    centerInfoWindow(infoWindow);
                                }

                                return true;
                            }

                            @Override
                            public boolean onDoubleTap(MotionEvent e) {

                                if (isOpen()) {
                                    centerInfoWindow(infoWindow);
                                }

                                return true;
                            }
                        }));


        currentWindowContainer = parent.findViewById(idProvider.currentId);

        if (currentWindowContainer == null) {
            currentWindowContainer = createContainerView(parent);

            parent.addView(currentWindowContainer);
        }

    }

    private View createContainerView(@NonNull final ViewGroup parent) {
        final LinearLayout container = new LinearLayout(parent.getContext());

        container.setBackground(containerSpec.background);
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

    public void toggle(@NonNull final InfoWindow infoWindow) {
        toggle(infoWindow, true);
    }

    public void toggle(@NonNull final InfoWindow infoWindow, final boolean animated) {

        if (isOpen()) {
            // If the toggled window is tha same as the already opened one, close it.
            // Otherwise close the currently opened window and open the new one.
            if (infoWindow.equals(this.infoWindow)) {
                hide(infoWindow, animated);
            } else {
                hide(this.infoWindow, animated);
                show(infoWindow, animated);
            }

        } else {
            show(infoWindow, animated);
        }

    }

    public void show(@NonNull final InfoWindow infoWindow) {
        show(infoWindow, true);
    }

    public void show(@NonNull final InfoWindow infoWindow, final boolean animated) {
        final InfoWindow oldWindow = this.infoWindow;

        setInfoWindow(infoWindow);

        final Fragment currentWindowFragment =
                fragmentManager.findFragmentById(idProvider.currentId);

        if (currentWindowFragment != null) {

            final View oldContainer = currentWindowContainer;

            internalHide(oldContainer, oldWindow);

            currentWindowContainer = createContainerView(parent);

            parent.addView(currentWindowContainer);
        }

        internalShow(infoWindow, animated);
    }

    private void internalShow(@NonNull final InfoWindow infoWindow, final boolean animated) {

        addWindowFragment(infoWindow.getWindowFragment());

        centerInfoWindow(infoWindow);

        parent.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {

                ensureVisible(currentWindowContainer);

                parent.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });

        if (animated) {

            animateWindowOpen(infoWindow);

        } else {

            currentWindowContainer.setVisibility(View.VISIBLE);

        }
    }

    private void animateWindowOpen(@NonNull final InfoWindow infoWindow) {

        final SimpleAnimationListener animationListener = new SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                currentWindowContainer.setVisibility(View.VISIBLE);
                propagateShowEvent(infoWindow, InfoWindow.WindowState.SHOWING);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                propagateShowEvent(infoWindow, InfoWindow.WindowState.SHOWN);
                setInfoWindow(infoWindow);
            }
        };

        if (showAnimation == null) {

            currentWindowContainer.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {

                        @Override
                        public boolean onPreDraw() {
                            final int containerWidth = currentWindowContainer.getWidth();
                            final int containerHeight = currentWindowContainer.getHeight();

                            final float pivotX = currentWindowContainer.getX() + containerWidth / 2;
                            final float pivotY = currentWindowContainer.getY() + containerHeight;

                            final ScaleAnimation scaleAnimation = new ScaleAnimation(
                                    0f, 1f,
                                    0f, 1f,
                                    pivotX, pivotY);

                            scaleAnimation.setDuration(WINDOW_ANIMATION_DURATION);
                            scaleAnimation.setInterpolator(new DecelerateInterpolator());
                            scaleAnimation.setAnimationListener(animationListener);

                            currentWindowContainer.startAnimation(scaleAnimation);

                            currentWindowContainer.getViewTreeObserver().removeOnPreDrawListener(this);

                            return true;
                        }
                    });
        } else {
            showAnimation.setAnimationListener(animationListener);
            currentWindowContainer.startAnimation(showAnimation);
        }
    }

    public void hide(@NonNull final InfoWindow infoWindow) {
        hide(infoWindow, true);
    }

    public void hide(@NonNull final InfoWindow infoWindow, final boolean animated) {
        internalHide(currentWindowContainer, infoWindow, animated);
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

                final int containerWidth = currentWindowContainer.getWidth();
                final int containerHeight = currentWindowContainer.getHeight();

                final float pivotX = currentWindowContainer.getX() + containerWidth / 2;
                final float pivotY = currentWindowContainer.getY() + containerHeight;

                animation = new ScaleAnimation(
                        1f, 0f,
                        1f, 0f,
                        pivotX, pivotY);

                animation.setDuration(WINDOW_ANIMATION_DURATION);
                animation.setInterpolator(new DecelerateInterpolator());


            } else {
                animation = hideAnimation;
            }

            animation.setAnimationListener(new SimpleAnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    toHideWindow.setWindowState(InfoWindow.WindowState.HIDING);
                    propagateShowEvent(toHideWindow, InfoWindow.WindowState.HIDING);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    removeWindow(container);

                    if (container.getId() != currentWindowContainer.getId()) {
                        parent.removeView(container);
                    }

                    toHideWindow.setWindowState(InfoWindow.WindowState.HIDDEN);
                    propagateShowEvent(toHideWindow, InfoWindow.WindowState.HIDDEN);
                }
            });

            currentWindowContainer.startAnimation(animation);

        } else {

            removeWindow(container);
            propagateShowEvent(toHideWindow, InfoWindow.WindowState.HIDDEN);

        }
    }

    private void propagateShowEvent(
            @NonNull final InfoWindow infoWindow,
            @NonNull final InfoWindow.WindowState state) {

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

    private void centerInfoWindow(@NonNull final InfoWindow infoWindow) {
        final Projection projection = googleMap.getProjection();
        final Point screenLocation = projection.toScreenLocation(infoWindow.getMarker().getPosition());

        currentWindowContainer.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {

                    @Override
                    public boolean onPreDraw() {

                        currentWindowContainer.getViewTreeObserver().removeOnPreDrawListener(this);

                        final int containerWidth = currentWindowContainer.getWidth();
                        final int containerHeight = currentWindowContainer.getHeight();

                        final int x = screenLocation.x - containerWidth / 2;
                        final int y = screenLocation.y - containerHeight - infoWindow.getMarkerSpec().getHeight();

                        final int pivotX = containerWidth / 2;
                        final int pivotY = containerHeight;

                        currentWindowContainer.setPivotX(pivotX);
                        currentWindowContainer.setPivotY(pivotY);

                        currentWindowContainer.setX(x);
                        currentWindowContainer.setY(y);

                        return true;
                    }
                });
    }

    private boolean ensureVisible(@NonNull final  View infoWindowContainer) {

        final int[] infoWindowLocation = new int[2];
        infoWindowContainer.getLocationOnScreen(infoWindowLocation);

        final String infoWindowPosition =
                String.format(
                        "InfoWindow x: %d, y: %d",
                        infoWindowLocation[0],
                        infoWindowLocation[1]
                );

        Log.d("InfoWidow", infoWindowPosition);


        final boolean visible = true;
        final Rect infoWindowRect = new Rect();
        infoWindowContainer.getHitRect(infoWindowRect);

        final int[] parentPosition = new int[2];
        parent.getLocationOnScreen(parentPosition);

        final Rect parentRect = new Rect();
        parent.getGlobalVisibleRect(parentRect);

        System.out.println(parentRect.toString());

        infoWindowContainer.getGlobalVisibleRect(infoWindowRect);

        final int visibleWidth = infoWindowRect.width();
        final int actualWidth = infoWindowContainer.getWidth();


        final int visibleHeight = infoWindowRect.height();
        final int actualHeight = infoWindowContainer.getHeight();

        int scrollX = (visibleWidth - actualWidth);
        int scrollY = (visibleHeight - actualHeight);

        Log.d("InvisibleWidth", scrollX + "");
        Log.d("InvisibleHeight", scrollY + "");

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

        googleMap.animateCamera(CameraUpdateFactory.scrollBy(scrollX, scrollY));


        if (infoWindowContainer.getLocalVisibleRect(parentRect)) {
            Log.d("InfoWindow", "FullyVisible");
        } else {
            Log.d("InfoWindow", "NotVisible");
        }

        System.out.println(parentRect.toString());


        if (infoWindowContainer.getRight() > parent.getRight()) {
            scrollX = parent.getRight() - infoWindowContainer.getRight();
        }

        if (infoWindowContainer.getLeft() < parent.getLeft()) {
//            scrollX = parent.getLeft()currentWindowContainer.getLeft()
        }


        return visible;
    }

    private void removeWindow(@NonNull final View container) {

        final Fragment windowFragment = fragmentManager.findFragmentById(container.getId());

        container.setVisibility(View.INVISIBLE);

        container.setScaleY(1f);
        container.setScaleX(1f);
        container.clearAnimation();

        if (windowFragment != null) {
            removeWindowFragment(windowFragment);
        }
    }

    void addWindowFragment(@NonNull final Fragment windowFragment) {
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(
                currentWindowContainer.getId(),
                windowFragment,
                FRAGMENT_TAG_INFO
        ).commit();

        fragmentManager.executePendingTransactions();
    }

    private void removeWindowFragment(final Fragment windowFragment) {
        final FragmentTransaction fragmentTransaction
                = fragmentManager.beginTransaction();

        fragmentTransaction.remove(windowFragment).commit();

        fragmentManager.executePendingTransactions();
    }

    private ContainerSpecification generateDefaultContainerSpecs(Context context) {
        final Drawable drawable =
                ContextCompat.getDrawable(context, R.drawable.infowindow_background);

        return new ContainerSpecification(drawable);
    }

    private boolean isOpen() {
        return currentWindowContainer.getVisibility() == View.VISIBLE;
    }

    public void setWindowShowListener(WindowShowListener windowShowListener) {
        this.windowShowListener = windowShowListener;
    }

    private void setInfoWindow(InfoWindow infoWindow) {
        this.infoWindow = infoWindow;
    }

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

    public void onSaveInstanceState(@NonNull final Bundle outState) {
        idProvider.onSaveInstanceState(outState);
    }

    public void onDestroy() {

        currentWindowContainer = null;
        parent = null;

    }

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
            internalHide(currentWindowContainer, infoWindow);
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
            centerInfoWindow(infoWindow);
        }
    }

    @Override
    public void onCameraMoveCanceled() {
        if (onCameraMoveCanceledListener != null) {
            onCameraMoveCanceledListener.onCameraMoveCanceled();
        }
    }

    public void setOnMapClickListener(GoogleMap.OnMapClickListener onMapClickListener) {

        this.onMapClickListener = onMapClickListener;
    }

    public void setOnCameraIdleListener(GoogleMap.OnCameraIdleListener onCameraIdleListener) {

        this.onCameraIdleListener = onCameraIdleListener;
    }

    public void setOnCameraMoveStartedListener(
            final GoogleMap.OnCameraMoveStartedListener onCameraMoveStartedListener) {

        this.onCameraMoveStartedListener = onCameraMoveStartedListener;
    }

    public void setOnCameraMoveListener(final GoogleMap.OnCameraMoveListener onCameraMoveListener) {

        this.onCameraMoveListener = onCameraMoveListener;
    }

    public void setOnCameraMoveCanceledListener(
            final GoogleMap.OnCameraMoveCanceledListener onCameraMoveCanceledListener) {

        this.onCameraMoveCanceledListener = onCameraMoveCanceledListener;
    }

    public void setShowAnimation(Animation showAnimation) {
        this.showAnimation = showAnimation;
    }

    public void setHideAnimation(Animation hideAnimation) {
        this.hideAnimation = hideAnimation;
    }

    public interface WindowShowListener {
        void onWindowShowStarted(@NonNull final InfoWindow infoWindow);

        void onWindowShown(@NonNull final InfoWindow infoWindow);

        void onWindowHideStarted(@NonNull final InfoWindow infoWindow);

        void onWindowHidden(@NonNull final InfoWindow infoWindow);
    }

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
