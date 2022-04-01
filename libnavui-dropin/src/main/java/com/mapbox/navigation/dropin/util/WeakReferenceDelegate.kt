package com.mapbox.navigation.dropin.util

import java.lang.ref.WeakReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
* Creates a new instance of the [ReadWriteProperty] that uses [WeakReference]
 * as a backing field.
*/
internal fun <T> weakReference(initialValue: T? = null): ReadWriteProperty<Any?, T?> {
    return object : ReadWriteProperty<Any?, T?> {

        private var ref = WeakReference<T?>(initialValue)

        override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
            return ref.get()
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
            ref = WeakReference(value)
        }
    }
}
