package com.mapbox.navigation.dropin

import androidx.lifecycle.ViewModel
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.dropin.component.camera.CameraViewModel
import com.mapbox.navigation.dropin.component.location.LocationViewModel
import com.mapbox.navigation.dropin.component.navigationstate.NavigationState
import com.mapbox.navigation.dropin.component.replay.ReplayViewModel
import com.mapbox.navigation.dropin.component.routefetch.RoutesViewModel
import com.mapbox.navigation.dropin.component.sound.MapboxAudioViewModel
import com.mapbox.navigation.dropin.model.Destination
import com.mapbox.navigation.utils.internal.logD
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * There is a single ViewModel for the navigation view. Use this class to store state that should
 * survive orientation changes.
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
internal class DropInNavigationViewModel : ViewModel() {
    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.FreeDrive)
    val navigationState = _navigationState.asStateFlow()

    private val _destination = MutableStateFlow<Destination?>(null)
    val destination = _destination.asStateFlow()

    @Suppress("PropertyName")
    val _activeNavigationStarted = MutableStateFlow(false)
    val activeNavigationStarted = _activeNavigationStarted.asStateFlow()

    private val _onBackPressedEvent = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val onBackPressedEvent = _onBackPressedEvent.asSharedFlow()

    /**
     * These classes are accessible through MapboxNavigationApp.getObserver(..)
     */
    val replayViewModel = ReplayViewModel()
    val audioGuidanceViewModel = MapboxAudioViewModel()
    val locationViewModel = LocationViewModel()
    val routesViewModel = RoutesViewModel()
    val cameraViewModel = CameraViewModel()
    val navigationObservers = listOf(
        replayViewModel,
        audioGuidanceViewModel,
        locationViewModel,
        routesViewModel,
        cameraViewModel,
        // TODO can add more mapbox navigation observers here
    )

    fun updateState(state: NavigationState) {
        if (_navigationState.value == state) return

        logD(
            this.javaClass.simpleName,
            "navigationState: ${_navigationState.value} -> $state"
        )
        _navigationState.value = state
    }

    fun updateDestination(destination: Destination?) {
        _destination.value = destination
    }

    fun onBackPressed() {
        _onBackPressedEvent.tryEmit(Unit)
    }

    init {
        navigationObservers.forEach { MapboxNavigationApp.registerObserver(it) }
    }

    override fun onCleared() {
        navigationObservers.reversed().forEach { MapboxNavigationApp.unregisterObserver(it) }
    }
}
