package com.example.prosci.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalProSciColors = staticCompositionLocalOf { ProSciThemes.Classic }

@Composable
fun ProSciTheme(
    colorScheme: ProSciColorScheme = ProSciThemes.Classic,
    content: @Composable () -> Unit
) {
    val m3Colors = if (colorScheme == ProSciThemes.Light) {
        lightColorScheme(
            primary = colorScheme.accentAction,
            background = colorScheme.background,
            surface = colorScheme.cardBackground
        )
    } else {
        darkColorScheme(
            primary = colorScheme.accentAction,
            background = colorScheme.background,
            surface = colorScheme.cardBackground
        )
    }

    CompositionLocalProvider(LocalProSciColors provides colorScheme) {
        MaterialTheme(
            colorScheme = m3Colors,
            content = content
        )
    }
}
