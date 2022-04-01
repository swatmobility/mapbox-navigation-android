package com.mapbox.examples.androidauto.car.preview

import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.examples.androidauto.car.MainCarContext
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.androidauto.MapboxCarMapObserver
import com.mapbox.maps.extension.androidauto.MapboxCarMapSurface
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.ui.maps.route.RouteLayerConstants.TOP_LEVEL_ROUTE_LINE_LAYER_ID
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources

/**
 * This class is to simplify the interaction with [MapboxRouteLineApi], [MapboxRouteArrowView]
 * [MapboxRouteArrowApi], and [RouteProgressObserver] use cases that the app needs in the car.
 *
 * Anything for rendering the car's route line, is handled here at this point.
 */
@OptIn(MapboxExperimental::class)
class CarRouteLine(
    val mainCarContext: MainCarContext
) : MapboxCarMapObserver {

    private val routeLineColorResources by lazy {
        RouteLineColorResources.Builder().build()
    }

    private val routeLineResources: RouteLineResources by lazy {
        RouteLineResources.Builder()
            .routeLineColorResources(routeLineColorResources)
            .build()
    }

    private lateinit var routeLineView: MapboxRouteLineView
    private lateinit var routeLineApi: MapboxRouteLineApi
    private lateinit var routeArrowApi: MapboxRouteArrowApi
    private lateinit var routeArrowView: MapboxRouteArrowView

    private val onPositionChangedListener = OnIndicatorPositionChangedListener { point ->
        val result = routeLineApi.updateTraveledRouteLine(point)
        mainCarContext.mapboxCarMap.carMapSurface?.mapSurface?.let { mapSurface ->
            mapSurface.getMapboxMap().getStyle { style ->
                routeLineView.renderRouteLineUpdate(style, result)
            }
        }
    }

    private val routesObserver = RoutesObserver { result ->
        logAndroidAuto("CarRouteLine onRoutesChanged ${result.routes.size}")
        mainCarContext.mapboxCarMap.carMapSurface?.mapSurface?.let { mapSurface ->
            mapSurface.getMapboxMap().getStyle { style ->
                if (result.routes.isNotEmpty()) {
                    val routeLines = result.routes.map { RouteLine(it, null) }
                    routeLineApi.setRoutes(routeLines) { value ->
                        routeLineView.renderRouteDrawData(style, value)
                    }
                } else {
                    routeLineApi.clearRouteLine { value ->
                        routeLineView.renderClearRouteLineValue(style, value)
                    }
                    val clearArrowValue = routeArrowApi.clearArrows()
                    routeArrowView.render(style, clearArrowValue)
                }
            }
        }
    }

    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        mainCarContext.mapboxCarMap.carMapSurface?.mapSurface?.let { mapSurface ->
            mapSurface.getMapboxMap().getStyle { style ->
                routeLineApi.updateWithRouteProgress(routeProgress) { result ->
                    routeLineView.renderRouteLineUpdate(style, result)
                }
                routeArrowApi.addUpcomingManeuverArrow(routeProgress).also { arrowUpdate ->
                    routeArrowView.renderManeuverUpdate(style, arrowUpdate)
                }
            }
        }
    }

    override fun onAttached(mapboxCarMapSurface: MapboxCarMapSurface) {
        logAndroidAuto("CarRouteLine carMapSurface loaded $mapboxCarMapSurface")
        val locationPlugin = mapboxCarMapSurface.mapSurface.location

        mainCarContext.mapboxCarMap.carMapSurface?.mapSurface?.let { mapSurface ->
            mapSurface.getMapboxMap().getStyle { style ->
                val routeLineOptions = getMapboxRouteLineOptions(mapboxCarMapSurface)
                routeLineView = MapboxRouteLineView(routeLineOptions)
                routeLineApi = MapboxRouteLineApi(routeLineOptions).also {
                    routeLineView.initializeLayers(style)
                }
                routeArrowApi = MapboxRouteArrowApi()
                routeArrowView = MapboxRouteArrowView(
                    RouteArrowOptions.Builder(mainCarContext.carContext)
                        .withAboveLayerId(TOP_LEVEL_ROUTE_LINE_LAYER_ID)
                        .build()
                )

                locationPlugin.addOnIndicatorPositionChangedListener(onPositionChangedListener)
                mainCarContext.mapboxNavigation.apply {
                    registerRouteProgressObserver(routeProgressObserver)
                    registerRoutesObserver(routesObserver)
                }
            }
        }
    }

    override fun onDetached(mapboxCarMapSurface: MapboxCarMapSurface) {
        logAndroidAuto("CarRouteLine carMapSurface detached $mapboxCarMapSurface")
        val mapSurface = mapboxCarMapSurface.mapSurface
        mapSurface.location.removeOnIndicatorPositionChangedListener(onPositionChangedListener)
        mainCarContext.mapboxNavigation.apply {
            unregisterRouteProgressObserver(routeProgressObserver)
            unregisterRoutesObserver(routesObserver)
        }
    }

    private fun getMapboxRouteLineOptions(
        mapboxCarMapSurface: MapboxCarMapSurface
    ): MapboxRouteLineOptions {
        return MapboxRouteLineOptions.Builder(mainCarContext.carContext)
            .withRouteLineResources(routeLineResources)
            .withRouteLineBelowLayerId(findRoadLabelsLayerId(mapboxCarMapSurface))
            .withVanishingRouteLineEnabled(true)
            .build()
    }

    private fun findRoadLabelsLayerId(mapboxCarMapSurface: MapboxCarMapSurface): String {
        return mapboxCarMapSurface.mapSurface.getMapboxMap().getStyle()?.styleLayers
            ?.firstOrNull { layer -> layer.id.contains("road-label") }
            ?.id ?: "road-label-navigation"
    }
}
