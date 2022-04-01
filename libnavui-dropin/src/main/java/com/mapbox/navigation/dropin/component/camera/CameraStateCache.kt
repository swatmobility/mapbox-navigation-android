package com.mapbox.navigation.dropin.component.camera

import com.mapbox.maps.MapboxMap
import com.mapbox.maps.toCameraOptions
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.utils.internal.logD

/**
 * Mapbox Camera State cache backed by CameraViewModel.
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
internal class CameraStateCache(
    private val viewModel: CameraViewModel,
) {

    /**
     * Save [mapboxMap] Camera State in [CameraViewModel].
     */
    fun saveCameraState(mapboxMap: MapboxMap) {
        val state = mapboxMap.cameraState
        viewModel.invoke(CameraAction.SaveMapCameraState(state))
        logD("saveCameraState $mapboxMap; $state", "CameraStateRecorder")
    }

    /**
     * Read Camera State from [CameraViewModel] and set it to [mapboxMap].
     */
    fun restoreCameraState(mapboxMap: MapboxMap) {
        viewModel.state.value.mapCameraState?.also { state ->
            logD("restoreCameraState $mapboxMap; $state", "CameraStateRecorder")
            mapboxMap.setCamera(state.toCameraOptions())
        }
    }
}
