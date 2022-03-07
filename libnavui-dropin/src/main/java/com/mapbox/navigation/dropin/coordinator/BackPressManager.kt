package com.mapbox.navigation.dropin.coordinator

import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.dropin.DropInNavigationViewContext
import com.mapbox.navigation.dropin.component.navigationstate.NavigationState
import com.mapbox.navigation.dropin.component.routefetch.RoutesAction
import com.mapbox.navigation.dropin.lifecycle.UIComponent
import kotlinx.coroutines.flow.map

/**
 * Class that manages onBackPressedCallback enabled state
 * and handles onBackPressed event for each NavigationState.
 */
internal class BackPressManager(
    private val context: DropInNavigationViewContext
) : UIComponent() {

    private val routesState = context.routesState
    private val onBackPressedEvent = context.viewModel.onBackPressedEvent
    private val navigationState = context.viewModel.navigationState

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        super.onAttached(mapboxNavigation)

        routesState.map { it.destination }.observe {
            context.onBackPressedCallback.isEnabled = it != null
        }

        onBackPressedEvent.observe {
            when (navigationState.value) {
                NavigationState.FreeDrive -> {
                    context.dispatch(RoutesAction.SetDestination(null))
                }
                NavigationState.RoutePreview -> {
                    context.dispatch(RoutesAction.SetRoutes(emptyList()))
                }
                NavigationState.ActiveNavigation,
                NavigationState.Arrival -> {
                    context.dispatch(RoutesAction.StopNavigation)
                }
                else -> Unit
            }
        }
    }

    override fun onDetached(mapboxNavigation: MapboxNavigation) {
        super.onDetached(mapboxNavigation)
        context.onBackPressedCallback.isEnabled = false
    }
}
