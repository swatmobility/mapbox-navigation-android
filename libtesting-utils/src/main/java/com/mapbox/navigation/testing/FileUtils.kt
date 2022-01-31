package com.mapbox.navigation.testing

object FileUtils {
    fun loadFixture(fileName: String): String {
        return javaClass.classLoader?.getResourceAsStream(fileName)
            ?.bufferedReader()
            ?.use { it.readText() }!!
    }
}
