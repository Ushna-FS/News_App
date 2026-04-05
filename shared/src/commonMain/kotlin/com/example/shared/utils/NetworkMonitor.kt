package com.example.shared.utils

import kotlinx.coroutines.flow.Flow

expect class NetworkMonitor {

    fun observe(): Flow<Boolean>
    fun isConnected(): Boolean
}