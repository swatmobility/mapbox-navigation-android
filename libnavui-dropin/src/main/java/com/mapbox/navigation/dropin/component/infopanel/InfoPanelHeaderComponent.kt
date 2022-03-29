package com.mapbox.navigation.dropin.component.infopanel

import androidx.core.view.isVisible
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.dropin.DropInNavigationViewContext
import com.mapbox.navigation.dropin.R
import com.mapbox.navigation.dropin.component.destination.DestinationAction
import com.mapbox.navigation.dropin.component.navigation.NavigationState
import com.mapbox.navigation.dropin.component.navigation.NavigationStateAction
import com.mapbox.navigation.dropin.component.routefetch.RoutesAction
import com.mapbox.navigation.dropin.component.routefetch.RoutesState
import com.mapbox.navigation.dropin.databinding.MapboxInfoPanelHeaderLayoutBinding
import com.mapbox.navigation.dropin.lifecycle.UIComponent
import com.mapbox.navigation.utils.internal.ifNonNull
import com.mapbox.navigation.utils.internal.logE
import com.mapbox.navigation.utils.internal.logW
import com.mapbox.navigation.utils.internal.toPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class InfoPanelHeaderComponent(
    context: DropInNavigationViewContext,
    private val binding: MapboxInfoPanelHeaderLayoutBinding,
) : UIComponent() {
    private val store = context.viewModel.store

    private val resources get() = binding.root.resources

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        super.onAttached(mapboxNavigation)

        // views visibility
        store.select { it.navigation }.observe {
            binding.poiName.isVisible = it == NavigationState.DestinationPreview
            binding.routePreview.isVisible = it == NavigationState.DestinationPreview
            binding.startNavigation.isVisible = it == NavigationState.DestinationPreview ||
                it == NavigationState.RoutePreview
            binding.endNavigation.isVisible = it == NavigationState.ActiveNavigation ||
                it == NavigationState.Arrival
            binding.tripProgressLayout.isVisible = it == NavigationState.ActiveNavigation ||
                it == NavigationState.RoutePreview
            binding.arrivedText.isVisible = it == NavigationState.Arrival
        }

        store.select { it.destination }.observe {
            val placeName = it?.features?.firstOrNull()?.placeName()
            binding.poiName.text = placeName
                ?: resources.getString(R.string.mapbox_drop_in_dropped_pin)
        }

        binding.routePreview.setOnClickListener {
            updateNavigationStateWhenRouteIsReady(NavigationState.RoutePreview)
        }

        binding.startNavigation.setOnClickListener {
            updateNavigationStateWhenRouteIsReady(NavigationState.ActiveNavigation)
        }

        binding.endNavigation.setOnClickListener {
            store.dispatch(RoutesAction.SetRoutes(emptyList()))
            store.dispatch(DestinationAction.SetDestination(null))
            store.dispatch(NavigationStateAction.Update(NavigationState.FreeDrive))
        }
    }

    private fun updateNavigationStateWhenRouteIsReady(navigationState: NavigationState) {
        ifNonNull(
            store.state.value.location?.toPoint(),
            store.state.value.destination
        ) { lastPoint, destination ->
            store.dispatch(RoutesAction.FetchPoints(listOf(lastPoint, destination.point)))
            coroutineScope.launch {
                // Wait for fetching to complete and then take action
                val isRouteReady = waitForFetched()
                if (isActive && isRouteReady) {
                    store.dispatch(NavigationStateAction.Update(navigationState))
                } else {
                    logW(TAG, "Routes are not ready")
                }
            }
        } ?: logE(TAG, "Cannot fetch routes because state is incorrect")
    }

    private suspend fun waitForFetched(): Boolean {
        store.select { it.routes }.takeWhile { it is RoutesState.Fetching }.collect()
        return store.state.value.routes is RoutesState.Ready
    }

    private companion object {
        private val TAG = this::class.java.simpleName
    }
}
