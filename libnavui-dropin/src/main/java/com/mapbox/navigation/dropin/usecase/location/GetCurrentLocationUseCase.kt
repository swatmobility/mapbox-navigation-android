package com.mapbox.navigation.dropin.usecase.location

import android.annotation.SuppressLint
import android.location.Location
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.dropin.component.location.LocationViewModel
import com.mapbox.navigation.dropin.extensions.getLastLocation
import com.mapbox.navigation.dropin.usecase.UseCase
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Use case for retrieving latest device location.
 */
@SuppressLint("MissingPermission")
internal class GetCurrentLocationUseCase(
    private val navigation: MapboxNavigation,
    private val locationModel: LocationViewModel,
    dispatcher: CoroutineDispatcher
) : UseCase<Unit, Location>(dispatcher) {

    override suspend fun execute(parameters: Unit): Location {
        var location = locationModel.state.value
        if (location != null) return location

        val locationEngine = navigation.navigationOptions.locationEngine
        location = locationEngine.getLastLocation().lastLocation
        if (location == null) throw Error("Unable to get Location from LocationEngine")

        return location
    }
}
