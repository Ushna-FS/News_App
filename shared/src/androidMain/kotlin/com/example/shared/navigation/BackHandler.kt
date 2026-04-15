package com.example.shared.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import kotlin.system.exitProcess
@Composable
actual fun handleBackPress(onBack: () -> Unit) {
    BackHandler {
        onBack()
    }
}


actual fun exitApp() {
    exitProcess(0)
}