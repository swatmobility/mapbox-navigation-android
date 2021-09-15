package com.mapbox.navigation.ui.maps.route.line.model

/**
 * Traffic related data for section of a route
 *
 * @param distanceFromOrigin the distance from the route origin
 * @param trafficCongestionIdentifier a string indicating the level of traffic congestion
 * @param roadClass an optional road class for route section
 * @param legIndex indicates the leg index within the route that this data originates from
 * @param isLegOrigin indicates if this item is the origin point for a route leg
 */
internal data class RouteLineAnnotationExpressionData(
    val distanceFromOrigin: Double,
    val trafficCongestionIdentifier: String,
    val roadClass: String?,
    val legIndex: Int = 0,
    val isLegOrigin: Boolean = false
)