package com.example.newsapp.ui.theme

import androidx.compose.material3.*

/* ---------- Light Theme ---------- */

val LightColors = lightColorScheme(

    primary = BlueMainLight,
    onPrimary = White,

    secondary = LightBlueLight,
    onSecondary = Black,

    background = White,
    onBackground = Black,

    surface = GrayLightLight,
    onSurface = Black,

    error = RedLight,
    onError = White,

    tertiary = GreenLight,
    onTertiary = White

)

/* ---------- Dark Theme ---------- */

val DarkColors = darkColorScheme(

    primary = BlueMainDark,
    onPrimary = WhiteDark,

    secondary = LightBlueDark,
    onSecondary = BlackDark,

    background = WhiteDark,
    onBackground = BlackDark,

    surface = GrayLightDark,
    onSurface = White,

    error = RedDark,
    onError = Black
)

