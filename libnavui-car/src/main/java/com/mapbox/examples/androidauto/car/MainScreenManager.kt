package com.mapbox.examples.androidauto.car

import androidx.car.app.Screen
import androidx.car.app.ScreenManager
import com.mapbox.androidauto.ActiveGuidanceState
import com.mapbox.androidauto.ArrivalState
import com.mapbox.androidauto.CarAppState
import com.mapbox.androidauto.FreeDriveState
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.androidauto.RoutePreviewState
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.androidauto.navigation.audioguidance.CarAudioGuidanceUi
import com.mapbox.examples.androidauto.car.feedback.core.CarFeedbackSender
import com.mapbox.examples.androidauto.car.feedback.ui.CarFeedbackAction
import com.mapbox.examples.androidauto.car.feedback.ui.activeGuidanceCarFeedbackProvider
import com.mapbox.examples.androidauto.car.navigation.ActiveGuidanceScreen
import com.mapbox.examples.androidauto.car.navigation.CarActiveGuidanceCarContext
import kotlinx.coroutines.flow.collect

class MainScreenManager(val mainCarContext: MainCarContext) {

    fun currentScreen(): Screen = currentScreen(MapboxCarApp.carAppState.value)

    private fun currentScreen(carAppState: CarAppState): Screen {
        return when (carAppState) {
            FreeDriveState, RoutePreviewState -> MainCarScreen(mainCarContext)
            ActiveGuidanceState, ArrivalState -> {
                ActiveGuidanceScreen(
                    CarActiveGuidanceCarContext(mainCarContext),
                    listOf(
                        CarFeedbackAction(
                            mainCarContext.mapboxCarMap,
                            CarFeedbackSender(),
                            activeGuidanceCarFeedbackProvider(mainCarContext.carContext)
                        ),
                        CarAudioGuidanceUi()
                    )
                )
            }
        }
    }

    suspend fun observeCarAppState() {
        MapboxCarApp.carAppState.collect { carAppState ->
            val currentScreen = currentScreen(carAppState)
            val screenManager = mainCarContext.carContext.getCarService(ScreenManager::class.java)
            logAndroidAuto("MainScreenManager screen change ${currentScreen.javaClass.simpleName}")
            if (screenManager.top.javaClass != currentScreen.javaClass) {
                screenManager.push(currentScreen)
            }
        }
    }
}
