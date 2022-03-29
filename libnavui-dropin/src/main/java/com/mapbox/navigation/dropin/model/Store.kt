package com.mapbox.navigation.dropin.model

import android.location.Location
import com.mapbox.navigation.dropin.component.audioguidance.AudioGuidanceState
import com.mapbox.navigation.dropin.component.camera.CameraState
import com.mapbox.navigation.dropin.component.navigation.NavigationState
import com.mapbox.navigation.dropin.component.routefetch.RoutesState
import com.mapbox.navigation.dropin.component.tripsession.TripSessionStarterState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

data class State(
    val destination: Destination? = null,
    val location: Location? = null,
    val navigation: NavigationState = NavigationState.FreeDrive,
    val camera: CameraState = CameraState(),
    val audio: AudioGuidanceState = AudioGuidanceState(),
    val routes: RoutesState = RoutesState.Empty,
    val tripSession: TripSessionStarterState = TripSessionStarterState()
)

interface Action

fun interface Reducer {
    fun process(state: State, action: Action): State
}

internal open class Store(
    private val coroutineScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    protected val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private val actionBus = MutableSharedFlow<Action>()
    private val reducers = ConcurrentLinkedQueue<Reducer>()

    init {
        coroutineScope.launch {
            actionBus.collect { action ->
                reducers.forEach { reducer ->
                    if (isActive) {
                        _state.value = reducer.process(_state.value, action)
                    }
                }
            }
        }
    }

    fun destroy() {
        coroutineScope.cancel()
    }

    fun <T> select(selector: (State) -> T): Flow<T> {
        return state.map { selector(it) }.distinctUntilChanged()
    }

    fun dispatch(action: Action) {
        coroutineScope.launch {
            actionBus.emit(action)
        }
    }

    fun register(vararg reducers: Reducer) {
        this.reducers.addAll(reducers)
    }

    fun unregister(vararg reducers: Reducer) {
        this.reducers.removeAll(reducers)
    }
}
