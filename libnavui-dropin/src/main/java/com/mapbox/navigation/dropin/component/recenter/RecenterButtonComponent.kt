package com.mapbox.navigation.dropin.component.recenter

import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.dropin.lifecycle.UICommand
import com.mapbox.navigation.dropin.lifecycle.UICommandDispatcher
import com.mapbox.navigation.dropin.lifecycle.UIComponent
import com.mapbox.navigation.dropin.view.MapboxExtendableButton

@ExperimentalPreviewMapboxNavigationAPI
internal class RecenterButtonComponent(
    private val recenterButton: MapboxExtendableButton,
    private val commandDispatcher: UICommandDispatcher,
) : UIComponent() {

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        super.onAttached(mapboxNavigation)

        recenterButton.setOnClickListener {
            commandDispatcher.dispatch(UICommand.CameraCommand.Recenter)
        }
    }

    override fun onDetached(mapboxNavigation: MapboxNavigation) {
        super.onDetached(mapboxNavigation)
        recenterButton.setOnClickListener(null)
    }
}
