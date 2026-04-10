package com.example.newsapp.utils

enum class Category(val displayName: String) {
    All("All"),
    Technology("Technology"),
    Business("Business"),
    Finance("Finance"),
    Energy("Energy"),
    Politics("Politics"),
    Health("Health"),
    Sports("Sports");

    companion object {
        val list: List<Category> = values().toList()             // List<Category> for enum usage
        val names: List<String> = values().map { it.displayName }  // For easy iteration
    }
}