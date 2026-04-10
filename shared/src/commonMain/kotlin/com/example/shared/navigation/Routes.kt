package com.example.shared.navigation

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
sealed class Routes {
    @Serializable
    data object Splash : Routes()

    @Serializable
    data object Home : Routes()

    @Serializable
    data object Discover : Routes()

    @Serializable
    data object Bookmarks : Routes()

    @Serializable
    data object Login : Routes()

    @Serializable
    data object Signup : Routes()

    @Serializable
    data object MainTabs : Routes()

    @Serializable
    data class ArticleDetail(val articleJson: String) : Routes()
}

// reusable json instance
val AppJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

// Helper functions
inline fun <reified T> encodeToJsonString(value: T): String {
    return AppJson.encodeToString(value)
}

inline fun <reified T> decodeFromJsonString(jsonString: String): T {
    return AppJson.decodeFromString(jsonString)
}