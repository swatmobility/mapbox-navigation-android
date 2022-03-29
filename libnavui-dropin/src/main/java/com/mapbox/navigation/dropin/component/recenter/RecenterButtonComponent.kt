package com.mapbox.navigation.dropin.component.recenter

import androidx.core.view.isVisible
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.dropin.DropInNavigationViewContext
import com.mapbox.navigation.dropin.component.camera.CameraAction
import com.mapbox.navigation.dropin.component.camera.TargetCameraMode
import com.mapbox.navigation.dropin.component.navigation.NavigationState
import com.mapbox.navigation.dropin.lifecycle.UIComponent
import com.mapbox.navigation.dropin.view.MapboxExtendableButton
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@ExperimentalPreviewMapboxNavigationAPI
internal class RecenterButtonComponent(
    context: DropInNavigationViewContext,
    private val recenterButton: MapboxExtendableButton,
) : UIComponent() {
    private val store = context.viewModel.store

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        super.onAttached(mapboxNavigation)

        recenterButton.setOnClickListener {
            store.dispatch(CameraAction.ToFollowing)
        }

        coroutineScope.launch {
            combine(
                store.select { it.camera },
                store.select { it.navigation }
            ) { cameraState, navigationState ->
                navigationState != NavigationState.RoutePreview &&
                    cameraState.cameraMode == TargetCameraMode.Idle
            }.collect { visible ->
                recenterButton.isVisible = visible
            }
        }
    }

    override fun onDetached(mapboxNavigation: MapboxNavigation) {
        super.onDetached(mapboxNavigation)
        recenterButton.setOnClickListener(null)
    }
}
