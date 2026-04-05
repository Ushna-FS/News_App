package com.example.shared.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

actual class NetworkMonitor {
    actual fun observe(): Flow<Boolean> {
        return flowOf(true)
    }
    actual fun isConnected(): Boolean {
        return true
    }
}