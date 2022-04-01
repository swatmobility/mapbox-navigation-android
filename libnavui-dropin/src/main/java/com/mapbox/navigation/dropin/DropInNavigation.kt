package com.mapbox.navigation.dropin

import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.dropin.component.audioguidance.AudioGuidanceViewModel
import com.mapbox.navigation.dropin.component.camera.CameraViewModel
import com.mapbox.navigation.dropin.component.destination.DestinationViewModel
import com.mapbox.navigation.dropin.component.location.LocationViewModel
import com.mapbox.navigation.dropin.component.navigation.NavigationState
import com.mapbox.navigation.dropin.component.navigation.NavigationStateViewModel
import com.mapbox.navigation.dropin.component.routefetch.RoutesViewModel
import com.mapbox.navigation.dropin.component.tripsession.TripSessionStarterViewModel
import com.mapbox.navigation.dropin.extensions.attachCreated

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class DropInNavigation private constructor() : MapboxNavigationObserver {

    val navigationStateViewModel = NavigationStateViewModel(NavigationState.FreeDrive)
    val locationViewModel = LocationViewModel()
    val tripSessionStarterViewModel = TripSessionStarterViewModel(navigationStateViewModel)
    val audioGuidanceViewModel = AudioGuidanceViewModel(navigationStateViewModel)
    val cameraViewModel = CameraViewModel()
    val destinationViewModel = DestinationViewModel()
    val routesViewModel = RoutesViewModel()
    private val navigationObservers = arrayOf(
        destinationViewModel,
        tripSessionStarterViewModel,
        audioGuidanceViewModel,
        locationViewModel,
        routesViewModel,
        cameraViewModel,
        navigationStateViewModel,
        // TODO can add more mapbox navigation observers here
    )

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        navigationObservers.forEach { it.onAttached(mapboxNavigation) }
    }

    override fun onDetached(mapboxNavigation: MapboxNavigation) {
        navigationObservers.reversed().forEach { it.onDetached(mapboxNavigation) }
    }

    companion object {
        private val LOCK = Object()

        /**
         * @throws IllegalStateException before [MapboxNavigationApp.setup] has been called, and
         * at least one lifecycle has been [MapboxNavigationApp.attach] and created.
         *
         * A guaranteed way to not cause an [IllegalStateException] is to access the instance
         * through [MapboxNavigationObserver.onAttached].
         */
        fun getInstance(): DropInNavigation {
            synchronized(LOCK) {
                return try {
                    MapboxNavigationApp.getObserver(DropInNavigation::class)
                } catch (e: IllegalStateException) {
                    val navigationState = DropInNavigation()
                    MapboxNavigationApp.lifecycleOwner.attachCreated(navigationState)
                    MapboxNavigationApp.getObserver(DropInNavigation::class)
                }
            }
        }
    }
}
