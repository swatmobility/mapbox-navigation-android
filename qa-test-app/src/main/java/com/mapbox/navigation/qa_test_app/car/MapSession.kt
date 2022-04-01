package com.mapbox.navigation.qa_test_app.car

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Configuration
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.ScreenManager
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.androidauto.MapboxCarMap
import com.mapbox.maps.extension.androidauto.MapboxCarMapObserver
import com.mapbox.maps.extension.androidauto.MapboxCarMapSurface
import com.mapbox.maps.extension.style.layers.generated.skyLayer
import com.mapbox.maps.extension.style.layers.properties.generated.SkyType
import com.mapbox.maps.extension.style.sources.generated.rasterDemSource
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.extension.style.terrain.generated.terrain
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.dropin.DropInNavigation
import com.mapbox.navigation.dropin.component.tripsession.TripSessionStarterAction
import com.mapbox.navigation.dropin.extensions.attachCreated
import com.mapbox.navigation.dropin.lifecycle.UIComponent
import com.mapbox.navigation.qa_test_app.utils.Utils
import com.mapbox.navigation.utils.internal.logI
import kotlinx.coroutines.launch

/**
 * Session class for the Mapbox Map sample app for Android Auto.
 */
@OptIn(MapboxExperimental::class, ExperimentalPreviewMapboxNavigationAPI::class)
class MapSession : Session() {

    private val dropInNavigationState by lazy { DropInNavigation.getInstance() }

    private var mapboxCarMapSurface: MapboxCarMapSurface? = null
    private var cameraComponent: CarCameraComponent? = null
    private var locationComponent: CarLocationComponent? = null
    private var mapboxCarMap: MapboxCarMap? = null

    private val sessionCameraObserver = object : MapboxCarMapObserver {
        override fun onAttached(mapboxCarMapSurface: MapboxCarMapSurface) {
            this@MapSession.mapboxCarMapSurface = mapboxCarMapSurface
            loadMapStyle(mapboxCarMapSurface.carContext)

            logI("CarMapShowcase onAttached", "kyle_debug")
            cameraComponent = CarCameraComponent(
                mapboxMap = mapboxCarMapSurface.mapSurface.getMapboxMap(),
                mapPluginProvider = mapboxCarMapSurface.mapSurface,
                cameraViewModel = dropInNavigationState.cameraViewModel,
                locationViewModel = dropInNavigationState.locationViewModel,
                navigationStateViewModel = dropInNavigationState.navigationStateViewModel
            )
            locationComponent = CarLocationComponent(
                context = carContext,
                mapboxMap = mapboxCarMapSurface.mapSurface.getMapboxMap(),
                mapPluginProvider = mapboxCarMapSurface.mapSurface,
                locationViewModel = dropInNavigationState.locationViewModel
            )

            MapboxNavigationApp
                .registerObserver(cameraComponent!!)
                .registerObserver(locationComponent!!)
        }

        override fun onDetached(mapboxCarMapSurface: MapboxCarMapSurface) {
            MapboxNavigationApp
                .unregisterObserver(cameraComponent!!)
                .unregisterObserver(locationComponent!!)

            this@MapSession.cameraComponent = null
            this@MapSession.locationComponent = null
            this@MapSession.mapboxCarMapSurface = null
        }
    }

    private val sessionNavigationObserver = object : UIComponent() {
        override fun onAttached(mapboxNavigation: MapboxNavigation) {
            super.onAttached(mapboxNavigation)

            coroutineScope.launch {
                if (PermissionsManager.areLocationPermissionsGranted(carContext)) {
                    dropInNavigationState.tripSessionStarterViewModel.invoke(
                        TripSessionStarterAction.OnLocationPermission(granted = true)
                    )
                }
            }
        }

        override fun onDetached(mapboxNavigation: MapboxNavigation) {
            super.onDetached(mapboxNavigation)
        }
    }

    init {
        attachCreated(sessionNavigationObserver)
    }

    override fun onCreateScreen(intent: Intent): Screen {
        // The onCreate is guaranteed to be called before onCreateScreen. You can pass the
        // mapboxCarMap to other screens. Each screen can register and unregister observers.
        // This allows you to scope behaviors to sessions, screens, or events.
        val mapScreen = MapScreen(carContext, dropInNavigationState.cameraViewModel)

        return if (carContext.checkSelfPermission(ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            carContext.getCarService(ScreenManager::class.java)
                .push(mapScreen)
            RequestPermissionScreen(carContext)
        } else mapScreen
    }

    override fun onCarConfigurationChanged(newConfiguration: Configuration) {
        loadMapStyle(carContext)
    }

    private fun mapStyleUri(carContext: CarContext): String {
        return if (carContext.isDarkMode) Style.TRAFFIC_NIGHT else Style.TRAFFIC_DAY
    }

    private fun loadMapStyle(carContext: CarContext) {
        logI("MapSession loadMapStyle", "kyle_debug")

        val sunDirection = if (carContext.isDarkMode) listOf(-50.0, 90.2) else listOf(0.0, 0.0)
        mapboxCarMapSurface?.mapSurface?.getMapboxMap()?.loadStyle(
            styleExtension = style(mapStyleUri(carContext)) {
                +rasterDemSource(DEM_SOURCE) {
                    url(TERRAIN_URL_TILE_RESOURCE)
                    tileSize(514)
                }
                +terrain(DEM_SOURCE)
                +skyLayer(SKY_LAYER) {
                    skyType(SkyType.ATMOSPHERE)
                    skyAtmosphereSun(sunDirection)
                }
            }
        )
    }

    init {
        MapboxNavigationApp.attach(lifecycleOwner = this)

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                if (!MapboxNavigationApp.isSetup()) {
                    MapboxNavigationApp.setup(
                        NavigationOptions.Builder(carContext.applicationContext)
                            .accessToken(Utils.getMapboxAccessToken(carContext))
                            .build()
                    )
                }
                mapboxCarMap = MapboxCarMap(MapInitOptions(carContext))
                    .registerObserver(CarMapWidgets())
                mapboxCarMap!!.setGestureHandler(
                    CameraIdleMapGestureHandler(
                        dropInNavigationState.cameraViewModel
                    )
                )
            }

            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)

                mapboxCarMap!!.registerObserver(sessionCameraObserver)
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)

                mapboxCarMap!!.unregisterObserver(sessionCameraObserver)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                mapboxCarMap?.setGestureHandler(null)
                mapboxCarMap?.clearObservers()
                mapboxCarMap = null
            }
        })
    }

    companion object {
        private const val SKY_LAYER = "sky"
        private const val DEM_SOURCE = "mapbox-dem"
        private const val TERRAIN_URL_TILE_RESOURCE = "mapbox://mapbox.mapbox-terrain-dem-v1"
    }
}
