package com.example.shared

import com.example.shared.ui.theme.NewsTypography
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.shared.ui.theme.DarkColors
import com.example.shared.ui.theme.LightColors

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