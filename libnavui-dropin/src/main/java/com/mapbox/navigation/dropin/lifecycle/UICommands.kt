package com.mapbox.navigation.dropin.lifecycle

import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.route.NavigationRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

sealed class UICommand {
    sealed class CameraCommand : UICommand() {
        object Recenter : CameraCommand()
        object Following : CameraCommand()
        object Overview : CameraCommand()
    }

    sealed class AudioGuidanceCommand : UICommand() {
        object Mute : AudioGuidanceCommand()
        object Unmute : AudioGuidanceCommand()
        object Toggle : AudioGuidanceCommand()
    }

    // Call them commands or orders
    sealed class RoutesCommand : UICommand() {
        data class FetchPoints(val points: List<Point>) : RoutesCommand()
        data class FetchOptions(val options: RouteOptions) : RoutesCommand()
        data class SetRoutes(
            val routes: List<NavigationRoute>,
            val legIndex: Int = 0
        ) : RoutesCommand()
    }
}

class UICommandDispatcher : UIComponent() {
    private val _commandFlow = MutableSharedFlow<UICommand>()
    val commandFlow: Flow<UICommand> = _commandFlow

    fun dispatch(command: UICommand) {
        coroutineScope.launch {
            _commandFlow.emit(command)
        }
    }
}
