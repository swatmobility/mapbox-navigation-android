package com.mapbox.navigation.qa_test_app.car

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.ContextCompat
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.delegates.MapPluginProviderDelegate
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.dropin.R
import com.mapbox.navigation.dropin.component.location.LocationViewModel
import com.mapbox.navigation.dropin.lifecycle.UIComponent
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CarLocationComponent(
    private val context: Context,
    private val mapboxMap: MapboxMap,
    private val mapPluginProvider: MapPluginProviderDelegate,
    private val locationViewModel: LocationViewModel
) : UIComponent() {

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        super.onAttached(mapboxNavigation)

        coroutineScope.launch {
            locationViewModel.firstLocation()
            mapboxMap.getStyle {
                mapPluginProvider.location.apply {
                    setLocationProvider(locationViewModel.navigationLocationProvider)
                    locationPuck = LocationPuck2D(
                        bearingImage = ContextCompat.getDrawable(
                            context,
                            R.drawable.mapbox_navigation_puck_icon
                        )
                    )
                    enabled = true
                }
            }
        }
    }
}
