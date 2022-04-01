package com.mapbox.navigation.qa_test_app.car

import androidx.car.app.CarAppService
import androidx.car.app.validation.HostValidator
import com.mapbox.navigation.qa_test_app.car.MainCarSession

class MainCarAppService : CarAppService() {
    override fun createHostValidator() = HostValidator.ALLOW_ALL_HOSTS_VALIDATOR

    override fun onCreateSession() = MainCarSession()
}
