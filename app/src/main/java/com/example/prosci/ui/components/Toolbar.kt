package com.example.prosci.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prosci.ui.CalculatorUiState
import com.example.prosci.ui.CalculatorViewModel
import com.example.prosci.ui.theme.LocalProSciColors

@Composable
fun CalculatorToolbar(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalProSciColors.current
    val isLightMode = state.themeId == "light"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: ProSci logo & Subtitle
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            val titleText = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = colors.functionKeyText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                ) {
                    append("Pro")
                }
                withStyle(
                    style = SpanStyle(
                        color = colors.accentAction,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                ) {
                    append("Sci")
                }
            }

            Text(
                text = titleText,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.5.sp,
                modifier = Modifier.testTag("app_title")
            )

            Text(
                text = "fx-991ES • NATURAL-V.P.A.M.",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = colors.functionKeyText.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.8.sp
            )
        }

        // Right: 3 squircle action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Day / Night mode toggle
            HeaderIconButton(
                icon = if (isLightMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = if (isLightMode) "Switch to Night Mode" else "Switch to Day Mode",
                testTag = "toolbar_theme_toggle",
                colors = colors,
                onClick = {
                    if (isLightMode) {
                        viewModel.setTheme("classic")
                    } else {
                        viewModel.setTheme("light")
                    }
                }
            )

            // Palette / Cycle Theme
            HeaderIconButton(
                icon = Icons.Default.Palette,
                contentDescription = "Change Theme",
                testTag = "toolbar_palette",
                colors = colors,
                onClick = { viewModel.cycleTheme() }
            )

            // Settings / Mode & Help
            HeaderIconButton(
                icon = Icons.Default.Settings,
                contentDescription = "Settings and Modes",
                testTag = "toolbar_settings",
                colors = colors,
                onClick = { viewModel.openModeDialog() }
            )
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    testTag: String,
    colors: com.example.prosci.ui.theme.ProSciColorScheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.functionKeyBackground)
            .border(1.dp, colors.borderLight, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.functionKeyText,
            modifier = Modifier.size(18.dp)
        )
    }
}
