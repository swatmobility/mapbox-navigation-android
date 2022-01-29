package com.mapbox.navigation.ui.maps.guidance.servicearea.api

import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.common.HttpRequest
import com.mapbox.common.HttpResponse
import com.mapbox.common.HttpResponseCallback
import com.mapbox.common.HttpResponseData
import com.mapbox.common.HttpServiceInterface
import com.mapbox.common.core.module.CommonSingletonModuleProvider
import com.mapbox.navigation.testing.MainCoroutineRule
import com.mapbox.navigation.utils.internal.InternalJobControlFactory
import com.mapbox.navigation.utils.internal.JobControl
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapboxServiceAreaApiTest {

    @get:Rule
    var coroutineRule = MainCoroutineRule()
    private val parentJob = SupervisorJob()
    private val testScope = CoroutineScope(parentJob + coroutineRule.testDispatcher)

    @Before
    fun setUp() {
        mockkObject(CommonSingletonModuleProvider)
        mockkObject(InternalJobControlFactory)
        every {
            InternalJobControlFactory.createIOScopeJobControl()
        } returns JobControl(parentJob, testScope)
    }

    @After
    fun cleanUp() {
        unmockkObject(InternalJobControlFactory)
        unmockkObject(CommonSingletonModuleProvider)
    }


    @Test
    fun fetchServiceAreaMap() = coroutineRule.runBlockingTest {
        setupCommonMocks()
        MapboxServiceAreaApi().fetchServiceAreaMap { expected ->
            assertEquals("foobar", String(expected.value!!.serviceMapData))
        }
    }

    fun setupCommonMocks() {
        val httpResponseCallbackSlot = slot<HttpResponseCallback>()
        val mockResponseData = getResponseData(200, "foobar".toByteArray())
        val response = getMockResponse(mockResponseData)
        val mockHttpService = getHttpServiceInterface(
            httpResponseCallbackSlot,
            response
        )
        every { CommonSingletonModuleProvider.createHttpService() } returns mockHttpService
    }

    private fun getResponseData(
        responseCode: Long,
        responseData: ByteArray
    ) = mockk<HttpResponseData> {
        every { code } returns responseCode
        every { data } returns responseData
    }

    private fun getMockResponse(responseData: HttpResponseData) = mockk<HttpResponse> {
        every { result } returns ExpectedFactory.createValue(responseData)
    }

    private fun getHttpServiceInterface(
        responseSlot: CapturingSlot<HttpResponseCallback> = slot(),
        mockResponse: HttpResponse
    ) = mockk<HttpServiceInterface> {
        every { request(any<HttpRequest>(), capture(responseSlot)) } answers {
            responseSlot.captured.run(mockResponse)
            0
        }
    }
}
