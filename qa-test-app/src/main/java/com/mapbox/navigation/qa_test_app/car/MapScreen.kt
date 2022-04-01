package com.mapbox.navigation.qa_test_app.car

import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarColor
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.androidauto.DefaultMapboxCarMapGestureHandler
import com.mapbox.maps.extension.androidauto.MapboxCarMap
import com.mapbox.navigation.dropin.component.camera.CameraAction
import com.mapbox.navigation.dropin.component.camera.CameraViewModel
import com.mapbox.navigation.qa_test_app.R

/**
 * Simple demo of how to show a Mapbox Map on the Android Auto screen.
 */
@OptIn(MapboxExperimental::class)
class MapScreen(
  carContext: CarContext,
  private val cameraViewModel: CameraViewModel
) : Screen(carContext) {
  private var isInPanMode: Boolean = false

  override fun onGetTemplate(): Template {
    val builder = NavigationTemplate.Builder()
      .setBackgroundColor(CarColor.SECONDARY)

    builder.setActionStrip(
      ActionStrip.Builder()
        .addAction(
          Action.Builder()
            .setIcon(
              CarIcon.Builder(
                IconCompat.createWithResource(
                  carContext,
                  android.R.drawable.ic_menu_mylocation
                )
              ).build()
            )
            .setOnClickListener {
                // Recenter button
                cameraViewModel.invoke(CameraAction.ToFollowing)
            }
            .build()
        )
        .build()
    )
    // Set the map action strip with the pan and zoom buttons.
    val panIconBuilder = CarIcon.Builder(
      IconCompat.createWithResource(
        carContext,
        R.drawable.ic_pan_24
      )
    )
    if (isInPanMode) {
      panIconBuilder.setTint(CarColor.BLUE)
    }
    builder.setMapActionStrip(
      ActionStrip.Builder()
        .addAction(
          Action.Builder(Action.PAN)
            .setIcon(panIconBuilder.build())
            .build()
        )
        .addAction(
          Action.Builder()
            .setIcon(
              CarIcon.Builder(
                IconCompat.createWithResource(
                  carContext,
                  R.drawable.ic_zoom_out_24
                )
              ).build()
            )
            .setOnClickListener {
                // TODO Add zoom out action
//                cameraViewModel.invoke(CameraAction.ToFollowing)
            }
            .build()
        )
        .addAction(
          Action.Builder()
            .setIcon(
              CarIcon.Builder(
                IconCompat.createWithResource(
                  carContext,
                  R.drawable.ic_zoom_in_24
                )
              ).build()
            )
            .setOnClickListener {
                // TODO Add zoom in action
//                cameraViewModel.invoke(CameraAction.ToFollowing)
            }
            .build()
        )
        .build()
    )

    // When the user enters the pan mode, remind the user that they can exit the pan mode by
    // pressing the select button again.
    builder.setPanModeListener { isInPanMode: Boolean ->
      if (isInPanMode) {
        CarToast.makeText(
          carContext,
          "Press Select to exit the pan mode",
          CarToast.LENGTH_LONG
        ).show()
      }
      this.isInPanMode = isInPanMode
      invalidate()
    }

    return builder.build()
  }
}
