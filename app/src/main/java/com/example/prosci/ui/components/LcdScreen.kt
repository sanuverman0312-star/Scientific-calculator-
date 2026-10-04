package com.example.prosci.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextLayoutResult
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
    onOpenHistory: () -> Unit = {},
    onCursorMoved: (Int) -> Unit = {},
    onSelectMenuIndex: (Int) -> Unit = {},
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(152.dp) // Fixed comfortable responsive height: does not shrink/grow with menus
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colors.lcdBackground,
                        colors.lcdBackground.copy(alpha = 0.92f)
                    )
                )
            )
            .border(
                2.dp,
                colors.borderLight.copy(alpha = 0.8f),
                RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("lcd_screen")
    ) {
        val wiz = state.wizardState
        if (wiz != null && state.currentMode != CalcMode.COMP && state.currentMode != CalcMode.CMPLX) {
            WizardLcdContent(
                wiz = wiz,
                colors = colors,
                cursorAlpha = cursorAlpha,
                onSelectMenuIndex = onSelectMenuIndex
            )
        } else {
            StandardLcdContent(
                state = state,
                colors = colors,
                cursorAlpha = cursorAlpha,
                scrollState = exprScrollState,
                onCursorMoved = onCursorMoved,
                onOpenHistory = onOpenHistory
            )
        }
    }
}

