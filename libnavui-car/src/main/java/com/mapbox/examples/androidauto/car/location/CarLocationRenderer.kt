package com.mapbox.examples.androidauto.car.location

import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.examples.androidauto.car.MainCarContext
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.androidauto.MapboxCarMapObserver
import com.mapbox.maps.extension.androidauto.MapboxCarMapSurface
import com.mapbox.maps.plugin.delegates.listeners.OnStyleLoadedListener
import com.mapbox.maps.plugin.locationcomponent.location

/**
 * Create a simple 3d location puck. This class is demonstrating how to
 * create a renderer. To Create a new location experience, try creating a new class.
 */
@OptIn(MapboxExperimental::class)
class CarLocationRenderer(
    private val mainCarContext: MainCarContext
) : MapboxCarMapObserver {

    private var mapboxCarMapSurface: MapboxCarMapSurface? = null
    private val styleLoadedListener = OnStyleLoadedListener {
        mapboxCarMapSurface?.setLocationPuck()
    }

    override fun onAttached(mapboxCarMapSurface: MapboxCarMapSurface) {
        this.mapboxCarMapSurface = mapboxCarMapSurface
        mapboxCarMapSurface.mapSurface.getMapboxMap()
            .addOnStyleLoadedListener(styleLoadedListener)
    }

    override fun onDetached(mapboxCarMapSurface: MapboxCarMapSurface) {
        this.mapboxCarMapSurface = null
        mapboxCarMapSurface.mapSurface.getMapboxMap()
            .removeOnStyleLoadedListener(styleLoadedListener)
    }

    private fun MapboxCarMapSurface.setLocationPuck() {
        mapSurface.location.apply {
            locationPuck = CarLocationPuck.navigationPuck2D(mainCarContext.carContext)
            enabled = true
            pulsingEnabled = true
            setLocationProvider(MapboxCarApp.carAppServices.location().navigationLocationProvider)
        }
    }
}
