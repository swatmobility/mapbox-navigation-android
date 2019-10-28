package com.mapbox.services.android.navigation.v5.internal.navigation

import android.annotation.SuppressLint
import android.os.Parcelable
import com.google.gson.Gson
import com.mapbox.navigation.base.metrics.DirectionsMetrics
import com.mapbox.navigation.base.metrics.MetricEvent
import com.mapbox.services.android.navigation.v5.internal.navigation.metrics.NavigationPerformanceEvent

@SuppressLint("ParcelCreator")
internal data class RouteRetrievalEvent(
    private val elapsedTime: Double,
    private val routeUuid: String,
    @Transient private val sessionId: String,
    @Transient override var metadata: NavigationPerformanceMetadata
) : NavigationPerformanceEvent(sessionId, DirectionsMetrics.ROUTE_RETRIEVAL, metadata), MetricEvent, Parcelable {

    companion object {
        private const val ELAPSED_TIME_NAME = "elapsed_time"
        private const val ROUTE_UUID_NAME = "route_uuid"
    }

    init {
        addCounter(DoubleCounter(ELAPSED_TIME_NAME, elapsedTime))
        addAttribute(Attribute(ROUTE_UUID_NAME, routeUuid))
    }

    override fun toJson(gson: Gson): String = gson.toJson(this)

    override val metric: String
        get() = DirectionsMetrics.ROUTE_RETRIEVAL
}
