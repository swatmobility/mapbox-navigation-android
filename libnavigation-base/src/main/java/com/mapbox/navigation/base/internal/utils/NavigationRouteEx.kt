@file:JvmName("NavigationRouteEx")

package com.mapbox.navigation.base.internal.utils

import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigator.RouterOrigin

val NavigationRoute.routeId: String get() = nativeRoute.routeId

val NavigationRoute.routerOrigin: RouterOrigin get() = nativeRoute.routerOrigin
