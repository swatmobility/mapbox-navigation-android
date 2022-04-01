package com.mapbox.examples.androidauto.car.navigation

import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarColor
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.androidauto.FreeDriveState
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.androidauto.car.navigation.roadlabel.RoadLabelSurfaceLayer
import com.mapbox.androidauto.car.navigation.speedlimit.CarSpeedLimitRenderer
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.navigation.ui.car.R
import com.mapbox.examples.androidauto.car.MainMapActionStrip
import com.mapbox.examples.androidauto.car.action.MapboxActionProvider
import com.mapbox.examples.androidauto.car.feedback.core.CarFeedbackSender
import com.mapbox.examples.androidauto.car.feedback.ui.CarGridFeedbackScreen
import com.mapbox.examples.androidauto.car.feedback.ui.buildArrivalFeedbackProvider
import com.mapbox.examples.androidauto.car.location.CarLocationRenderer
import com.mapbox.examples.androidauto.car.preview.CarRouteLine
import com.mapbox.maps.MapboxExperimental
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp

/**
 * After a route has been selected. This view gives turn-by-turn instructions
 * for completing the route.
 */
@OptIn(MapboxExperimental::class)
class ActiveGuidanceScreen(
    private val carActiveGuidanceContext: CarActiveGuidanceCarContext,
    private val actionProviders: List<MapboxActionProvider>
) : Screen(carActiveGuidanceContext.carContext) {

    val carRouteLine = CarRouteLine(carActiveGuidanceContext.mainCarContext)
    val carLocationRenderer = CarLocationRenderer(carActiveGuidanceContext.mainCarContext)
    val carSpeedLimitRenderer = CarSpeedLimitRenderer(carActiveGuidanceContext.mainCarContext)
    val carNavigationCamera = CarNavigationCamera(
        carActiveGuidanceContext.mapboxCarMap,
        carActiveGuidanceContext.mapboxNavigation,
        CarCameraMode.FOLLOWING
    )
    private val roadLabelSurfaceLayer = RoadLabelSurfaceLayer(
        carActiveGuidanceContext.carContext,
        carActiveGuidanceContext.mapboxNavigation
    )

    private val carRouteProgressObserver = CarNavigationInfoObserver(carActiveGuidanceContext)
    private val mapActionStripBuilder = MainMapActionStrip(this, carNavigationCamera)

    init {
        logAndroidAuto("ActiveGuidanceScreen constructor")
        lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onResume(owner: LifecycleOwner) {
                logAndroidAuto("ActiveGuidanceScreen onResume")
                carActiveGuidanceContext.mapboxCarMap.registerObserver(carLocationRenderer)
                carActiveGuidanceContext.mapboxCarMap.registerObserver(roadLabelSurfaceLayer)
                carActiveGuidanceContext.mapboxCarMap.registerObserver(carSpeedLimitRenderer)
                carActiveGuidanceContext.mapboxCarMap.registerObserver(carNavigationCamera)
                carActiveGuidanceContext.mapboxCarMap.registerObserver(carRouteLine)
                carRouteProgressObserver.start {
                    invalidate()
                }
            }

            override fun onPause(owner: LifecycleOwner) {
                logAndroidAuto("ActiveGuidanceScreen onPause")
                carActiveGuidanceContext.mapboxCarMap.unregisterObserver(roadLabelSurfaceLayer)
                carActiveGuidanceContext.mapboxCarMap.unregisterObserver(carLocationRenderer)
                carActiveGuidanceContext.mapboxCarMap.unregisterObserver(carSpeedLimitRenderer)
                carActiveGuidanceContext.mapboxCarMap.unregisterObserver(carNavigationCamera)
                carActiveGuidanceContext.mapboxCarMap.unregisterObserver(carRouteLine)
                carRouteProgressObserver.stop()
            }
        })
    }

    override fun onGetTemplate(): Template {
        logAndroidAuto("ActiveGuidanceScreen onGetTemplate")
        val actionStrip = ActionStrip.Builder().apply {
            actionProviders.forEach {
                when (it) {
                    is MapboxActionProvider.ScreenActionProvider -> {
                        this.addAction(it.getAction(this@ActiveGuidanceScreen))
                    }
                    is MapboxActionProvider.ActionProvider -> {
                        this.addAction(it.getAction())
                    }
                }
            }
            this.addAction(
                Action.Builder()
                    .setTitle(carContext.getString(R.string.car_action_navigation_stop_button))
                    .setOnClickListener {
                        stopNavigation()
                    }.build()
            )
        }.build()
        val builder = NavigationTemplate.Builder()
            .setBackgroundColor(CarColor.PRIMARY)
            .setActionStrip(actionStrip)
            .setMapActionStrip(mapActionStripBuilder.build())

        carRouteProgressObserver.navigationInfo?.let {
            builder.setNavigationInfo(it)
        }

        carRouteProgressObserver.travelEstimateInfo?.let {
            builder.setDestinationTravelEstimate(it)
        }

        return builder.build()
    }

    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private fun stopNavigation() {
        logAndroidAuto("ActiveGuidanceScreen stopNavigation")
        // Push to screen and capture feedback, and clear the current route.
        // When completed, go back to FreeDriveState
        MapboxNavigationApp.current()?.let { mapboxNavigation ->
            screenManager.pushForResult(
                CarGridFeedbackScreen(
                    carContext,
                    javaClass.simpleName,
                    CarFeedbackSender(),
                    feedbackItems = buildArrivalFeedbackProvider(carContext),
                    encodedSnapshot = null
                )
            ) {
                MapboxCarApp.updateCarAppState(FreeDriveState)
            }
            mapboxNavigation.setRoutes(emptyList())
        }
    }
}
