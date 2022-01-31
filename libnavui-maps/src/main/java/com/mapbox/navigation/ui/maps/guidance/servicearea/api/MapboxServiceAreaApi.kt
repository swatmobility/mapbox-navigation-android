package com.mapbox.navigation.ui.maps.guidance.servicearea.api

import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.navigation.ui.base.internal.MapboxUtilDownloader
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.maps.guidance.servicearea.model.ServiceAreaMap
import com.mapbox.navigation.ui.maps.guidance.servicearea.model.ServiceAreaMapError
import com.mapbox.navigation.utils.internal.InternalJobControlFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapboxServiceAreaApi {

    private val jobController by lazy { InternalJobControlFactory.createIOScopeJobControl() }

    // perhaps input should be step intersection
    fun fetchServiceAreaMap(consumer: MapboxNavigationConsumer<Expected<ServiceAreaMapError, ServiceAreaMap>>) {
        jobController.scope.launch {
            val payload = MapboxUtilDownloader.downloadPayload("https://br-temp.s3.amazonaws.com/BM01009A.svg").run {
                this.fold( {
                    ExpectedFactory.createError<ServiceAreaMapError, ServiceAreaMap>(ServiceAreaMapError(it, null))
                },{
                    ExpectedFactory.createValue<ServiceAreaMapError, ServiceAreaMap>(ServiceAreaMap(it.url, it.data))
                })
            }
            jobController.scope.launch(Dispatchers.Main) {
                consumer.accept(payload)
            }
        }
    }
}
