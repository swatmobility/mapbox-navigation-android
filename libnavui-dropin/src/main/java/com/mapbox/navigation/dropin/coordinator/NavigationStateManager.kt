package com.mapbox.navigation.dropin.coordinator

import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.navigation.base.trip.model.RouteProgressState
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.dropin.component.navigationstate.NavigationState
import com.mapbox.navigation.dropin.extensions.flowOnFinalDestinationArrival
import com.mapbox.navigation.dropin.extensions.flowRoutesUpdated
import com.mapbox.navigation.dropin.lifecycle.UIComponent
import com.mapbox.navigation.dropin.model.Destination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Class that manages NavigationState as following:
 *
 * ```
 *  [ FreeDrive ]━━( destination != null )━━━━━━>>[ RoutePreview ]<<━━━━━━━━━━━━┓
 *       ^         ( & routes not empty  )               ┃                      ┃
 *       ┃                                  (activeNavigationStarted=true)      ┃
 *       ┃                                               ┃                      ┃
 *       ┃                                               ┃         (activeNavigationStarted=false)
 *       ┣━━━━( destination == null )━━━━┓               v                      ┃
 *       ┗━━━━( routes empty        )━━━━╋━━━━━[ ActiveNavigation ]━━━━━━━━━━━━━┫
 *                                       ┃               ┃                      ┃
 *                                       ┃  (onFinalDestinationArrival())       ┃
 *                                       ┃               ┃                      ┃
 *                                       ┃               v                      ┃
 *                                       ┗━━━━━━━━━━[ Arrival ]━━━━━━━━━━━━━━━━━┛
 * ```
 */
internal class NavigationStateManager(
    private val destinationFlow: Flow<Destination?>,
    private val navigationState: MutableStateFlow<NavigationState>,
    private val activeNavigationStarted: Flow<Boolean>
) : UIComponent() {

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        super.onAttached(mapboxNavigation)

        coroutineScope.launch {
            combine(
                destinationFlow.stateIn(this),
                activeNavigationStarted.stateIn(this)
            ) { destination, started ->
                getNavigationState(destination, started, mapboxNavigation.getRoutes())
            }.collect {
                updateState(it)
            }
        }

        coroutineScope.launch {
            val destination = destinationFlow.stateIn(this)
            val started = activeNavigationStarted.stateIn(this)
            mapboxNavigation.flowRoutesUpdated().collect {
                val state = getNavigationState(
                    destination.value,
                    started.value,
                    it.routes
                )
                updateState(state)
            }
        }

        mapboxNavigation.flowOnFinalDestinationArrival().observe {
            val inActiveNav =
                navigationState.value == NavigationState.ActiveNavigation
            val arrived = it.currentState == RouteProgressState.COMPLETE
            if (inActiveNav && arrived) {
                updateState(NavigationState.Arrival)
            }
        }
    }

    private fun updateState(state: NavigationState) {
        if (navigationState.value != state) {
            navigationState.value = state
        }
    }

    private fun getNavigationState(
        destination: Destination?,
        activeNavigationStarted: Boolean,
        routes: List<DirectionsRoute>
    ): NavigationState {
        return if (destination != null) {
            if (routes.isNotEmpty()) {
                val inArrivalState = navigationState.value == NavigationState.Arrival
                when {
                    inArrivalState && activeNavigationStarted -> NavigationState.Arrival
                    activeNavigationStarted -> NavigationState.ActiveNavigation
                    else -> NavigationState.RoutePreview
                }
            } else {
                NavigationState.FreeDrive
            }
        } else {
            NavigationState.FreeDrive
        }
    }
}
