package com.mapbox.navigation.dropin.component.location

import androidx.core.content.ContextCompat
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.dropin.DropInNavigationViewContext
import com.mapbox.navigation.dropin.util.TestStore
import com.mapbox.navigation.testing.MainCoroutineRule
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class, ExperimentalCoroutinesApi::class)
internal class LocationComponentTest {

    @get:Rule
    var coroutineRule = MainCoroutineRule()

    private lateinit var sut: LocationComponent
    private lateinit var locationProvider: NavigationLocationProvider
    private lateinit var testStore: TestStore
    private lateinit var navContext: DropInNavigationViewContext

    @MockK
    lateinit var mockLocationViewModel: LocationViewModel

    @MockK
    lateinit var mockLocationPlugin: LocationComponentPlugin

    @Before
    fun setUp() {
        mockkStatic(ContextCompat::class)
        MockKAnnotations.init(this, relaxUnitFun = true)
        testStore = spyk(TestStore(coroutineRule.coroutineScope))
        navContext = mockk(relaxed = true) {
            every { viewModel } returns mockk {
                every { store } returns testStore
                every { locationViewModel } returns mockLocationViewModel
            }
        }
        locationProvider = NavigationLocationProvider()
        val mockMapView = mockk<MapView> {
            every { context } returns mockk()
            every { getMapboxMap() } returns mockk {
                val slot = slot<Style.OnStyleLoaded>()
                every { getStyle(capture(slot)) } answers {
                    slot.captured.onStyleLoaded(mockk())
                }
            }
            every { location } returns mockLocationPlugin
        }
        every { ContextCompat.getDrawable(any(), any()) } returns mockk()
        coEvery { mockLocationViewModel.firstLocation() } returns mockk()
        every { mockLocationViewModel.navigationLocationProvider } returns locationProvider

        sut = LocationComponent(navContext, mockMapView)
    }

    @After
    fun tearDown() {
        unmockkStatic(ContextCompat::class)
    }

    @Test
    fun `onAttach should configure and enable location component`() =
        coroutineRule.runBlockingTest {
            sut.onAttached(mockk())

            verify { mockLocationPlugin.setLocationProvider(locationProvider) }
            verify { mockLocationPlugin.enabled = true }
        }

    @Test
    fun `onAttach should configure location puck`() = coroutineRule.runBlockingTest {
        sut.onAttached(mockk())

        verify { mockLocationPlugin.locationPuck = ofType(LocationPuck2D::class) }
    }
}
