package com.mapbox.navigation.dropin.util

import android.location.Location
import android.location.LocationManager
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.navigation.testing.FileUtils

object TestingUtil {
    fun loadRoute(routeFileName: String): DirectionsRoute {
        val routeAsJson = FileUtils.loadJsonFixture(routeFileName)
        return DirectionsRoute.fromJson(routeAsJson)
    }

    fun makeLocation(latitude: Double, longitude: Double, bearing: Float = 0f) =
        Location(LocationManager.PASSIVE_PROVIDER).apply {
            this.latitude = latitude
            this.longitude = longitude
            this.bearing = bearing
        }
}
