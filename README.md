
[ ![Download](https://api.bintray.com/packages/appolica-ltd/appolica/InfoWindow/images/download.svg) ](https://bintray.com/appolica-ltd/appolica/InfoWindow/_latestVersion)


# InteractiveInfoWindowAndroid *by Appolica*

InteractiveInfoWindowAndroid is (suprisingly :D) an Android library which gives you the opportunity to show interactive info windows on your google map. The library is developed and maintained by [Appolica](http://www.appolica.com). The UI of your window is encapsulated in your own fragment with its own lifecycle. You just pass it to the InfoWindowManager and display it above whichever marker you want.

![](https://github.com/Appolica/InteractiveInfoWindowAndroid/blob/develop/gifs/Sample1.gif?raw=true) ![](https://github.com/Appolica/InteractiveInfoWindowAndroid/blob/develop/gifs/Sample2.gif?raw=true)


![](https://github.com/Appolica/InteractiveInfoWindowAndroid/blob/develop/gifs/Sample3.gif?raw=true) ![](https://github.com/Appolica/InteractiveInfoWindowAndroid/blob/develop/gifs/Sample4.gif?raw=true)

## How to use?

### Add it to your Android project

```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.appolica:interactive-info-window-android:1.1.0'
}

```

### And then use it in your code

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
 * ```public void setContainerSpec(ContainerSpecification containerSpec)``` - Set the container specification for your InfoWindow container. The ContainerSpecification class wraps (at least for now) only the backgorund drawable of the container view. You can change the background from here.
 
### InfoWindow:
 * ```public InfoWindow(Marker marker, MarkerSpecification markerSpec, Fragment fragment)``` - 
    * marker - The marker which determines the window's position on the screen
    * markerSpec - Provides the marker's offsetX and offsetY
    * fragment - The actual window that is displayed on the screen
    
 * ```public InfoWindow(LatLng position, MarkerSpecification markerSpec, Fragment fragment)``` - 
    * position - The LatLng position which determines the window's position on the screen.
    * markerSpec - Provides the marker's offsetX and offsetY
    * fragment - The actual window that is displayed on the screen

### InfoWindow.MarkerSpecification:
 * ```public MarkerSpecification(int offsetX, int offsetY)``` - Create marker specification by providing InfoWindow's x and y offsets from marker's screen location.
    
    Note: By default offsetX will be ignored, so in order for it to take effect, you must call setCenterByX(false). 
    
    Also if you want to use dp, you should convert the values to px by yourself. The constructor expects absolute pixel values.

### The following listener settters are a copy of GoogleMap's setters. Use these methods instead of the original ones.
 
 * ```public void setOnMapClickListener(GoogleMap.OnMapClickListener onMapClickListener)```
 * ```public void setOnCameraIdleListener(GoogleMap.OnCameraIdleListener onCameraIdleListener)```
 * ```public void setOnCameraMoveStartedListener(final GoogleMap.OnCameraMoveStartedListener onCameraMoveStartedListener)```
 * ```public void setOnCameraMoveListener(final GoogleMap.OnCameraMoveListener onCameraMoveListener)```
 * ```public void setOnCameraMoveCanceledListener(final GoogleMap.OnCameraMoveCanceledListener onCameraMoveCanceledListener)```

## Known issues

The InfoWindow lags when you fling the map. However if you want to hide it instead of moving it with the map, you can use ```InfoWindowManager.setHideOnFling(final boolean hideOnFling)```.
We are open to suggestions how to fix this.

# License

Copyright 2016 Appolica Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
