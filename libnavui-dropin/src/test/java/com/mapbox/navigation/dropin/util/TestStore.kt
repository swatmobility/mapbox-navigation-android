package com.mapbox.navigation.dropin.util

import com.mapbox.navigation.dropin.model.State
import com.mapbox.navigation.dropin.model.Store
import kotlinx.coroutines.CoroutineScope

internal class TestStore(
    coroutineScope: CoroutineScope
) : Store(coroutineScope) {

    fun setState(state: State) {
        _state.value = state
    }
}
