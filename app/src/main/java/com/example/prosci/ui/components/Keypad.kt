package com.example.prosci.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prosci.engine.AngleUnit
import com.example.prosci.engine.NumberFormatType
import com.example.prosci.ui.CalcMode
import com.example.prosci.ui.CalculatorUiState
import com.example.prosci.ui.theme.LocalProSciColors

data class KeyDef(
    val label: String,
    val shiftLabel: String = "",
    val alphaLabel: String = "",
    val action: String = "",
    val isNumber: Boolean = false,
    val isAccent: Boolean = false,
    val isGold: Boolean = false,
    val isRed: Boolean = false,
    val testTag: String
)

@Composable
fun Keypad(
    state: CalculatorUiState,
    onKeyPress: (main: String, shift: String, alpha: String, action: String) -> Unit,
    onModeClick: () -> Unit,
    onAngleClick: () -> Unit,
    onFormatClick: () -> Unit,
    onMathIoClick: () -> Unit,
    onSdClick: () -> Unit,
    onCompClick: () -> Unit,
    onStatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Clean, authentic, well-adjusted button heights with comfortable gaps
    val sciRowHeight = 38.dp
    val numRowHeight = 48.dp
    val dPadSize = 78.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Horizontal Mode Quick-Bar (7 pills)
        ModeQuickBar(
            state = state,
            onModeClick = onModeClick,
            onAngleClick = onAngleClick,
            onFormatClick = onFormatClick,
            onMathIoClick = onMathIoClick,
            onSdClick = onSdClick,
            onCompClick = onCompClick,
            onStatClick = onStatClick
        )

        // 2. Control Row: SHIFT, ALPHA, Centered Circular D-Pad, DEL, AC
        ControlAndDPadRow(
            isShiftActive = state.isShift,
            isAlphaActive = state.isAlpha,
            dPadSize = dPadSize,
            onKeyPress = onKeyPress
        )

        // 3. Scientific Deck (6 columns × 3 rows)
        ScientificDeck(
            rowHeight = sciRowHeight,
            onKeyPress = onKeyPress
        )

        // 4. Numeric & Operator Deck (5 columns × 4 rows with tall Ans and =)
        NumericAndOperatorDeck(
            cellHeight = numRowHeight,
            onKeyPress = onKeyPress
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}

/**
 * 1. Mode Quick-Bar: 7 rounded pills matching the reference design.
 */
@Composable
private fun ModeQuickBar(
    state: CalculatorUiState,
    onModeClick: () -> Unit,
    onAngleClick: () -> Unit,
    onFormatClick: () -> Unit,
    onMathIoClick: () -> Unit,
    onSdClick: () -> Unit,
    onCompClick: () -> Unit,
    onStatClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // MODE
        ModePill(
            label = "MODE",
            isAccent = true,
            testTag = "toolbar_mode",
            onClick = onModeClick,
            modifier = Modifier.weight(1.15f)
        )

        // Angle unit (DEG / RAD / GRA)
        val angleStr = when (state.angleUnit) {
            AngleUnit.DEG -> "DEG"
            AngleUnit.RAD -> "RAD"
            AngleUnit.GRA -> "GRA"
        }
        ModePill(
            label = angleStr,
            testTag = "toolbar_angle",
            onClick = onAngleClick,
            modifier = Modifier.weight(1f)
        )

        // Format (NORM / FIX / SCI)
        val fmtStr = when (state.formatSetting.type) {
            NumberFormatType.NORM -> "NORM"
            NumberFormatType.FIX -> "FIX"
            NumberFormatType.SCI -> "SCI"
        }
        ModePill(
            label = fmtStr,
            testTag = "toolbar_format",
            onClick = onFormatClick,
            modifier = Modifier.weight(1f)
        )

        // MathIO / LineIO
        ModePill(
            label = if (state.isMathIo) "MathIO" else "LineIO",
            testTag = "toolbar_math_io",
            onClick = onMathIoClick,
            modifier = Modifier.weight(1.15f)
        )

        // S <=> D
        ModePill(
            label = "S⇔D",
            testTag = "toolbar_sd",
            onClick = onSdClick,
            modifier = Modifier.weight(1f)
        )

        // COMP
        ModePill(
            label = "COMP",
            isSelected = state.currentMode == CalcMode.COMP,
            testTag = "toolbar_comp",
            onClick = onCompClick,
            modifier = Modifier.weight(1f)
        )

        // STAT
        ModePill(
            label = "STAT",
            isSelected = state.currentMode == CalcMode.STAT,
            testTag = "toolbar_stat",
            onClick = onStatClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ModePill(
    label: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    isAccent: Boolean = false,
    isSelected: Boolean = false
) {
    val colors = LocalProSciColors.current

    val bg = when {
        isAccent -> colors.accentAction
        isSelected -> colors.accentAction.copy(alpha = 0.35f)
        else -> colors.functionKeyBackground
    }

    val borderColor = when {
        isAccent -> colors.accentAction
        isSelected -> colors.accentAction
        else -> colors.borderLight
    }

    val fg = when {
        isAccent -> Color.White
        else -> colors.functionKeyText
    }

    Box(
        modifier = modifier
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
            fontFamily = FontFamily.SansSerif
        )
    }
}

/**
 * 2. Control Row: [SHIFT] [ALPHA] ( D-PAD ) [DEL] [AC]
 */
@Composable
private fun ControlAndDPadRow(
    isShiftActive: Boolean,
    isAlphaActive: Boolean,
    dPadSize: Dp = 78.dp,
    onKeyPress: (main: String, shift: String, alpha: String, action: String) -> Unit
) {
    val colors = LocalProSciColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dPadSize),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: SHIFT & ALPHA
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // SHIFT (Warm Amber Gold)
            MajorActionButton(
                label = "SHIFT",
                bgColor = if (isShiftActive) colors.goldShift.copy(alpha = 0.8f) else colors.goldShift,
                textColor = Color(0xFF1B1D22),
                testTag = "key_shift",
                onClick = { onKeyPress("", "", "", "@SH") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(0.68f)
            )

            // ALPHA (Vibrant Crimson Coral)
            MajorActionButton(
                label = "ALPHA",
                bgColor = if (isAlphaActive) colors.redAlpha.copy(alpha = 0.8f) else colors.redAlpha,
                textColor = Color.White,
                testTag = "key_alpha",
                onClick = { onKeyPress("", "", "", "@AL") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(0.68f)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Center: Circular 4-Direction D-Pad
        CircularDPad(
            onUp = { onKeyPress("", "", "", "@U") },
            onDown = { onKeyPress("", "", "", "@D") },
            onLeft = { onKeyPress("", "", "", "@L") },
            onRight = { onKeyPress("", "", "", "@R") },
            modifier = Modifier.size(dPadSize)
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Right side: DEL & AC
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // DEL
            MajorActionButton(
                label = "DEL",
                bgColor = colors.accentAction,
                textColor = Color.White,
                testTag = "key_del",
                onClick = { onKeyPress("", "", "", "@DEL") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(0.68f)
            )

            // AC
            MajorActionButton(
                label = "AC",
                bgColor = colors.accentAction,
                textColor = Color.White,
                testTag = "key_ac",
                onClick = { onKeyPress("", "", "", "@AC") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(0.68f)
            )
        }
    }
}

@Composable
private fun MajorActionButton(
    label: String,
    bgColor: Color,
    textColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.94f else 1.0f

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(if (isPressed) 1.dp else 4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White.copy(alpha = 0.3f)),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontFamily = FontFamily.SansSerif
        )
    }
}

/**
 * Centered Circular 4-Direction D-Pad Controller
 */
@Composable
private fun CircularDPad(
    onUp: () -> Unit,
    onDown: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalProSciColors.current

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(colors.cardBackground)
            .border(1.5.dp, colors.borderLight, CircleShape)
            .testTag("circular_dpad"),
        contentAlignment = Alignment.Center
    ) {
        // Up Arrow
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.5f)
                .fillMaxHeight(0.35f)
                .clickable(onClick = onUp)
                .testTag("key_up"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▲",
                fontSize = 12.sp,
                color = colors.functionKeyText,
                fontWeight = FontWeight.Bold
            )
        }

        // Down Arrow
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.5f)
                .fillMaxHeight(0.35f)
                .clickable(onClick = onDown)
                .testTag("key_down"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▼",
                fontSize = 12.sp,
                color = colors.functionKeyText,
                fontWeight = FontWeight.Bold
            )
        }

        // Left Arrow
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(0.35f)
                .fillMaxHeight(0.5f)
                .clickable(onClick = onLeft)
                .testTag("key_left"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "◀",
                fontSize = 12.sp,
                color = colors.functionKeyText,
                fontWeight = FontWeight.Bold
            )
        }

        // Right Arrow
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(0.35f)
                .fillMaxHeight(0.5f)
                .clickable(onClick = onRight)
                .testTag("key_right"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▶",
                fontSize = 12.sp,
                color = colors.functionKeyText,
                fontWeight = FontWeight.Bold
            )
        }

        // Center circular disc
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(colors.functionKeyBackground)
                .border(1.dp, colors.borderLight, CircleShape)
        )
    }
}

