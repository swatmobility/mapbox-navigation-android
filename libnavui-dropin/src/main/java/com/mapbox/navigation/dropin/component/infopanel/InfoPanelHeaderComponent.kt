package com.mapbox.navigation.dropin.component.infopanel

import androidx.core.view.isVisible
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.dropin.DropInNavigationViewContext
import com.mapbox.navigation.dropin.DropInNavigationViewModel
import com.mapbox.navigation.dropin.component.navigationstate.NavigationState
import com.mapbox.navigation.dropin.databinding.MapboxInfoPanelHeaderLayoutBinding
import com.mapbox.navigation.dropin.lifecycle.UIComponent
import com.mapbox.navigation.dropin.usecase.guidance.StartActiveGuidanceUseCase
import com.mapbox.navigation.dropin.usecase.guidance.StopActiveGuidanceUseCase
import com.mapbox.navigation.dropin.usecase.route.FetchAndSetRouteUseCase
import kotlinx.coroutines.launch

internal class InfoPanelHeaderComponent(
    private val binding: MapboxInfoPanelHeaderLayoutBinding,
    private val context: DropInNavigationViewContext
) : UIComponent() {

    private val viewModel: DropInNavigationViewModel
        get() = context.viewModel
    private val stopActiveGuidanceUseCase: StopActiveGuidanceUseCase
        get() = context.stopActiveGuidanceUseCase()
    private val fetchAndSetRouteUseCase: FetchAndSetRouteUseCase
        get() = context.fetchAndSetRouteUseCase()
    private val startActiveGuidanceUseCase: StartActiveGuidanceUseCase
        get() = context.startActiveGuidanceUseCase()

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        super.onAttached(mapboxNavigation)

        // views visibility
        viewModel.navigationState.observe {
            binding.poiName.isVisible = it == NavigationState.FreeDrive
            binding.routePreview.isVisible = it == NavigationState.FreeDrive
            binding.startNavigation.isVisible = it == NavigationState.FreeDrive ||
                it == NavigationState.RoutePreview
            binding.endNavigation.isVisible = it == NavigationState.ActiveNavigation ||
                it == NavigationState.Arrival
            binding.tripProgressView.isVisible = it == NavigationState.ActiveNavigation ||
                it == NavigationState.RoutePreview
            binding.arrivedText.isVisible = it == NavigationState.Arrival
        }

        binding.routePreview.setOnClickListener {
            // TODO: dispatch OnPressStartButtonEvent
            //  and move this logic to the event handler
            viewModel.destination.value?.also { destination ->
                coroutineScope.launch {
                    fetchAndSetRouteUseCase(destination.point)
                }
            }
        }

        binding.startNavigation.setOnClickListener {
            // TODO: dispatch OnPressStartButtonEvent
            //  and move this logic to the event handler
            when (viewModel.navigationState.value) {
                NavigationState.FreeDrive -> {
                    viewModel.destination.value?.also { destination ->
                        coroutineScope.launch {
                            fetchAndSetRouteUseCase(destination.point)
                            startActiveGuidanceUseCase(Unit)
                        }
                    }
                }
                NavigationState.RoutePreview -> {
                    coroutineScope.launch {
                        startActiveGuidanceUseCase(Unit)
                    }
                }
                else -> Unit
            }
        }

        binding.endNavigation.setOnClickListener {
            // TODO: dispatch OnPressEndNavigationButtonEvent
            //  and move this logic to the event handler
            coroutineScope.launch {
                stopActiveGuidanceUseCase(Unit)
                mapboxNavigation.setRoutes(emptyList())
            }
        }
    }
}
