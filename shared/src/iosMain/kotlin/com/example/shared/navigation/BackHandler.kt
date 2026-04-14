package com.example.shared.navigation

import androidx.compose.runtime.Composable

@Composable
actual fun handleBackPress(onBack: () -> Unit) {
    // no-op
}

actual fun exitApp() {
    // Usually do nothing (Apple discourages force exit)
}