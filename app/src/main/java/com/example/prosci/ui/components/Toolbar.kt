package com.example.prosci.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // MODE button
        ToolbarButton(
            text = "MODE",
            isAccent = true,
            testTag = "toolbar_mode",
            onClick = { viewModel.openModeDialog() }
        )

        // Angle unit toggle
        ToolbarButton(
            text = state.angleUnit.name,
            testTag = "toolbar_angle",
            onClick = { viewModel.cycleAngleUnit() }
        )

        // Number format toggle
        ToolbarButton(
            text = state.formatSetting.toString(),
            testTag = "toolbar_format",
            onClick = { viewModel.cycleNumberFormat() }
        )

        // MathIO / LineIO
        ToolbarButton(
            text = if (state.isMathIo) "MathIO" else "LineIO",
            testTag = "toolbar_math_io",
            onClick = { viewModel.toggleMathIo() }
        )

        // S <=> D toggle
        ToolbarButton(
            text = "S⇔D",
            testTag = "toolbar_sd",
            onClick = { viewModel.toggleStandardDecimal() }
        )

        // Theme cycle
        ToolbarButton(
            text = "🎨",
            testTag = "toolbar_theme",
            onClick = { viewModel.cycleTheme() }
        )

        // Copy button
        ToolbarButton(
            text = if (state.copiedToast) "✓" else "Copy",
            testTag = "toolbar_copy",
            onClick = { viewModel.copyResult() }
        )

        // History
        ToolbarButton(
            text = "History",
            testTag = "toolbar_history",
            onClick = { viewModel.openHistoryDialog() }
        )

        // Reference / Help
        ToolbarButton(
            text = "ℹ",
            testTag = "toolbar_info",
            onClick = { viewModel.openHelpDialog() }
        )
    }
}

@Composable
private fun ToolbarButton(
    text: String,
    onClick: () -> Unit,
    testTag: String,
    isAccent: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = LocalProSciColors.current
    val bg = if (isAccent) colors.accentAction else colors.functionKeyBackground
    val fg = if (isAccent) Color.White else colors.functionKeyText

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, colors.borderLight, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg
        )
    }
}
