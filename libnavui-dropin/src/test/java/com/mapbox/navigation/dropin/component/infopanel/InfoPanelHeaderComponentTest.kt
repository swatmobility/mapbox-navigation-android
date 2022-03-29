package com.mapbox.navigation.dropin.component.infopanel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.test.core.app.ApplicationProvider
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.dropin.DropInNavigationViewContext
import com.mapbox.navigation.dropin.component.navigation.NavigationState
import com.mapbox.navigation.dropin.component.navigation.NavigationStateAction
import com.mapbox.navigation.dropin.component.routefetch.RoutesAction
import com.mapbox.navigation.dropin.component.routefetch.RoutesState
import com.mapbox.navigation.dropin.databinding.MapboxInfoPanelHeaderLayoutBinding
import com.mapbox.navigation.dropin.model.Destination
import com.mapbox.navigation.dropin.model.State
import com.mapbox.navigation.dropin.util.TestStore
import com.mapbox.navigation.dropin.util.TestingUtil.makeLocation
import com.mapbox.navigation.testing.MainCoroutineRule
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class, ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal class InfoPanelHeaderComponentTest {

    @get:Rule
    var coroutineRule = MainCoroutineRule()

    private lateinit var binding: MapboxInfoPanelHeaderLayoutBinding
    private lateinit var testStore: TestStore
    private lateinit var navContext: DropInNavigationViewContext

    private lateinit var sut: InfoPanelHeaderComponent

    @Before
    fun setup() {
        testStore = spyk(TestStore(coroutineRule.coroutineScope))
        navContext = mockk(relaxed = true) {
            every { viewModel } returns mockk {
                every { store } returns testStore
            }
        }

        val context: Context = ApplicationProvider.getApplicationContext()
        binding = MapboxInfoPanelHeaderLayoutBinding.inflate(
            context.getSystemService(LayoutInflater::class.java),
            FrameLayout(context)
        )

        sut = InfoPanelHeaderComponent(
            navContext,
            binding,
        )
    }

    @Test
    fun `should update views visibility for FreeDrive state`() = runBlockingTest {
        testStore.setState(State(navigation = NavigationState.FreeDrive))

        sut.onAttached(mockk())

        assertGone("poiName", binding.poiName)
        assertGone("routePreview", binding.routePreview)
        assertGone("startNavigation", binding.startNavigation)
        assertGone("endNavigation", binding.endNavigation)
        assertGone("tripProgressLayout", binding.tripProgressLayout)
        assertGone("arrivedText", binding.arrivedText)
    }

    @Test
    fun `should update views visibility for DestinationPreview state`() = runBlockingTest {
        testStore.setState(State(navigation = NavigationState.DestinationPreview))

        sut.onAttached(mockk())

        assertVisible("poiName", binding.poiName)
        assertVisible("routePreview", binding.routePreview)
        assertVisible("startNavigation", binding.startNavigation)
        assertGone("endNavigation", binding.endNavigation)
        assertGone("tripProgressLayout", binding.tripProgressLayout)
        assertGone("arrivedText", binding.arrivedText)
    }

    @Test
    fun `should update views visibility for RoutePreview state`() = runBlockingTest {
        testStore.setState(State(navigation = NavigationState.RoutePreview))

        sut.onAttached(mockk())

        assertGone("poiName", binding.poiName)
        assertGone("routePreview", binding.routePreview)
        assertVisible("startNavigation", binding.startNavigation)
        assertGone("endNavigation", binding.endNavigation)
        assertVisible("tripProgressLayout", binding.tripProgressLayout)
        assertGone("arrivedText", binding.arrivedText)
    }

    @Test
    fun `should update views visibility for ActiveNavigation state`() = runBlockingTest {
        testStore.setState(State(navigation = NavigationState.ActiveNavigation))

        sut.onAttached(mockk())

        assertGone("poiName", binding.poiName)
        assertGone("routePreview", binding.routePreview)
        assertGone("startNavigation", binding.startNavigation)
        assertVisible("endNavigation", binding.endNavigation)
        assertVisible("tripProgressLayout", binding.tripProgressLayout)
        assertGone("arrivedText", binding.arrivedText)
    }

    @Test
    fun `should update views visibility for Arrival state`() = runBlockingTest {
        testStore.setState(State(navigation = NavigationState.Arrival))

        sut.onAttached(mockk())

        assertGone("poiName", binding.poiName)
        assertGone("routePreview", binding.routePreview)
        assertGone("startNavigation", binding.startNavigation)
        assertVisible("endNavigation", binding.endNavigation)
        assertGone("tripProgressLayout", binding.tripProgressLayout)
        assertVisible("arrivedText", binding.arrivedText)
    }

    @Test
    fun `should update poiName text`() {
        val featurePlaceName = "POI NAME"
        val newDestination = Destination(
            Point.fromLngLat(1.0, 2.0),
            listOf(
                mockk {
                    every { placeName() } returns featurePlaceName
                }
            )
        )
        testStore.setState(State(destination = null))

        sut.onAttached(mockk())
        testStore.setState(State(destination = newDestination))

        assertEquals(binding.poiName.text, featurePlaceName)
    }

    @Test
    fun `onClick routePreview should FetchPoints`() = runBlockingTest {
        testStore.setState(
            State(
                navigation = NavigationState.DestinationPreview,
                location = makeLocation(10.0, 11.0),
                routes = RoutesState.Ready(mockk()),
                destination = Destination(Point.fromLngLat(22.0, 23.0))
            )
        )
        val routeActionSlot = mutableListOf<RoutesAction>()
        every { testStore.dispatch(capture(routeActionSlot)) } just Runs

        sut.onAttached(mockk())
        binding.routePreview.performClick()

        assertEquals(1, routeActionSlot.size)
        assertTrue(routeActionSlot[0] is RoutesAction.FetchPoints)
    }

    @Test
    fun `onClick routePreview should start RoutePreview`() = runBlockingTest {
        testStore.setState(
            State(
                navigation = NavigationState.DestinationPreview,
                location = makeLocation(10.0, 11.0),
                routes = RoutesState.Ready(mockk()),
                destination = Destination(Point.fromLngLat(22.0, 23.0))
            )
        )

        sut.onAttached(mockk())
        binding.routePreview.performClick()

        verify {
            testStore.dispatch(
                NavigationStateAction.Update(NavigationState.RoutePreview)
            )
        }
    }

    @Test
    fun `onClick startNavigation should FetchPoints`() = runBlockingTest {
        testStore.setState(
            State(
                navigation = NavigationState.DestinationPreview,
                location = makeLocation(10.0, 11.0),
                routes = RoutesState.Ready(mockk()),
                destination = Destination(Point.fromLngLat(22.0, 23.0))
            )
        )

        val routeActionSlot = mutableListOf<RoutesAction>()
        every { testStore.dispatch(capture(routeActionSlot)) } just Runs

        sut.onAttached(mockk())
        binding.startNavigation.performClick()

        assertEquals(1, routeActionSlot.size)
        assertTrue(routeActionSlot[0] is RoutesAction.FetchPoints)
    }

    @Test
    fun `onClick startNavigation start ActiveNavigation`() = runBlockingTest {
        testStore.setState(
            State(
                navigation = NavigationState.DestinationPreview,
                location = makeLocation(10.0, 11.0),
                routes = RoutesState.Ready(mockk()),
                destination = Destination(Point.fromLngLat(22.0, 23.0))
            )
        )

        sut.onAttached(mockk())
        binding.startNavigation.performClick()

        verify {
            testStore.dispatch(
                NavigationStateAction.Update(NavigationState.ActiveNavigation)
            )
        }
    }
}

private fun assertVisible(name: String, view: View) =
    assertTrue("$name should be VISIBLE", view.isVisible)

private fun assertGone(name: String, view: View) =
    assertFalse("$name should be GONE", view.isVisible)
