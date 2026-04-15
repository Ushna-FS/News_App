package com.example.shared.navigation

import androidx.compose.runtime.Composable

@Composable
expect fun handleBackPress(onBack: () -> Unit)

expect fun exitApp()