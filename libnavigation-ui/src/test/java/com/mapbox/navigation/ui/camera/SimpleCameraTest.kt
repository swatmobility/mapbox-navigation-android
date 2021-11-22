package com.mapbox.navigation.ui.camera

import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.ui.BaseTest
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class SimpleCameraTest : BaseTest() {

    @Test
    fun overview() {
        val route = buildTestDirectionsRoute()

        val result = SimpleCamera().overview(RouteInformation(route, null, null))

        assertEquals(19, result.size)
    }

    @Test
    fun overview_withRouteProgress() {
        val route = buildTestDirectionsRoute()
        val routeProgress = mockk<RouteProgress> {
            every { distanceTraveled } returns (route.distance() / 2).toFloat()
        }

        val result = SimpleCamera().overview(RouteInformation(route, null, routeProgress))

        assertEquals(9, result.size)
    }

    @Test
    fun overviewWillNotCacheRoute() {
        val route1 = buildTestDirectionsRoute()
        val route2 = getMultilegRoute()
        SimpleCamera().overview(RouteInformation(route1, null, null))

        val result = SimpleCamera().overview(RouteInformation(route2, null, null))

        assertEquals(114, result.size)
    }

    @Test
    fun tilt() {
        val result = SimpleCamera().tilt(RouteInformation(null, null, null))

        assertEquals(SimpleCamera.DEFAULT_TILT, result, 0.0)
    }

    @Test
    fun zoom() {
        val result = SimpleCamera().zoom(RouteInformation(null, null, null))

        assertEquals(15.0, result, 0.0)
    }

    private fun getMultilegRoute(): DirectionsRoute {
        val routeAsJson = loadJsonFixture("multileg_route.json")
        return DirectionsRoute.fromJson(routeAsJson)
    }
}