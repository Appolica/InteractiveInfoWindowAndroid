# InteractiveInfoWindowAndroid

InteractiveInfoWindowAndroid is (suprisingly :D) an Android library which gives you the opportunity to show interactive info windows on your google map. The UI of yout window is encapsulated in your own fragment with its own lifecycle. You just pass it to the InfoWindowManager and display it above whichever marker you want.

## How to use?

First you need to add our map fragment to your layout. It embeds the SupportMapFragment and all the magic which makes this library to work and provides its API.

```xml
  <RelativeLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    ...
    
    <fragment
      android:id="@+id/infoWindowMap"
      android:layout_width="match_parent"
      android:layout_height="match_parent"
      android:name="com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment"/>

  </RelativeLayout>
```

Then you obtain an instance of it and basically you are ready to go:

```java
  final MapInfoWindowFragment mapInfoWindowFragment =
        (MapInfoWindowFragment) getSupportFragmentManager().findFragmentById(R.id.infoWindowMap);

  final InfoWindow infoWindow = new InfoWindow(marker, markerSpec, fragment);
  // Shows the InfoWindow or hides it if it is already opened.
  mapInfoWindowFragment.infoWindowManager().toggle(infoWindow, true); 
```
Listen when an InfoWindow is hiding or showing by implementing InfoWindowManager.WindowShowListener:

```java
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
```
You can get an instance of the GoogleMap by calling ```mapInfoWindowFragment.getMapAsync(onMapReadyCallback)```.

_If you don't want to use our fragment it's okay but it's not that straight-forward. You have to bind the InfoWindowManager to your Activity/Fragment and add TouchInterceptFrameLayout as a parent of your map. Take a look at [this example](https://github.com/Appolica/InteractiveInfoWindowAndroid/blob/develop/sample/src/main/java/com/appolica/sample/activities/SampleWithMapViewActivity.java)._

## API

### MapInfoWindowFragment:

 * ```public void getMapAsync(OnMapReadyCallback onMapReadyCallback)``` - Use this method to get the GoogleMap object asynchronously from our fragment.
 * ```public InfoWindowManager infoWindowManager()``` - Get an instance of InfoWindowManager to control your InfoWindow.

### InfoWindowManager:
 * ```public void toggle(@NonNull final InfoWindow infoWindow, final boolean animated)``` - Shows or hides the given InfoWindow with or without animation.
 * ```public void show(@NonNull final InfoWindow infoWindow, final boolean animated)``` - Shows the given InfoWindow with or without animation.
 * ```public void hide(@NonNull final InfoWindow infoWindow, final boolean animated)``` - Hides the given InfoWindow with or without animation.
 * ```public void setWindowShowListener(WindowShowListener windowShowListener)``` - Listen for InfoWindow show/hide events.
 * ```public void setShowAnimation(Animation showAnimation)``` - Set your own InfoWindow show animation.
 * ```public void setHideAnimation(Animation hideAnimation)``` - Set your own InfoWindow hide animation.
 
The following listener settters are a copy of GoogleMap's setters. Use these methods instead of the original ones.
 
 * ```public void setOnMapClickListener(GoogleMap.OnMapClickListener onMapClickListener)```
 * ```public void setOnCameraIdleListener(GoogleMap.OnCameraIdleListener onCameraIdleListener)```
 * ```public void setOnCameraMoveStartedListener(final GoogleMap.OnCameraMoveStartedListener onCameraMoveStartedListener)```
 * ```public void setOnCameraMoveListener(final GoogleMap.OnCameraMoveListener onCameraMoveListener)```
 * ```public void setOnCameraMoveCanceledListener(final GoogleMap.OnCameraMoveCanceledListener onCameraMoveCanceledListener)```



## Known issues

The InfoWindow lags when you fling the map. Wea are open to suggestions how to fix this.