/**
 * 3. Scientific Deck (6 columns × 3 rows)
 */
@Composable
private fun ScientificDeck(
    rowHeight: Dp = 38.dp,
    onKeyPress: (main: String, shift: String, alpha: String, action: String) -> Unit
) {
    val r1 = listOf(
        KeyDef("x²", shiftLabel = "x³", testTag = "key_x2"),
        KeyDef("xʸ", shiftLabel = "ˣ√", testTag = "key_power"),
        KeyDef("log", shiftLabel = "10ˣ", testTag = "key_log"),
        KeyDef("ln", shiftLabel = "eˣ", testTag = "key_ln"),
        KeyDef("(", alphaLabel = "Y", testTag = "key_lparen"),
        KeyDef(")", alphaLabel = "X", testTag = "key_rparen")
    )

    val r2 = listOf(
        KeyDef("sin", shiftLabel = "sin⁻¹", alphaLabel = "D", testTag = "key_sin"),
        KeyDef("cos", shiftLabel = "cos⁻¹", alphaLabel = "E", testTag = "key_cos"),
        KeyDef("tan", shiftLabel = "tan⁻¹", alphaLabel = "F", testTag = "key_tan"),
        KeyDef("√", shiftLabel = "∛", testTag = "key_sqrt"),
        KeyDef("x⁻¹", shiftLabel = "x!", alphaLabel = "B", testTag = "key_inverse"),
        KeyDef("nCr", shiftLabel = "nPr", testTag = "key_ncr")
    )

    val r3 = listOf(
        KeyDef("hyp", action = "@HYP", alphaLabel = "C", testTag = "key_hyp"),
        KeyDef("STO", shiftLabel = "RCL", action = "@STO", testTag = "key_sto"),
        KeyDef("π", shiftLabel = "e", testTag = "key_pi"),
        KeyDef("(−)", alphaLabel = "A", testTag = "key_neg"),
        KeyDef("M+", shiftLabel = "M−", alphaLabel = "M", action = "@M+", testTag = "key_mplus"),
        KeyDef("∫", shiftLabel = "d/dx", alphaLabel = "Σ", testTag = "key_integral")
    )

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        DeckRow6(r1, rowHeight, onKeyPress)
        DeckRow6(r2, rowHeight, onKeyPress)
        DeckRow6(r3, rowHeight, onKeyPress)
    }
}

