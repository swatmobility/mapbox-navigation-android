package com.mapbox.navigation.qa_test_app.car

import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.extension.androidauto.DefaultMapboxCarMapGestureHandler
import com.mapbox.maps.extension.androidauto.MapboxCarMapObserver
import com.mapbox.maps.extension.androidauto.MapboxCarMapSurface
import com.mapbox.navigation.dropin.component.camera.CameraAction
import com.mapbox.navigation.dropin.component.camera.CameraViewModel

/**
 * Controller class to handle map camera changes.
 */
@OptIn(MapboxExperimental::class)
class CameraIdleMapGestureHandler(
    private val carCameraViewModel: CameraViewModel
) : DefaultMapboxCarMapGestureHandler() {

    override fun onScroll(
      mapboxCarMapSurface: MapboxCarMapSurface,
      visibleCenter: ScreenCoordinate,
      distanceX: Float,
      distanceY: Float
    ) {
        carCameraViewModel.invoke(CameraAction.ToIdle)
        super.onScroll(mapboxCarMapSurface, visibleCenter, distanceX, distanceY)
    }
}
