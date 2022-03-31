package com.mapbox.navigation.dropin.binder.map

import com.mapbox.maps.MapView
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.dropin.DropInNavigationViewContext
import com.mapbox.navigation.dropin.binder.Binder
import com.mapbox.navigation.dropin.binder.navigationListOf
import com.mapbox.navigation.dropin.component.camera.CameraComponent
import com.mapbox.navigation.dropin.component.location.LocationComponent
import com.mapbox.navigation.dropin.component.marker.GeocodingComponent
import com.mapbox.navigation.dropin.component.marker.MapMarkersComponent
import com.mapbox.navigation.dropin.component.marker.RoutePreviewLongPressMapComponent
import com.mapbox.navigation.dropin.component.routeline.RouteLineComponent
import com.mapbox.navigation.dropin.lifecycle.reloadOnChange

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
internal class RoutePreviewMapBinder(
    private val context: DropInNavigationViewContext,
) : Binder<MapView> {

    override fun bind(mapView: MapView): MapboxNavigationObserver {
        val store = context.viewModel.store
        return navigationListOf(
            LocationComponent(context.viewModel.locationViewModel, mapView),
            reloadOnChange(
                context.mapStyleLoader.loadedMapStyle,
                context.options.routeLineOptions
            ) { _, lineOptions ->
                RouteLineComponent(store, mapView, lineOptions)
            },
            CameraComponent(store, mapView),
            MapMarkersComponent(store, mapView),
            RoutePreviewLongPressMapComponent(store, mapView),
            GeocodingComponent(store)
        )
    }
}