@Composable
private fun DeckRow6(
    keys: List<KeyDef>,
    rowHeight: Dp,
    onKeyPress: (main: String, shift: String, alpha: String, action: String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        keys.forEach { key ->
            KeyButton(
                keyDef = key,
                modifier = Modifier
                    .weight(1f)
                    .height(rowHeight),
                fontSize = 13.sp,
                onClick = {
                    val mainVal = when (key.label) {
                        "x²" -> "²"
                        "xʸ" -> "^"
                        "log" -> "log("
                        "ln" -> "ln("
                        "sin" -> "sin("
                        "cos" -> "cos("
                        "tan" -> "tan("
                        "√" -> "√("
                        "x⁻¹" -> "⁻¹"
                        "nCr" -> "nCr"
                        "π" -> "π"
                        "(−)" -> "−"
                        "∫" -> "∫("
                        else -> key.label
                    }
                    val shiftVal = when (key.shiftLabel) {
                        "x³" -> "³"
                        "ˣ√" -> "ˣ√("
                        "10ˣ" -> "10^("
                        "eˣ" -> "e^("
                        "sin⁻¹" -> "sin⁻¹("
                        "cos⁻¹" -> "cos⁻¹("
                        "tan⁻¹" -> "tan⁻¹("
                        "∛" -> "∛("
                        "x!" -> "!"
                        "nPr" -> "nPr"
                        "RCL" -> "@RCL"
                        "e" -> "e"
                        "M−" -> "@M-"
                        "d/dx" -> "d/dx("
                        else -> key.shiftLabel
                    }
                    val alphaVal = when (key.alphaLabel) {
                        "Σ" -> "Σ("
                        else -> key.alphaLabel
                    }
                    onKeyPress(mainVal, shiftVal, alphaVal, key.action)
                }
            )
        }
    }
}

