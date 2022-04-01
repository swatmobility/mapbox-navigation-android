package com.mapbox.navigation.dropin

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp

/**
 * There is a single ViewModel for the navigation view. Use this class to store state that should
 * survive orientation changes.
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
internal class DropInNavigationViewModel : ViewModel() {

    /**
     * LifecycleOwner available for attaching events to a lifecycle that will survive configuration
     * changes. This is only available to the [DropInNavigationViewModel] for now. We can consider
     * exposing the LifecycleOwner to downstream components, but we do not have a use for it yet.
     */
    private val viewModelLifecycleOwner = DropInViewModelLifecycleOwner()

    private val dropInNavigationState by lazy { DropInNavigation.getInstance() }

    val navigationStateViewModel by lazy { dropInNavigationState.navigationStateViewModel }
    val locationViewModel by lazy { dropInNavigationState.locationViewModel }
    val tripSessionStarterViewModel by lazy { dropInNavigationState.tripSessionStarterViewModel }
    val audioGuidanceViewModel by lazy { dropInNavigationState.audioGuidanceViewModel }
    val cameraViewModel by lazy { dropInNavigationState.cameraViewModel }
    val destinationViewModel by lazy { dropInNavigationState.destinationViewModel }
    val routesViewModel by lazy { dropInNavigationState.routesViewModel }

    init {
        MapboxNavigationApp.attach(viewModelLifecycleOwner)
    }

    override fun onCleared() {
        viewModelLifecycleOwner.destroy()
    }
}

/**
 * The [MapboxNavigationApp] needs a scope that can survive configuration changes.
 * Everything inside the [DropInNavigationViewModel] will survive the orientation change, we can
 * assume that the [MapboxNavigationApp] is [Lifecycle.State.CREATED] between init and onCleared.
 *
 * The [Lifecycle.State.STARTED] and [Lifecycle.State.RESUMED] states are represented by the
 * hosting view [DropInNavigationView].
 */
private class DropInViewModelLifecycleOwner : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
        .apply { currentState = Lifecycle.State.CREATED }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    fun destroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }
}
