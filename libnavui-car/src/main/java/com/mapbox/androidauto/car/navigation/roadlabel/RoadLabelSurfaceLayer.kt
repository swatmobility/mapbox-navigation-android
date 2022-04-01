package com.mapbox.androidauto.car.navigation.roadlabel

import android.graphics.Color
import androidx.car.app.CarContext
import androidx.car.app.Screen
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.androidauto.logAndroidAutoFailure
import com.mapbox.androidauto.surfacelayer.CarSurfaceLayer
import com.mapbox.androidauto.surfacelayer.textview.CarTextLayerHost
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.androidauto.MapboxCarMapSurface
import com.mapbox.maps.plugin.delegates.listeners.OnStyleLoadedListener
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.trip.model.eh.RoadName
import com.mapbox.navigation.core.MapboxNavigation

/**
 * This will show the current road name at the bottom center of the screen.
 *
 * In your [Screen], create an instance of this class and enable by
 * registering it to the [MapboxCarMap.registerObserver]. Disable by
 * removing the listener with [MapboxCarMap.unregisterObserver].
 */
@OptIn(MapboxExperimental::class, ExperimentalPreviewMapboxNavigationAPI::class)
class RoadLabelSurfaceLayer(
    val carContext: CarContext,
    val mapboxNavigation: MapboxNavigation
) : CarSurfaceLayer() {

    private val roadLabelRenderer = RoadLabelRenderer()
    private val carTextLayerHost = CarTextLayerHost()

    private val styleLoadedListener = OnStyleLoadedListener {
        render(roadNameObserver.currentRoadName)
    }

    private val roadNameObserver = object : RoadNameObserver(mapboxNavigation) {
        override fun onRoadUpdate(currentRoadName: RoadName?) {
            render(currentRoadName)
        }
    }

    override fun children() = listOf(carTextLayerHost.mapScene)

    override fun onAttached(mapboxCarMapSurface: MapboxCarMapSurface) {
        logAndroidAuto("RoadLabelSurfaceLayer carMapSurface loaded")
        super.onAttached(mapboxCarMapSurface)

        val mapboxMap = mapboxCarMapSurface.mapSurface.getMapboxMap()
        mapboxMap.getStyle { style ->
            style.addPersistentStyleCustomLayer(
                layerId = CAR_NAVIGATION_VIEW_LAYER_ID,
                carTextLayerHost,
                null,
            ).error?.let {
                logAndroidAutoFailure("Add custom layer exception $it")
            }

            mapboxNavigation.registerEHorizonObserver(roadNameObserver)
            mapboxMap.addOnStyleLoadedListener(styleLoadedListener)
        }
    }

    private fun render(currentRoadName: RoadName?) {
        val bitmap = roadLabelRenderer.render(currentRoadName?.name, roadLabelOptions())
        carTextLayerHost.offerBitmap(bitmap)
        mapboxCarMapSurface?.mapSurface?.getMapboxMap()
            ?.removeOnStyleLoadedListener(styleLoadedListener)
    }

    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    override fun onDetached(mapboxCarMapSurface: MapboxCarMapSurface) {
        mapboxCarMapSurface.mapSurface.getMapboxMap()
            .removeOnStyleLoadedListener(styleLoadedListener)

        logAndroidAuto("RoadLabelSurfaceLayer carMapSurface detached")
        mapboxCarMapSurface.mapSurface.getMapboxMap().getStyle()
            ?.removeStyleLayer(CAR_NAVIGATION_VIEW_LAYER_ID)
        mapboxNavigation.unregisterEHorizonObserver(roadNameObserver)
        super.onDetached(mapboxCarMapSurface)
    }

    private fun roadLabelOptions(): RoadLabelOptions =
        if (carContext.isDarkMode) {
            DARK_OPTIONS
        } else {
            LIGHT_OPTIONS
        }

    private companion object {
        private const val CAR_NAVIGATION_VIEW_LAYER_ID = "car_road_label_layer_id"

        private val DARK_OPTIONS = RoadLabelOptions.Builder()
            .shadowColor(null)
            .roundedLabelColor(Color.BLACK)
            .textColor(Color.WHITE)
            .build()

        private val LIGHT_OPTIONS = RoadLabelOptions.Builder()
            .roundedLabelColor(Color.WHITE)
            .textColor(Color.BLACK)
            .build()
    }
}
