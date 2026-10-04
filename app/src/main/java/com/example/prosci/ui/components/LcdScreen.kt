package com.example.prosci.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prosci.engine.AngleUnit
import com.example.prosci.engine.Fraction
import com.example.prosci.engine.NumberFormatType
import com.example.prosci.ui.CalcMode
import com.example.prosci.ui.CalculatorUiState
import com.example.prosci.ui.WizardState
import com.example.prosci.ui.theme.LocalProSciColors

@Composable
fun LcdScreen(
    state: CalculatorUiState,
    modifier: Modifier = Modifier
) {
    val colors = LocalProSciColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor"
    )

    val exprScrollState = rememberScrollState()

    // Auto-scroll to cursor position
    LaunchedEffect(state.cursorPos, state.expression) {
        exprScrollState.animateScrollTo(exprScrollState.maxValue)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.lcdBackground)
            .border(2.dp, Color(0x33000000), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("lcd_screen")
    ) {
        val wiz = state.wizardState
        if (wiz != null && state.currentMode != CalcMode.COMP && state.currentMode != CalcMode.CMPLX) {
            WizardLcdContent(wiz = wiz, colors = colors, cursorAlpha = cursorAlpha)
        } else {
            StandardLcdContent(
                state = state,
                colors = colors,
                cursorAlpha = cursorAlpha,
                scrollState = exprScrollState
            )
        }
    }
}

@Composable
private fun StandardLcdContent(
    state: CalculatorUiState,
    colors: com.example.prosci.ui.theme.ProSciColorScheme,
    cursorAlpha: Float,
    scrollState: androidx.compose.foundation.ScrollState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Status row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.isShift) StatusBadge("S", colors.goldShift, colors.lcdBackground)
            if (state.isAlpha) StatusBadge("A", colors.redAlpha, colors.lcdBackground)
            if (state.currentMode != CalcMode.COMP) StatusBadge(state.currentMode.title, colors.lcdText, colors.lcdBackground)
            if (state.isHyp) StatusBadge("hyp", colors.lcdText, colors.lcdBackground)
            if (state.pendingAction.isNotEmpty()) StatusBadge(state.pendingAction.uppercase(), colors.lcdText, colors.lcdBackground)
            if (state.memoryHasValue) StatusBadge("M", colors.lcdText, colors.lcdBackground)

            val angleStr = when (state.angleUnit) {
                AngleUnit.DEG -> "D"
                AngleUnit.RAD -> "R"
                AngleUnit.GRA -> "G"
            }
            StatusBadge(angleStr, colors.lcdText, colors.lcdBackground)

            val fmtStr = when (state.formatSetting.type) {
                NumberFormatType.NORM -> ""
                NumberFormatType.FIX -> "FIX"
                NumberFormatType.SCI -> "SCI"
            }
            if (fmtStr.isNotEmpty()) StatusBadge(fmtStr, colors.lcdText, colors.lcdBackground)

            if (state.isMathIo) StatusBadge("Math", colors.lcdText, colors.lcdBackground)
        }

        // Expression line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val expr = state.expression
            val pos = state.cursorPos.coerceIn(0, expr.length)
            val before = expr.substring(0, pos)
            val after = expr.substring(pos)

            Text(
                text = before.ifEmpty { if (after.isEmpty() && state.resultText.isEmpty()) "" else "" },
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = colors.lcdText
            )
            // Blinking cursor
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height(20.dp)
                    .alpha(cursorAlpha)
                    .background(colors.lcdText)
            )
            Text(
                text = after,
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = colors.lcdText
            )
        }

        // Result / Error line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accentAction,
                    modifier = Modifier.testTag("error_text")
                )
            } else if (state.resultText.isNotEmpty()) {
                val exact = state.exactResult
                if (!state.isShowingDecimal && state.isMathIo && exact?.fraction != null) {
                    FractionView(exact.fraction, colors.lcdText)
                } else {
                    Text(
                        text = state.resultText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.lcdText,
                        textAlign = TextAlign.End,
                        modifier = Modifier.testTag("result_text")
                    )
                }
            }
        }
    }
}

@Composable
fun FractionView(
    fraction: Fraction,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.testTag("fraction_view"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        val num = fraction.num
        val den = fraction.den
        if (num < 0) {
            Text(
                text = "− ",
                fontFamily = FontFamily.Monospace,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = kotlin.math.abs(num).toString(),
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Box(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .height(2.dp)
                    .background(color)
                    .padding(horizontal = 4.dp)
            )
            Text(
                text = den.toString(),
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun WizardLcdContent(
    wiz: WizardState,
    colors: com.example.prosci.ui.theme.ProSciColorScheme,
    cursorAlpha: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Wizard header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = wiz.title + (if (wiz.subType.isNotEmpty()) " · ${wiz.subType}" else ""),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.lcdText.copy(alpha = 0.8f)
            )
            Text(
                text = wiz.stage.uppercase(),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.lcdText.copy(alpha = 0.6f)
            )
        }

        when (wiz.stage) {
            "menu" -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    wiz.menuOptions.forEachIndexed { i, opt ->
                        val isSelected = i == wiz.menuIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelected) colors.lcdText else Color.Transparent, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${i + 1}: $opt",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) colors.lcdBackground else colors.lcdText
                            )
                        }
                    }
                }
                Text(
                    text = "Press 1–${wiz.menuOptions.size} or ▲▼ + = to select",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = colors.lcdText.copy(alpha = 0.7f)
                )
            }
            "prompt" -> {
                val currPrompt = wiz.prompts.getOrNull(wiz.promptIndex) ?: "Value"
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "$currPrompt ?",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.lcdText
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = wiz.buffer,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.lcdText
                        )
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .height(20.dp)
                                .alpha(cursorAlpha)
                                .background(colors.lcdText)
                        )
                    }
                }
                Text(
                    text = "${wiz.promptIndex + 1}/${wiz.prompts.size} · [=] Next · [AC] Reset",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = colors.lcdText.copy(alpha = 0.7f)
                )
            }
            "result" -> {
                val scroll = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 110.dp)
                        .verticalScroll(scroll)
                        .padding(vertical = 2.dp)
                ) {
                    wiz.resultLines.forEach { line ->
                        Text(
                            text = line,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.lcdText
                        )
                    }
                }
                Text(
                    text = "[=] Restart · [AC] Reset",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = colors.lcdText.copy(alpha = 0.7f)
                )
            }
            "calc" -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Base: ${wiz.baseLabel}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.lcdText
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = wiz.buffer,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.lcdText
                        )
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .height(18.dp)
                                .alpha(cursorAlpha)
                                .background(colors.lcdText)
                        )
                    }
                    if (wiz.baseResult.isNotEmpty()) {
                        Text(
                            text = wiz.baseResult,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = colors.lcdText.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String, textColor: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .background(textColor, RoundedCornerShape(3.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = bgColor
        )
    }
}
