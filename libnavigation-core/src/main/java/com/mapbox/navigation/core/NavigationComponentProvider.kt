package com.mapbox.navigation.core

import android.content.Context
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.base.common.logger.Logger
import com.mapbox.navigation.base.route.Router
import com.mapbox.navigation.base.trip.notification.TripNotification
import com.mapbox.navigation.core.directions.session.DirectionsSession
import com.mapbox.navigation.core.directions.session.MapboxDirectionsSession
import com.mapbox.navigation.core.trip.service.MapboxTripService
import com.mapbox.navigation.core.trip.service.TripService
import com.mapbox.navigation.core.trip.session.MapboxTripSession
import com.mapbox.navigation.core.trip.session.TripSession

internal object NavigationComponentProvider {
    fun createDirectionsSession(
        router: Router
    ): DirectionsSession =
        MapboxDirectionsSession(router)

    fun createTripService(
        applicationContext: Context,
        tripNotification: TripNotification,
        logger: Logger
    ): TripService = MapboxTripService(
        applicationContext,
        tripNotification,
        logger
    )

    fun createTripSession(
        tripService: TripService,
        locationEngine: LocationEngine,
        locationEngineRequest: LocationEngineRequest,
        navigatorPredictionMillis: Long,
        logger: Logger
    ): TripSession = MapboxTripSession(
        tripService,
        locationEngine,
        locationEngineRequest,
        navigatorPredictionMillis,
        logger = logger
    )

    fun createNavigationSession(): NavigationSession = NavigationSession()
}
