package com.mapbox.androidauto.car.navigation.speedlimit

import android.graphics.Rect
import android.location.Location
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.examples.androidauto.car.MainCarContext
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.androidauto.MapboxCarMapObserver
import com.mapbox.maps.extension.androidauto.MapboxCarMapSurface
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.speedlimit.api.MapboxSpeedLimitApi
import com.mapbox.navigation.ui.speedlimit.model.SpeedLimitFormatter

/**
 * Create a speed limit sign. This class is demonstrating how to
 * create a renderer. To Create a new speed limit sign experience, try creating a new class.
 */
@OptIn(MapboxExperimental::class)
class CarSpeedLimitRenderer(
    private val mainCarContext: MainCarContext,
) : MapboxCarMapObserver {
    private val speedLimitWidget by lazy { SpeedLimitWidget() }

    private val speedLimitFormatter: SpeedLimitFormatter by lazy {
        SpeedLimitFormatter(mainCarContext.carContext)
    }
    private val speedLimitApi: MapboxSpeedLimitApi by lazy {
        MapboxSpeedLimitApi(speedLimitFormatter)
    }

    private val locationObserver = object : LocationObserver {

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val value = speedLimitApi.updateSpeedLimit(locationMatcherResult.speedLimit)
            speedLimitWidget.update(value)
        }

        override fun onNewRawLocation(rawLocation: Location) {
            // no op
        }
    }

    override fun onAttached(mapboxCarMapSurface: MapboxCarMapSurface) {
        logAndroidAuto("CarSpeedLimitRenderer carMapSurface loaded")
        mapboxCarMapSurface.mapSurface.getMapboxMap().getStyle { style ->
            style.addPersistentStyleCustomLayer(
                SpeedLimitWidget.SPEED_LIMIT_WIDGET_LAYER_ID,
                speedLimitWidget.viewWidgetHost,
                null
            )
        }
        MapboxNavigationProvider.retrieve().registerLocationObserver(locationObserver)
    }

    override fun onDetached(mapboxCarMapSurface: MapboxCarMapSurface) {
        logAndroidAuto("CarSpeedLimitRenderer carMapSurface detached")
        MapboxNavigationProvider.retrieve().unregisterLocationObserver(locationObserver)
        mapboxCarMapSurface.mapSurface.getMapboxMap().getStyle()
            ?.removeStyleLayer(SpeedLimitWidget.SPEED_LIMIT_WIDGET_LAYER_ID)
        speedLimitWidget.clear()
    }

    override fun onVisibleAreaChanged(visibleArea: Rect, edgeInsets: EdgeInsets) {
        super.onVisibleAreaChanged(visibleArea, edgeInsets)
        if (edgeInsets.right > 0) {
            speedLimitWidget.setVisible(false)
        } else {
            speedLimitWidget.setVisible(true)
        }
    }
}
