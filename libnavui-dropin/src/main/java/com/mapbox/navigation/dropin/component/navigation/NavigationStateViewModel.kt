package com.mapbox.navigation.dropin.component.navigation

import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.navigation.base.trip.model.RouteProgressState
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.dropin.component.navigationstate.NavigationState
import com.mapbox.navigation.dropin.component.routefetch.RoutesState
import com.mapbox.navigation.dropin.extensions.flowOnFinalDestinationArrival
import com.mapbox.navigation.dropin.extensions.flowRoutesUpdated
import com.mapbox.navigation.dropin.lifecycle.UIViewModel
import com.mapbox.navigation.dropin.model.Destination
import com.mapbox.navigation.utils.internal.logD
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
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
internal class NavigationStateViewModel(
    mutableState: MutableStateFlow<NavigationState>,
    private val routesState: StateFlow<RoutesState>
) : UIViewModel<NavigationState, NavigationStateAction>(mutableState) {

    override fun process(
        mapboxNavigation: MapboxNavigation,
        state: NavigationState,
        action: NavigationStateAction
    ): NavigationState {
        return when (action) {
            is NavigationStateAction.Update -> action.state
        }
    }

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        super.onAttached(mapboxNavigation)

        mainJobControl.scope.launch {
            routesState.collect {
                val state = getNavigationState(
                    it.destination,
                    it.navigationStarted,
                    mapboxNavigation.getRoutes()
                )
                updateState(state)
            }
        }

        mainJobControl.scope.launch {
            mapboxNavigation.flowRoutesUpdated().collect {
                val state = getNavigationState(
                    routesState.value.destination,
                    routesState.value.navigationStarted,
                    it.routes
                )
                updateState(state)
            }
        }

        mainJobControl.scope.launch {
            mapboxNavigation.flowOnFinalDestinationArrival().collect {
                val inActiveNav =
                    state.value == NavigationState.ActiveNavigation
                val arrived = it.currentState == RouteProgressState.COMPLETE
                if (inActiveNav && arrived) {
                    updateState(NavigationState.Arrival)
                }
            }
        }
    }

    private fun updateState(state: NavigationState) {
        if (this.state.value != state) {
            logD(TAG, "updateState ${this.state.value} -> $state")
            invoke(NavigationStateAction.Update(state))
        }
    }

    private fun getNavigationState(
        destination: Destination?,
        activeNavigationStarted: Boolean,
        routes: List<DirectionsRoute>
    ): NavigationState {
        return if (destination != null) {
            if (routes.isNotEmpty()) {
                val inArrivalState = state.value == NavigationState.Arrival
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

    companion object {
        private val TAG = NavigationStateViewModel::class.java.simpleName
    }
}
