package com.mapbox.navigation.ui.maps.guidance.servicearea.util

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.mapbox.navigation.testing.FileUtils.loadFixture
import com.mapbox.navigation.ui.maps.guidance.servicearea.model.ServiceAreaMap
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.N])
class ServiceAreaMapUtilTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun toBitmap() {
        val svgByteArray = loadFixture("test_svg.svg").toByteArray()
        val serviceAreaMap = ServiceAreaMap("url", svgByteArray)

        val result = ServiceAreaMapUtil.toBitmap(serviceAreaMap, context.resources, 64)

        assertEquals(64, result!!.width)
        assertEquals(64, result.height)
    }

    @Test
    fun toBitmap_defaultHeight() {
        val svgByteArray = loadFixture("test_svg.svg").toByteArray()
        val serviceAreaMap = ServiceAreaMap("url", svgByteArray)

        val result = ServiceAreaMapUtil.toBitmap(serviceAreaMap, context.resources, null)

        assertEquals(36, result!!.width)
        assertEquals(36, result.height)
    }
}
