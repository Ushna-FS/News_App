package com.example.shared.data.config

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ApiKeyManager(private val defaultKey: String) {

    companion object {
        const val REMOTE_CONFIG_KEY = "apiKeys"
    }

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady
    private var keys: List<String> = emptyList()
    private var currentIndex = 0

    val currentKey: String
        get() {
            val key = keys.getOrElse(currentIndex) { keys.last() }
            println("API DEBUG -> Using key index=$currentIndex key=${key.take(6)}***")
            return key
        }

    suspend fun fetchKeys() {
        try {
            val remoteConfig = Firebase.remoteConfig


            remoteConfig.fetchAndActivate()

            val csv = remoteConfig.getValue(REMOTE_CONFIG_KEY).asString()

            val fetchedKeys = parseKeys(csv)

            keys = (fetchedKeys.ifEmpty { listOf(defaultKey) }).distinct()
            currentIndex = 0
            _isReady.value = true
            println("API DEBUG -> Keys loaded: ${keys.size}")
            keys.forEachIndexed { index, key ->
                println("API DEBUG -> [$index] ${key.take(6)}***")
            }

        } catch (_: Exception) {
            // fallback already handled by defaultKey
            keys = listOf(defaultKey)
            println("API DEBUG -> Fetch failed, using default key")
            _isReady.value = true
        }
    }

    fun rotateToNextKey(): Boolean {
        if (keys.isEmpty()) return false

        val nextIndex = currentIndex + 1

        if (nextIndex >= keys.size) {
            println("API DEBUG -> All keys exhausted")
            return false
        }

        currentIndex = nextIndex

        println("API DEBUG -> Rotated to index=$currentIndex key=${keys[currentIndex].take(6)}***")

        return true
    }

    private fun parseKeys(csv: String): List<String> {
        return csv
            .replace("[", "")
            .replace("]", "")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }


}