/**
 * 4. Numeric & Operator Deck: 5 columns × 4 rows
 * Columns 1-3: Numeric keys
 * Columns 4-5: Operator keys, with Ans and = spanning Rows 3 & 4!
 */
@Composable
private fun NumericAndOperatorDeck(
    cellHeight: Dp = 48.dp,
    onKeyPress: (main: String, shift: String, alpha: String, action: String) -> Unit
) {
    val spacing = 5.dp
    val tallHeight = cellHeight * 2 + spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
        // Row 1: 7, 8, 9, ×, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            KeyButton(KeyDef("7", isNumber = true, testTag = "key_7"), Modifier.weight(1f).height(cellHeight), 20.sp) { onKeyPress("7", "", "", "") }
            KeyButton(KeyDef("8", isNumber = true, testTag = "key_8"), Modifier.weight(1f).height(cellHeight), 20.sp) { onKeyPress("8", "", "", "") }
            KeyButton(KeyDef("9", isNumber = true, testTag = "key_9"), Modifier.weight(1f).height(cellHeight), 20.sp) { onKeyPress("9", "", "", "") }
            KeyButton(KeyDef("×", testTag = "key_multiply"), Modifier.weight(1f).height(cellHeight), 18.sp) { onKeyPress("×", "", "", "") }
            KeyButton(KeyDef("÷", testTag = "key_divide"), Modifier.weight(1f).height(cellHeight), 18.sp) { onKeyPress("÷", "", "", "") }
        }

        // Row 2: 4, 5, 6, +, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            KeyButton(KeyDef("4", isNumber = true, testTag = "key_4"), Modifier.weight(1f).height(cellHeight), 20.sp) { onKeyPress("4", "", "", "") }
            KeyButton(KeyDef("5", isNumber = true, testTag = "key_5"), Modifier.weight(1f).height(cellHeight), 20.sp) { onKeyPress("5", "", "", "") }
            KeyButton(KeyDef("6", isNumber = true, testTag = "key_6"), Modifier.weight(1f).height(cellHeight), 20.sp) { onKeyPress("6", "", "", "") }
            KeyButton(KeyDef("+", testTag = "key_plus"), Modifier.weight(1f).height(cellHeight), 18.sp) { onKeyPress("+", "", "", "") }
            KeyButton(KeyDef("−", testTag = "key_minus"), Modifier.weight(1f).height(cellHeight), 18.sp) { onKeyPress("-", "", "", "") }
        }

        // Rows 3 & 4 combined:
        // Left 3 columns:
        //   Row 3: 1, 2, 3
        //   Row 4: 0, ., ×10ˣ
        // Col 4: Ans (spans 2 rows)
        // Col 5: = (spans 2 rows)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            // Columns 1-3
            Column(
                modifier = Modifier.weight(3f),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                // Row 3: 1, 2, 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    KeyButton(KeyDef("1", isNumber = true, testTag = "key_1"), Modifier.weight(1f).height(cellHeight), 20.sp) { onKeyPress("1", "", "", "") }
                    KeyButton(KeyDef("2", isNumber = true, testTag = "key_2"), Modifier.weight(1f).height(cellHeight), 20.sp) { onKeyPress("2", "", "", "") }
                    KeyButton(KeyDef("3", isNumber = true, testTag = "key_3"), Modifier.weight(1f).height(cellHeight), 20.sp) { onKeyPress("3", "", "", "") }
                }

                // Row 4: 0, ., ×10ˣ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    KeyButton(KeyDef("0", isNumber = true, testTag = "key_0"), Modifier.weight(1f).height(cellHeight), 20.sp) { onKeyPress("0", "", "", "") }
                    KeyButton(KeyDef(".", shiftLabel = "Ran#", isNumber = true, testTag = "key_dot"), Modifier.weight(1f).height(cellHeight), 20.sp) { onKeyPress(".", "Ran#", "", "") }
                    KeyButton(KeyDef("×10ˣ", testTag = "key_exp"), Modifier.weight(1f).height(cellHeight), 13.sp) { onKeyPress("×10^(", "", "", "") }
                }
            }

            // Column 4: Ans (Tall button spanning 2 rows)
            KeyButton(
                keyDef = KeyDef("Ans", testTag = "key_ans"),
                modifier = Modifier
                    .weight(1f)
                    .height(tallHeight),
                fontSize = 15.sp,
                onClick = { onKeyPress("Ans", "", "", "") }
            )

            // Column 5: = (Tall prominent button spanning 2 rows)
            KeyButton(
                keyDef = KeyDef("=", action = "@EQ", isAccent = true, testTag = "key_equals"),
                modifier = Modifier
                    .weight(1f)
                    .height(tallHeight),
                fontSize = 24.sp,
                onClick = { onKeyPress("=", "", "", "@EQ") }
            )
        }
    }
}

