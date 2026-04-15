package com.example.shared.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

/* ---------- Light Theme ---------- */

val LightColors = lightColorScheme(

    primary = BlueMainLight,
    onPrimary = White,
    primaryContainer = searchBarLight,

    secondary = LightBlueLight,
    onSecondary = Black,

    background = GrayLightLight,
    onBackground = Black,

    surface = White,
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
    primaryContainer = searchBarDark,

    secondary = LightBlueDark,
    onSecondary = BlackDark,

    background = WhiteDark,
    onBackground = BlackDark,

    surface = GrayLightDark,
    onSurface = White,

    error = RedDark,
    onError = Black
)