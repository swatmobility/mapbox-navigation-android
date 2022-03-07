package com.mapbox.navigation.core.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.utils.internal.logI

@ExperimentalPreviewMapboxNavigationAPI
internal class CarAppLifecycleOwner : LifecycleOwner {

    // Keeps track of the activities created and foregrounded
    private var activitiesCreated = 0
    private var activitiesForegrounded = 0

    // Keeps track of the car session created and foregrounded
    private var lifecycleCreated = 0
    private var lifecycleForegrounded = 0

    // Keeps track of the activities changing configurations
    private var createdChangingConfiguration = 0
    private var foregroundedChangingConfiguration = 0

    private val lifecycleRegistry = LifecycleRegistry(this)
        .apply { currentState = Lifecycle.State.INITIALIZED }

    @VisibleForTesting
    internal val startedReferenceCounter = object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            if (createdChangingConfiguration > 0) {
                createdChangingConfiguration--
            } else {
                lifecycleCreated++
                logI(TAG, "LifecycleOwner ($owner) onCreate")
                if (activitiesCreated == 0 && lifecycleCreated == 1) {
                    changeState(Lifecycle.State.STARTED)
                }
            }
        }

        override fun onStart(owner: LifecycleOwner) {
            if (foregroundedChangingConfiguration > 0) {
                foregroundedChangingConfiguration--
            } else {
                lifecycleForegrounded++
                logI(TAG, "LifecycleOwner ($owner) onStart")
                if (activitiesForegrounded == 0 && lifecycleForegrounded == 1) {
                    changeState(Lifecycle.State.RESUMED)
                }
            }
        }

        override fun onStop(owner: LifecycleOwner) {
            if (owner.isChangingConfigurations()) {
                foregroundedChangingConfiguration++
            } else {
                lifecycleForegrounded--
                logI(TAG, "LifecycleOwner ($owner) onStop")
                if (activitiesForegrounded == 0 && lifecycleForegrounded == 0) {
                    changeState(Lifecycle.State.STARTED)
                }
            }
        }

        override fun onDestroy(owner: LifecycleOwner) {
            if (owner.isChangingConfigurations()) {
                createdChangingConfiguration++
            } else {
                lifecycleCreated--
                logI(TAG, "LifecycleOwner ($owner) onDestroy")
                if (activitiesCreated == 0 && lifecycleCreated == 0) {
                    changeState(Lifecycle.State.CREATED)
                }
            }
        }

        private fun LifecycleOwner.isChangingConfigurations(): Boolean =
            (this is Activity && this.isChangingConfigurations) ||
                (this is Fragment && this.activity?.isChangingConfigurations == true)
    }

    @VisibleForTesting
    internal val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (createdChangingConfiguration > 0) {
                createdChangingConfiguration--
            } else {
                activitiesCreated++
                logI(TAG, "app onActivityCreated")
                if (lifecycleCreated == 0 && activitiesCreated == 1) {
                    changeState(Lifecycle.State.STARTED)
                }
            }
        }

        override fun onActivityStarted(activity: Activity) {
            if (foregroundedChangingConfiguration > 0) {
                foregroundedChangingConfiguration--
            } else {
                activitiesForegrounded++
                logI(TAG, "app onActivityStarted")
                if (lifecycleForegrounded == 0 && activitiesForegrounded == 1) {
                    changeState(Lifecycle.State.RESUMED)
                }
            }
        }

        override fun onActivityResumed(activity: Activity) {
            logI(TAG, "app onActivityResumed")
        }

        override fun onActivityPaused(activity: Activity) {
            logI(TAG, "app onActivityPaused")
        }

        override fun onActivityStopped(activity: Activity) {
            if (activity.isChangingConfigurations) {
                foregroundedChangingConfiguration++
            } else {
                activitiesForegrounded--
                logI(TAG, "app onActivityStopped")
                if (lifecycleForegrounded == 0 && activitiesForegrounded == 0) {
                    changeState(Lifecycle.State.STARTED)
                }
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            logI(TAG, "app onActivitySaveInstanceState")
        }

        override fun onActivityDestroyed(activity: Activity) {
            if (activity.isChangingConfigurations) {
                createdChangingConfiguration++
            } else {
                activitiesCreated--
                logI(TAG, "app onActivityDestroyed")
                if (lifecycleCreated == 0 && activitiesCreated == 0) {
                    changeState(Lifecycle.State.CREATED)
                }
            }
        }
    }

    private fun changeState(state: Lifecycle.State) {
        if (lifecycleRegistry.currentState != state) {
            lifecycleRegistry.currentState = state
            logI(TAG, "changeState ${lifecycleRegistry.currentState}")
        }
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    internal fun attachAllActivities(application: Application) {
        logI(TAG, "attachAllActivities")
        application.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks)
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    fun attach(lifecycleOwner: LifecycleOwner) {
        logI(TAG, "attach")
        lifecycleOwner.lifecycle.addObserver(startedReferenceCounter)
    }

    fun detach(lifecycleOwner: LifecycleOwner) {
        logI(TAG, "detach")
        lifecycleOwner.lifecycle.removeObserver(startedReferenceCounter)
        val currentState = lifecycleOwner.lifecycle.currentState
        if (currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            startedReferenceCounter.onPause(lifecycleOwner)
        }
        if (currentState.isAtLeast(Lifecycle.State.STARTED)) {
            startedReferenceCounter.onStop(lifecycleOwner)
        }
        if (currentState.isAtLeast(Lifecycle.State.CREATED)) {
            startedReferenceCounter.onDestroy(lifecycleOwner)
        }
    }

    fun isConfigurationChanging(): Boolean =
        createdChangingConfiguration > 0 || foregroundedChangingConfiguration > 0

    private companion object {
        private const val TAG = "MbxCarAppLifecycleOwner"
    }
}