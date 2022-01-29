package com.mapbox.navigation.ui.base.internal

import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.common.HttpMethod
import com.mapbox.common.HttpRequest
import com.mapbox.common.HttpRequestError
import com.mapbox.common.HttpResponse
import com.mapbox.common.HttpResponseCallback
import com.mapbox.common.HttpResponseData
import com.mapbox.common.HttpServiceInterface
import com.mapbox.common.core.module.CommonSingletonModuleProvider
import com.mapbox.navigation.testing.MainCoroutineRule
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MapboxUtilDownloaderTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        mockkObject(CommonSingletonModuleProvider)
    }

    @After
    fun tearDown() {
        unmockkObject(CommonSingletonModuleProvider)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun download_statusCode200() = coroutineRule.runBlockingTest {
        setupMocks(200, "foobar".toByteArray(), false)

        val result = MapboxUtilDownloader.download("http://localhost/getFoobar")

        assertEquals("foobar", String(result.value!!))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun download_statusCode401() = coroutineRule.runBlockingTest {
        setupMocks(401, "foobar".toByteArray(), false)

        val result = MapboxUtilDownloader.download("http://localhost/getFoobar")

        assertEquals("Your token cannot access this resource.", result.error!!)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun download_statusCode404() = coroutineRule.runBlockingTest {
        setupMocks(404, "foobar".toByteArray(), false)

        val result = MapboxUtilDownloader.download("http://localhost/getFoobar")

        assertEquals("Resource is missing.", result.error!!)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun download_statusCode_other() = coroutineRule.runBlockingTest {
        setupMocks(999, "foobar".toByteArray(), false)

        val result = MapboxUtilDownloader.download("http://localhost/getFoobar")

        assertEquals("Unknown error (code: 999).", result.error!!)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun download_error_response() = coroutineRule.runBlockingTest {
        setupMocks(999, "foobar".toByteArray(), true)

        val result = MapboxUtilDownloader.download("http://localhost/getFoobar")

        assertEquals("FAIL!", result.error!!)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun download_getRequest() = coroutineRule.runBlockingTest {
        val httpRequestCallbackSlot: CapturingSlot<HttpRequest> = slot()
        setupMocks(
            200,
            "foobar".toByteArray(),
            false,
            httpRequestCallbackSlot
        )

        assertEquals(HttpMethod.GET, httpRequestCallbackSlot.captured.method)
        assertEquals("MapboxJava/" ,httpRequestCallbackSlot.captured.headers["User-Agent"])
        assertEquals(
            "mapbox-navigation-ui-android",
            httpRequestCallbackSlot.captured.uaComponents.sdkIdentifierComponent
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun fetch() = coroutineRule.runBlockingTest {
        setupMocks(200, "foobar".toByteArray(), false)

        val result = MapboxUtilDownloader.downloadPayload("http://localhost/getFoobar")

        assertEquals("http://localhost/getFoobar", result.value!!.url)
        assertEquals("foobar", String(result.value!!.data))
    }

    private fun setupMocks(
        responseCode: Long,
        responseData: ByteArray,
        makeErrorResponse: Boolean,
        httpRequestCallbackSlot: CapturingSlot<HttpRequest> = slot()
    ) {
        val httpResponseCallbackSlot = slot<HttpResponseCallback>()
        val mockResponse = when (makeErrorResponse) {
            true -> {
                val mockResponseError = mockk<HttpRequestError> {
                    every { message } returns "FAIL!"
                }
                getMockResponseError(mockResponseError)
            }
            false -> {
                val mockResponseData = getResponseData(responseCode, responseData)
                getMockResponse(mockResponseData)
            }
        }
        val mockHttpService = getHttpServiceInterface(
            httpRequestCallbackSlot,
            httpResponseCallbackSlot,
            mockResponse
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

    private fun getMockResponseError(error: HttpRequestError) = mockk<HttpResponse> {
        every { result } returns ExpectedFactory.createError(error)
    }

    private fun getHttpServiceInterface(
        httpRequestCallbackSlot: CapturingSlot<HttpRequest>,
        responseSlot: CapturingSlot<HttpResponseCallback>,
        mockResponse: HttpResponse
    ) = mockk<HttpServiceInterface> {
        every { request(capture(httpRequestCallbackSlot), capture(responseSlot)) } answers {
            responseSlot.captured.run(mockResponse)
            0
        }
    }
}
