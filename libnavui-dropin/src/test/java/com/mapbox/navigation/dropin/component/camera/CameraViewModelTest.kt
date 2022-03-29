package com.mapbox.navigation.dropin.component.camera

import com.mapbox.maps.EdgeInsets
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.dropin.util.TestStore
import com.mapbox.navigation.testing.MainCoroutineRule
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class, ExperimentalCoroutinesApi::class)
class CameraViewModelTest {

    @get:Rule
    var coroutineRule = MainCoroutineRule()

    private lateinit var testStore: TestStore

    @Before
    fun setUp() {
        testStore = spyk(TestStore(coroutineRule.coroutineScope))
    }

    @Test
    fun `when action initialize update camera mode`() = coroutineRule.runBlockingTest {
        val cameraViewModel = CameraViewModel(testStore)
        val mockMapboxNavigation = mockk<MapboxNavigation>(relaxed = true)
        cameraViewModel.onAttached(mockMapboxNavigation)
        testStore.dispatch(CameraAction.InitializeCamera(TargetCameraMode.Overview))

        val cameraState = testStore.state.value.camera

        assertTrue(cameraState.isCameraInitialized)
        assertEquals(TargetCameraMode.Overview, cameraState.cameraMode)
    }

    @Test
    fun `when action toIdle updates camera mode`() = coroutineRule.runBlockingTest {
        val cameraViewModel = CameraViewModel(testStore)
        val mockMapboxNavigation = mockk<MapboxNavigation>(relaxed = true)
        cameraViewModel.onAttached(mockMapboxNavigation)
        testStore.dispatch(CameraAction.ToIdle)

        val cameraState = testStore.state.value.camera

        assertEquals(TargetCameraMode.Idle, cameraState.cameraMode)
    }

    @Test
    fun `when action toOverview updates camera mode`() = coroutineRule.runBlockingTest {
        val cameraViewModel = CameraViewModel(testStore)
        val mockMapboxNavigation = mockk<MapboxNavigation>(relaxed = true)
        cameraViewModel.onAttached(mockMapboxNavigation)
        testStore.dispatch(CameraAction.ToOverview)

        val cameraState = testStore.state.value.camera

        assertEquals(TargetCameraMode.Overview, cameraState.cameraMode)
    }

    @Test
    fun `when action toFollowing updates camera mode and zoomUpdatesAllowed`() =
        coroutineRule.runBlockingTest {
            val cameraViewModel = CameraViewModel(testStore)
            val mockMapboxNavigation = mockk<MapboxNavigation>(relaxed = true)
            cameraViewModel.onAttached(mockMapboxNavigation)
            testStore.dispatch(CameraAction.ToFollowing)

            val cameraState = testStore.state.value.camera

            assertEquals(TargetCameraMode.Following, cameraState.cameraMode)
        }

    @Test
    fun `when action UpdatePadding updates cameraPadding`() =
        coroutineRule.runBlockingTest {
            val padding = EdgeInsets(1.0, 2.0, 3.0, 4.0)
            val cameraViewModel = CameraViewModel(testStore)
            val mockMapboxNavigation = mockk<MapboxNavigation>(relaxed = true)
            cameraViewModel.onAttached(mockMapboxNavigation)

            testStore.dispatch(CameraAction.UpdatePadding(padding))

            assertEquals(padding, testStore.state.value.camera.cameraPadding)
        }
}
