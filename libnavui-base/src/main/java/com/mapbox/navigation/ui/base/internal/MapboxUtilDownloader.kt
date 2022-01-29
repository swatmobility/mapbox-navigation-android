package com.mapbox.navigation.ui.base.internal

import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.common.HttpMethod
import com.mapbox.common.HttpRequest
import com.mapbox.common.UAComponents
import com.mapbox.common.core.module.CommonSingletonModuleProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object MapboxUtilDownloader {

    private const val USER_AGENT_KEY = "User-Agent"
    private const val USER_AGENT_VALUE = "MapboxJava/"
    private const val SDK_IDENTIFIER = "mapbox-navigation-ui-android"

    private const val CODE_200 = 200L
    private const val CODE_401 = 401L
    private const val CODE_404 = 404L

    suspend fun downloadPayload(urlToDownload: String): Expected<String, MapboxDownloadResult> {
        return download(urlToDownload).mapValue {
            MapboxDownloadResult(urlToDownload, it)
        }
    }

    suspend fun download(urlToDownload: String): Expected<String, ByteArray> =
        suspendCancellableCoroutine { continuation ->
            val service = CommonSingletonModuleProvider.createHttpService()
            val requestId = service.request(
                getHttpRequest(urlToDownload)
            ) { response ->
                response.result.fold(
                    {
                        ExpectedFactory.createError<String, ByteArray>(
                            response.result.error?.message ?: ""
                        )
                    }, { responseData ->
                    when (responseData.code) {
                        CODE_200 -> ExpectedFactory.createValue(responseData.data)
                        CODE_401 -> ExpectedFactory.createError(
                            "Your token cannot access this resource."
                        )
                        CODE_404 -> ExpectedFactory.createError("Resource is missing.")
                        else -> ExpectedFactory.createError(
                            "Unknown error (code: ${responseData.code})."
                        )
                    }
                }
                ).apply {
                    continuation.resume(this)
                }
            }
            continuation.invokeOnCancellation {
                service.cancelRequest(requestId) {}
            }
        }

    private fun getHttpRequest(urlToDownload: String): HttpRequest {
        return HttpRequest.Builder()
            .url(urlToDownload)
            .body(byteArrayOf())
            .headers(
                hashMapOf(
                    Pair(
                        USER_AGENT_KEY,
                        USER_AGENT_VALUE
                    )
                )
            )
            .method(HttpMethod.GET)
            .uaComponents(
                UAComponents.Builder()
                    .sdkIdentifierComponent(SDK_IDENTIFIER)
                    .build()
            )
            .build()
    }
}
