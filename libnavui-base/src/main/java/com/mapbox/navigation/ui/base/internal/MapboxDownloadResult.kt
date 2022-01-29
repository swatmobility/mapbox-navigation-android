package com.mapbox.navigation.ui.base.internal

data class MapboxDownloadResult(val url: String, val data: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MapboxDownloadResult

        if (url != other.url) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}
