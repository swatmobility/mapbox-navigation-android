package com.mapbox.androidauto

import com.mapbox.navigation.utils.internal.logE
import com.mapbox.navigation.utils.internal.logI

object AndroidAutoLog {
    fun logAndroidAuto(message: String) {
        logI(
            msg = "${Thread.currentThread().id}: $message",
            category = "MapboxAndroidAuto"
        )
//        LoggerProvider.logger.i(
//            tag = Tag("MapboxAndroidAuto"),
//            msg = Message("${Thread.currentThread().id}: $message")
//        )
    }

    fun logAndroidAutoFailure(message: String, throwable: Throwable? = null) {
        logE(
            msg = "${Thread.currentThread().id}: $message ${throwable?.message}",
            category = "MapboxAndroidAuto"
        )
//        LoggerProvider.logger.e(
//            tag = Tag("MapboxAndroidAuto"),
//            msg = Message("${Thread.currentThread().id}: $message"),
//            tr = throwable
//        )
    }
}

fun logAndroidAuto(message: String) {
    AndroidAutoLog.logAndroidAuto(message)
}

fun logAndroidAutoFailure(message: String, throwable: Throwable? = null) {
    AndroidAutoLog.logAndroidAutoFailure(message, throwable)
}
