package com.mapbox.navigation.dropin.util

import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
internal class CompositeMapboxNavigationObserver(
    vararg elements: MapboxNavigationObserver
) : MapboxNavigationObserver {
    private val observers = mutableListOf(*elements)

    fun add(o: MapboxNavigationObserver) = observers.add(o)
    fun remove(o: MapboxNavigationObserver) = observers.remove(o)

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        observers.forEach { it.onAttached(mapboxNavigation) }
    }

    override fun onDetached(mapboxNavigation: MapboxNavigation) {
        observers.reversed().forEach { it.onDetached(mapboxNavigation) }
    }
}
