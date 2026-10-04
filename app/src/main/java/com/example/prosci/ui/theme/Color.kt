package com.example.prosci.ui.theme

import androidx.compose.ui.graphics.Color

data class ProSciColorScheme(
    val id: String,
    val name: String,
    val background: Color,
    val cardBackground: Color,
    val lcdBackground: Color,
    val lcdText: Color,
    val functionKeyBackground: Color,
    val functionKeyText: Color,
    val numberKeyBackground: Color,
    val numberKeyText: Color,
    val accentAction: Color,
    val goldShift: Color,
    val redAlpha: Color,
    val borderLight: Color
)

object ProSciThemes {
    val Classic = ProSciColorScheme(
        id = "classic",
        name = "Classic fx",
        background = Color(0xFF141518),
        cardBackground = Color(0xFF23262B),
        lcdBackground = Color(0xFFC5D1B4),
        lcdText = Color(0xFF1C2616),
        functionKeyBackground = Color(0xFF3E434D),
        functionKeyText = Color(0xFFF1F1F1),
        numberKeyBackground = Color(0xFFDCDCDC),
        numberKeyText = Color(0xFF1B1D22),
        accentAction = Color(0xFFD6362D),
        goldShift = Color(0xFFF2B632),
        redAlpha = Color(0xFFFF6B5E),
        borderLight = Color(0x33FFFFFF)
    )

    val Midnight = ProSciColorScheme(
        id = "midnight",
        name = "Midnight",
        background = Color(0xFF05070D),
        cardBackground = Color(0xFF121626),
        lcdBackground = Color(0xFF0E1A2E),
        lcdText = Color(0xFF7FD1FF),
        functionKeyBackground = Color(0xFF252C47),
        functionKeyText = Color(0xFFCFE6FF),
        numberKeyBackground = Color(0xFF313A5C),
        numberKeyText = Color(0xFFFFFFFF),
        accentAction = Color(0xFF3D7BFF),
        goldShift = Color(0xFFFFCC00),
        redAlpha = Color(0xFFFF6B81),
        borderLight = Color(0x334466AA)
    )

    val Light = ProSciColorScheme(
        id = "light",
        name = "Light Studio",
        background = Color(0xFFE9ECF2),
        cardBackground = Color(0xFFDDE2EC),
        lcdBackground = Color(0xFFDBE6D0),
        lcdText = Color(0xFF14210F),
        functionKeyBackground = Color(0xFFC2C8D6),
        functionKeyText = Color(0xFF1D2230),
        numberKeyBackground = Color(0xFFFFFFFF),
        numberKeyText = Color(0xFF1D2230),
        accentAction = Color(0xFFC4271B),
        goldShift = Color(0xFFB67F00),
        redAlpha = Color(0xFFC4271B),
        borderLight = Color(0x22000000)
    )

    val Neon = ProSciColorScheme(
        id = "neon",
        name = "Cyber Neon",
        background = Color(0xFF08000F),
        cardBackground = Color(0xFF18032E),
        lcdBackground = Color(0xFF0B0016),
        lcdText = Color(0xFF00FFD0),
        functionKeyBackground = Color(0xFF2A0B4D),
        functionKeyText = Color(0xFFFF4DF0),
        numberKeyBackground = Color(0xFF431074),
        numberKeyText = Color(0xFFFFFFFF),
        accentAction = Color(0xFFFF2D95),
        goldShift = Color(0xFFFFD700),
        redAlpha = Color(0xFFFF3366),
        borderLight = Color(0x44FF2D95)
    )

    val Rose = ProSciColorScheme(
        id = "rose",
        name = "Rose Gold",
        background = Color(0xFF221519),
        cardBackground = Color(0xFF3A2127),
        lcdBackground = Color(0xFFF6E6E8),
        lcdText = Color(0xFF4A1F27),
        functionKeyBackground = Color(0xFF6E3942),
        functionKeyText = Color(0xFFFFFFFF),
        numberKeyBackground = Color(0xFFFBEAEC),
        numberKeyText = Color(0xFF4A1F27),
        accentAction = Color(0xFF9E3447),
        goldShift = Color(0xFFD4AF37),
        redAlpha = Color(0xFFE55A70),
        borderLight = Color(0x33FFAABB)
    )

    val Contrast = ProSciColorScheme(
        id = "contrast",
        name = "High Contrast",
        background = Color(0xFF000000),
        cardBackground = Color(0xFF111111),
        lcdBackground = Color(0xFFFFFFFF),
        lcdText = Color(0xFF000000),
        functionKeyBackground = Color(0xFF222222),
        functionKeyText = Color(0xFFFFFF00),
        numberKeyBackground = Color(0xFFFFFFFF),
        numberKeyText = Color(0xFF000000),
        accentAction = Color(0xFFFFFF00),
        goldShift = Color(0xFFFFFF00),
        redAlpha = Color(0xFFFF4444),
        borderLight = Color(0x66FFFFFF)
    )

    val AllThemes = listOf(Classic, Midnight, Light, Neon, Rose, Contrast)

    fun getThemeById(id: String): ProSciColorScheme {
        return AllThemes.find { it.id == id } ?: Classic
    }
}