/**
 * Universal Tactile Key Button using theme colors
 */
@Composable
private fun KeyButton(
    keyDef: KeyDef,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 13.sp,
    onClick: () -> Unit
) {
    val colors = LocalProSciColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.94f else 1.0f

    val bg = when {
        keyDef.isAccent -> colors.accentAction
        keyDef.isGold -> colors.goldShift
        keyDef.isRed -> colors.redAlpha
        keyDef.isNumber -> colors.numberKeyBackground
        else -> colors.functionKeyBackground
    }

    val fg = when {
        keyDef.isAccent || keyDef.isRed -> Color.White
        keyDef.isGold -> Color(0xFF1B1D22)
        keyDef.isNumber -> colors.numberKeyText
        else -> colors.functionKeyText
    }

    val borderColor = when {
        keyDef.isAccent -> colors.accentAction
        else -> colors.borderLight
    }

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(if (isPressed) 1.dp else 2.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = colors.functionKeyText.copy(alpha = 0.2f)),
                onClick = onClick
            )
            .testTag(keyDef.testTag)
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        // Shift label in top-left
        if (keyDef.shiftLabel.isNotEmpty()) {
            Text(
                text = keyDef.shiftLabel,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = colors.goldShift,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 2.dp, top = 1.dp)
            )
        }

        // Alpha label in top-right
        if (keyDef.alphaLabel.isNotEmpty()) {
            Text(
                text = keyDef.alphaLabel,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = colors.redAlpha,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 2.dp, top = 1.dp)
            )
        }

        // Primary key label
        Text(
            text = keyDef.label,
            fontSize = fontSize,
            fontWeight = if (keyDef.isNumber || keyDef.label == "=") FontWeight.Bold else FontWeight.SemiBold,
            color = fg,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
