package com.example.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.utils.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NetworkViewModel(
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _isConnected = MutableStateFlow(networkMonitor.isConnected())
    val isConnected: StateFlow<Boolean> = _isConnected

    init {
        observeNetwork()
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.observe().collect { connected ->
                _isConnected.value = connected
            }
        }
    }
}