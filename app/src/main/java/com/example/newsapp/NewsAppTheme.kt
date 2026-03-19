package com.example.newsapp

import com.example.newsapp.ui.theme.NewsTypography
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.newsapp.ui.theme.DarkColors
import com.example.newsapp.ui.theme.LightColors

@Composable
fun NewsAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = NewsTypography,
        content = content
    )
}