package com.mapbox.navigation.dropin.component.navigation

import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.dropin.util.TestStore
import com.mapbox.navigation.testing.MainCoroutineRule
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class, ExperimentalCoroutinesApi::class)
internal class NavigationStateViewModelTest {

    @get:Rule
    var coroutineRule = MainCoroutineRule()

    private lateinit var testStore: TestStore

    lateinit var sut: NavigationStateViewModel

    @Before
    fun setUp() {
        testStore = TestStore(coroutineRule.coroutineScope)
        sut = NavigationStateViewModel(testStore)
    }

    @Test
    fun `should set new state on Update action`() = coroutineRule.runBlockingTest {
        sut.onAttached(mockk())

        testStore.dispatch(NavigationStateAction.Update(NavigationState.RoutePreview))

        assertEquals(NavigationState.RoutePreview, testStore.state.value.navigation)
    }
}
