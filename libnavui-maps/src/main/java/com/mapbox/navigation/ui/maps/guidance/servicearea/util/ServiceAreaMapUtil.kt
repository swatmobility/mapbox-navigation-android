package com.mapbox.navigation.ui.maps.guidance.servicearea.util

import android.content.res.Resources
import android.graphics.Bitmap
import android.util.TypedValue
import com.mapbox.navigation.ui.maps.guidance.servicearea.model.ServiceAreaMap
import com.mapbox.navigation.ui.utils.internal.SvgUtil
import java.io.ByteArrayInputStream

object ServiceAreaMapUtil {

    private const val DEFAULT_HEIGHT_FOR_LEGACY_DIP = 36f

    @JvmStatic
    fun toBitmap(
        serviceAreaMap: ServiceAreaMap,
        resources: Resources,
        desiredHeight: Int?
    ): Bitmap? {
        val heightPx = desiredHeight
            ?: TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_HEIGHT_FOR_LEGACY_DIP,
                resources.displayMetrics
            ).toInt()
        val stream = ByteArrayInputStream(serviceAreaMap.serviceMapData)
        return SvgUtil.renderAsBitmapWithHeight(stream, heightPx)
    }
}
