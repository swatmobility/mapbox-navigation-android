package com.mapbox.navigation.ui.maps.internal.route.line

import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxMap
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.model.ClosestRouteValue
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineClearValue
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineError
import com.mapbox.navigation.ui.maps.route.line.model.RouteNotFound
import com.mapbox.navigation.ui.maps.route.line.model.RouteSetValue
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Extension functions for [MapboxRouteLineApi] calls that are implemented as callbacks. This offers
 * an alternative to those callbacks by providing Kotlin oriented suspend functions.
 */
object MapboxRouteLineApiExtensions {

    /**
     * Updates which route is identified as the primary route.
     *
     * @param route the [DirectionsRoute] which should be designated as the primary
     * @return a state which contains the side effects to be applied to the map displaying the
     * newly designated route line.
     */
    suspend fun MapboxRouteLineApi.updateToPrimaryRoute(route: DirectionsRoute):
        Expected<RouteLineError, RouteSetValue> {
        return suspendCoroutine { continuation ->
            this.updateToPrimaryRoute(
                route
            ) { value -> continuation.resume(value) }
        }
    }

    /**
     * Sets the routes that will be operated on.
     *
     * @param newRoutes one or more routes. The first route in the collection will be considered
     * the primary route and any additional routes will be alternate routes.
     *
     * @return a state which contains the side effects to be applied to the map
     */
    suspend fun MapboxRouteLineApi.setRoutes(newRoutes: List<RouteLine>):
        Expected<RouteLineError, RouteSetValue> {
        return suspendCoroutine { continuation ->
            this.setRoutes(
                newRoutes
            ) { value -> continuation.resume(value) }
        }
    }

    /**
     * @return a state which contains the side effects to be applied to the map. The data
     * can be used to draw the current route line(s) on the map.
     */
    suspend fun MapboxRouteLineApi.getRouteDrawData(): Expected<RouteLineError, RouteSetValue> {
        return suspendCoroutine { continuation ->
            this.getRouteDrawData { value -> continuation.resume(value) }
        }
    }

    /**
     * The map will be queried for a route line feature at the target point or a bounding box
     * centered at the target point with a padding value determining the box's size. If a route
     * feature is found the index of that route in this class's route collection is returned. The
     * primary route is given precedence if more than one route is found.
     *
     * @param target a target latitude/longitude serving as the search point
     * @param mapboxMap a reference to the [MapboxMap] that will be queried
     * @param padding a sizing value added to all sides of the target point for creating a bounding
     * box to search in.
     *
     * @return a value containing the [DirectionsRoute] found or an error indicating no route was
     * found.
     */
    suspend fun MapboxRouteLineApi.findClosestRoute(
        target: Point,
        mapboxMap: MapboxMap,
        padding: Float,
    ): Expected<RouteNotFound, ClosestRouteValue> {
        return suspendCoroutine { continuation ->
            this.findClosestRoute(
                target,
                mapboxMap,
                padding
            ) { value -> continuation.resume(value) }
        }
    }

    /**
     * Clears the route line data.
     *
     * @return a state representing the side effects to be rendered on the map. In this case
     * the map should appear without any route lines.
     */
    suspend fun MapboxRouteLineApi.clearRouteLine(): Expected<RouteLineError, RouteLineClearValue> {
        return suspendCoroutine { continuation ->
            this.clearRouteLine { value -> continuation.resume(value) }
        }
    }
}
