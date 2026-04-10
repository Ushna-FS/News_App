package com.example.newsapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.utils.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AppViewModel @Inject constructor(
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected

    init {
        viewModelScope.launch {
            networkMonitor.observe().collect {
                _isConnected.value = it
            }
        }
    }
}