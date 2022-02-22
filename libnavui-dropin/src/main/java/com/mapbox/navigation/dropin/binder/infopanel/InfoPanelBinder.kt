package com.mapbox.navigation.dropin.binder.infopanel

import android.view.ViewGroup
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.dropin.R
import com.mapbox.navigation.dropin.binder.UIBinder
import com.mapbox.navigation.dropin.util.CompositeMapboxNavigationObserver

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
internal class InfoPanelBinder(
    private val headerBinder: UIBinder,
    private val contentBinder: UIBinder?
) : UIBinder {

    override fun bind(viewGroup: ViewGroup): MapboxNavigationObserver {
        return CompositeMapboxNavigationObserver().apply {
            headerBinder
                .bind(viewGroup.findViewById(R.id.infoPanelHeader))
                .also { add(it) }

            contentBinder
                ?.bind(viewGroup.findViewById(R.id.infoPanelContent))
                ?.also { add(it) }
        }
    }
}
