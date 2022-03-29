package com.mapbox.navigation.dropin.component.camera

import com.mapbox.maps.EdgeInsets
import com.mapbox.navigation.dropin.lifecycle.UIComponent
import com.mapbox.navigation.dropin.model.Action
import com.mapbox.navigation.dropin.model.Reducer
import com.mapbox.navigation.dropin.model.State
import com.mapbox.navigation.dropin.model.Store

sealed class CameraAction : Action {
    data class InitializeCamera(val target: TargetCameraMode) : CameraAction()
    object ToIdle : CameraAction()
    object ToOverview : CameraAction()
    object ToFollowing : CameraAction()
    data class UpdatePadding(val padding: EdgeInsets) : CameraAction()
}

internal class CameraViewModel(store: Store) : UIComponent(), Reducer {

    init {
        store.register(this)
    }

    override fun process(state: State, action: Action): State {
        if (action is CameraAction) {
            return state.copy(camera = processCameraAction(state.camera, action))
        }
        return state
    }

    private fun processCameraAction(state: CameraState, action: CameraAction): CameraState {
        return when (action) {
            is CameraAction.InitializeCamera -> {
                state.copy(isCameraInitialized = true, cameraMode = action.target)
            }
            is CameraAction.ToIdle -> {
                state.copy(cameraMode = TargetCameraMode.Idle)
            }
            is CameraAction.ToOverview -> {
                state.copy(cameraMode = TargetCameraMode.Overview)
            }
            is CameraAction.ToFollowing -> {
                state.copy(cameraMode = TargetCameraMode.Following)
            }
            is CameraAction.UpdatePadding -> {
                state.copy(cameraPadding = action.padding)
            }
        }
    }
}