@Composable
private fun StandardLcdContent(
    state: CalculatorUiState,
    colors: com.example.prosci.ui.theme.ProSciColorScheme,
    cursorAlpha: Float,
    scrollState: androidx.compose.foundation.ScrollState,
    onCursorMoved: (Int) -> Unit,
    onOpenHistory: () -> Unit
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    // Auto-scroll horizontally to cursor position
    LaunchedEffect(state.cursorPos, state.expression, textLayoutResult) {
        val layout = textLayoutResult
        if (layout != null && state.expression.isNotEmpty()) {
            val safePos = state.cursorPos.coerceIn(0, state.expression.length)
            val cursorRect = layout.getCursorRect(safePos)
            val currentScroll = scrollState.value
            val viewportWidth = scrollState.viewportSize
            if (viewportWidth > 0) {
                if (cursorRect.right > currentScroll + viewportWidth - 20) {
                    scrollState.animateScrollTo((cursorRect.right - viewportWidth + 30).toInt().coerceAtLeast(0))
                } else if (cursorRect.left < currentScroll + 20) {
                    scrollState.animateScrollTo((cursorRect.left - 20).toInt().coerceAtLeast(0))
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Status row: Badges on Left, NATURAL-V.P.A.M. on Right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Angle badge
                val angleStr = when (state.angleUnit) {
                    AngleUnit.DEG -> "D"
                    AngleUnit.RAD -> "R"
                    AngleUnit.GRA -> "G"
                }
                StatusPillBadge(angleStr, colors)

                // Math badge
                if (state.isMathIo) {
                    StatusPillBadge("Math", colors)
                }

                // Other status flags
                if (state.isShift) StatusPillBadge("S", colors, colors.goldShift)
                if (state.isAlpha) StatusPillBadge("A", colors, colors.redAlpha)
                if (state.currentMode != CalcMode.COMP) StatusPillBadge(state.currentMode.title, colors)
                if (state.isHyp) StatusPillBadge("hyp", colors)
                if (state.pendingAction.isNotEmpty()) StatusPillBadge(state.pendingAction.uppercase(), colors)
                if (state.memoryHasValue) StatusPillBadge("M", colors)

                val fmtStr = when (state.formatSetting.type) {
                    NumberFormatType.NORM -> ""
                    NumberFormatType.FIX -> "FIX"
                    NumberFormatType.SCI -> "SCI"
                }
                if (fmtStr.isNotEmpty()) StatusPillBadge(fmtStr, colors)
            }

            // Watermark on right
            Text(
                text = "NATURAL-V.P.A.M.",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = colors.lcdText.copy(alpha = 0.55f),
                letterSpacing = 0.5.sp
            )
        }

        // Expression line: Supports touch cursor editing, tap-to-position, and horizontal scrolling
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .horizontalScroll(scrollState)
                .pointerInput(state.expression) {
                    detectTapGestures { offset ->
                        val layout = textLayoutResult
                        if (layout != null && state.expression.isNotEmpty()) {
                            val tappedIndex = layout.getOffsetForPosition(offset)
                            onCursorMoved(tappedIndex)
                        } else {
                            onCursorMoved(0)
                        }
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = if (state.expression.isEmpty()) " " else state.expression,
                fontFamily = FontFamily.Monospace,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.lcdText,
                onTextLayout = { textLayoutResult = it },
                modifier = Modifier
                    .drawWithContent {
                        drawContent()
                        if (cursorAlpha > 0.05f) {
                            val layout = textLayoutResult
                            val safePos = state.cursorPos.coerceIn(0, state.expression.length)
                            val cursorX = if (state.expression.isEmpty() || layout == null) {
                                0f
                            } else {
                                layout.getCursorRect(safePos).left
                            }
                            drawRect(
                                color = colors.lcdText.copy(alpha = cursorAlpha),
                                topLeft = Offset(cursorX, 4.dp.toPx()),
                                size = Size(2.5.dp.toPx(), size.height - 8.dp.toPx())
                            )
                        }
                    }
            )
        }

        // Result line / Error line & History / Expand icon in bottom right
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            // Result text aligned to end
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 32.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 22.sp,
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

            // Expand / History button at bottom-right corner
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onOpenHistory)
                    .testTag("lcd_expand_history"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CropFree,
                    contentDescription = "Expand Calculation History",
                    tint = colors.lcdText.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
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
                fontSize = 24.sp,
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
                fontSize = 16.sp,
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
                fontSize = 16.sp,
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
    cursorAlpha: Float,
    onSelectMenuIndex: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
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
                color = colors.lcdText.copy(alpha = 0.9f)
            )
            Text(
                text = wiz.stage.uppercase(),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.lcdText.copy(alpha = 0.7f)
            )
        }

        when (wiz.stage) {
            "menu" -> {
                val listState = rememberLazyListState()

                // Smoothly scroll to keep selector in view whenever menuIndex changes
                LaunchedEffect(wiz.menuIndex) {
                    listState.animateScrollToItem(wiz.menuIndex)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 2.dp)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        itemsIndexed(wiz.menuOptions) { i, opt ->
                            val isSelected = i == wiz.menuIndex
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) colors.lcdText else Color.Transparent)
                                    .clickable { onSelectMenuIndex(i) }
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${i + 1}: $opt",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colors.lcdBackground else colors.lcdText
                                )
                                if (isSelected) {
                                    Text(
                                        text = "▶",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.lcdBackground
                                    )
                                }
                            }
                        }
                    }

                    // Up / Down scroll indicator arrows on right edge
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 2.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (listState.canScrollBackward) {
                            Text("▲", fontSize = 10.sp, color = colors.lcdText.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                        } else {
                            Spacer(modifier = Modifier.size(10.dp))
                        }
                        if (listState.canScrollForward) {
                            Text("▼", fontSize = 10.sp, color = colors.lcdText.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                        } else {
                            Spacer(modifier = Modifier.size(10.dp))
                        }
                    }
                }

                Text(
                    text = "Press 1–${wiz.menuOptions.size} or ▲▼ + [=] to select",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = colors.lcdText.copy(alpha = 0.75f)
                )
            }
            "prompt" -> {
                val currPrompt = wiz.prompts.getOrNull(wiz.promptIndex) ?: "Value"
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.Center
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
                            .padding(vertical = 4.dp),
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
                    color = colors.lcdText.copy(alpha = 0.75f)
                )
            }
            "result" -> {
                val scroll = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scroll)
                        .padding(vertical = 4.dp)
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
                    color = colors.lcdText.copy(alpha = 0.75f)
                )
            }
            "calc" -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.Center
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
                            text = wiz.baseResult.take(30),
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
private fun StatusPillBadge(
    text: String,
    colors: com.example.prosci.ui.theme.ProSciColorScheme,
    customColor: Color? = null
) {
    Box(
        modifier = Modifier
            .background(customColor ?: colors.lcdText, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (customColor != null) Color.White else colors.lcdBackground
        )
    }
}
