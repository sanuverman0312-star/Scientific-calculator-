package com.example.prosci.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onKeyPress: (main: String, shift: String, alpha: String, action: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Top 6-column Deck
        TopDeck(onKeyPress = onKeyPress)

        Spacer(modifier = Modifier.height(2.dp))

        // Bottom 5-column Deck
        BottomDeck(onKeyPress = onKeyPress)
    }
}

@Composable
private fun TopDeck(
    onKeyPress: (main: String, shift: String, alpha: String, action: String) -> Unit
) {
    val r1 = listOf(
        KeyDef("SHIFT", action = "@SH", isGold = true, testTag = "key_shift"),
        KeyDef("ALPHA", action = "@AL", isRed = true, testTag = "key_alpha"),
        KeyDef("◀", action = "@L", testTag = "key_left"),
        KeyDef("▶", action = "@R", testTag = "key_right"),
        KeyDef("▲", action = "@U", testTag = "key_up"),
        KeyDef("▼", action = "@D", testTag = "key_down")
    )

    val r2 = listOf(
        KeyDef("x²", shiftLabel = "x³", testTag = "key_x2"),
        KeyDef("xʸ", shiftLabel = "ˣ√", action = "^", testTag = "key_power"),
        KeyDef("log", shiftLabel = "10ˣ", testTag = "key_log"),
        KeyDef("ln", shiftLabel = "eˣ", testTag = "key_ln"),
        KeyDef("(", alphaLabel = "Y", testTag = "key_lparen"),
        KeyDef(")", alphaLabel = "X", testTag = "key_rparen")
    )

    val r3 = listOf(
        KeyDef("sin", shiftLabel = "sin⁻¹", alphaLabel = "D", testTag = "key_sin"),
        KeyDef("cos", shiftLabel = "cos⁻¹", alphaLabel = "E", testTag = "key_cos"),
        KeyDef("tan", shiftLabel = "tan⁻¹", alphaLabel = "F", testTag = "key_tan"),
        KeyDef("√", shiftLabel = "∛", testTag = "key_sqrt"),
        KeyDef("x⁻¹", shiftLabel = "x!", alphaLabel = "B", testTag = "key_inverse"),
        KeyDef("nCr", shiftLabel = "nPr", testTag = "key_ncr")
    )

    val r4 = listOf(
        KeyDef("hyp", action = "@HYP", alphaLabel = "C", testTag = "key_hyp"),
        KeyDef("STO", shiftLabel = "RCL", action = "@STO", testTag = "key_sto"),
        KeyDef("π", shiftLabel = "e", testTag = "key_pi"),
        KeyDef("(−)", alphaLabel = "A", testTag = "key_neg"),
        KeyDef("M+", shiftLabel = "M−", alphaLabel = "M", action = "@M+", testTag = "key_mplus"),
        KeyDef("∫", shiftLabel = "d/dx", alphaLabel = "Σ", testTag = "key_integral")
    )

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        DeckRow6(r1, onKeyPress)
        DeckRow6(r2, onKeyPress)
        DeckRow6(r3, onKeyPress)
        DeckRow6(r4, onKeyPress)
    }
}

@Composable
private fun DeckRow6(
    keys: List<KeyDef>,
    onKeyPress: (main: String, shift: String, alpha: String, action: String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        keys.forEach { key ->
            KeyButton(
                keyDef = key,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                onClick = {
                    val mainVal = when (key.label) {
                        "x²" -> "^2"
                        "xʸ" -> "^"
                        "log" -> "log("
                        "ln" -> "ln("
                        "sin" -> "sin("
                        "cos" -> "cos("
                        "tan" -> "tan("
                        "√" -> "sqrt("
                        "x⁻¹" -> "^(-1)"
                        "nCr" -> "nCr"
                        "π" -> "π"
                        "(−)" -> "(-)"
                        "∫" -> "∫("
                        else -> key.label
                    }
                    val shiftVal = when (key.shiftLabel) {
                        "x³" -> "^3"
                        "ˣ√" -> "root("
                        "10ˣ" -> "10^("
                        "eˣ" -> "e^("
                        "sin⁻¹" -> "asin("
                        "cos⁻¹" -> "acos("
                        "tan⁻¹" -> "atan("
                        "∛" -> "cbrt("
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

@Composable
private fun BottomDeck(
    onKeyPress: (main: String, shift: String, alpha: String, action: String) -> Unit
) {
    val r1 = listOf(
        KeyDef("7", isNumber = true, testTag = "key_7"),
        KeyDef("8", isNumber = true, testTag = "key_8"),
        KeyDef("9", isNumber = true, testTag = "key_9"),
        KeyDef("DEL", action = "@DEL", isAccent = true, testTag = "key_del"),
        KeyDef("AC", action = "@AC", isAccent = true, testTag = "key_ac")
    )

    val r2 = listOf(
        KeyDef("4", isNumber = true, testTag = "key_4"),
        KeyDef("5", isNumber = true, testTag = "key_5"),
        KeyDef("6", isNumber = true, testTag = "key_6"),
        KeyDef("×", testTag = "key_multiply"),
        KeyDef("÷", testTag = "key_divide")
    )

    val r3 = listOf(
        KeyDef("1", isNumber = true, testTag = "key_1"),
        KeyDef("2", isNumber = true, testTag = "key_2"),
        KeyDef("3", isNumber = true, testTag = "key_3"),
        KeyDef("+", testTag = "key_plus"),
        KeyDef("−", testTag = "key_minus")
    )

    val r4 = listOf(
        KeyDef("0", isNumber = true, testTag = "key_0"),
        KeyDef(".", shiftLabel = "Ran#", isNumber = true, testTag = "key_dot"),
        KeyDef("×10ˣ", testTag = "key_exp"),
        KeyDef("Ans", testTag = "key_ans"),
        KeyDef("=", action = "@EQ", isAccent = true, testTag = "key_equals")
    )

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        DeckRow5(r1, onKeyPress)
        DeckRow5(r2, onKeyPress)
        DeckRow5(r3, onKeyPress)
        DeckRow5(r4, onKeyPress)
    }
}

@Composable
private fun DeckRow5(
    keys: List<KeyDef>,
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
                    .height(46.dp),
                fontSize = if (key.isNumber || key.label == "=") 19.sp else 14.sp,
                onClick = {
                    val mainVal = when (key.label) {
                        "×10ˣ" -> "×10^("
                        "−" -> "-"
                        else -> key.label
                    }
                    val shiftVal = when (key.shiftLabel) {
                        "Ran#" -> "Ran#"
                        else -> key.shiftLabel
                    }
                    onKeyPress(mainVal, shiftVal, key.alphaLabel, key.action)
                }
            )
        }
    }
}

@Composable
private fun KeyButton(
    keyDef: KeyDef,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    onClick: () -> Unit
) {
    val colors = LocalProSciColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bg = when {
        keyDef.isAccent -> colors.accentAction
        keyDef.isGold -> colors.goldShift
        keyDef.isRed -> colors.redAlpha
        keyDef.isNumber -> colors.numberKeyBackground
        else -> colors.functionKeyBackground
    }

    val fg = when {
        keyDef.isAccent -> Color.White
        keyDef.isGold -> Color(0xFF1B1D22)
        keyDef.isRed -> Color(0xFF1B1D22)
        keyDef.isNumber -> colors.numberKeyText
        else -> colors.functionKeyText
    }

    val scale = if (isPressed) 0.95f else 1.0f

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(if (isPressed) 1.dp else 3.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, colors.borderLight, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White.copy(alpha = 0.2f)),
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
            fontWeight = FontWeight.SemiBold,
            color = fg,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
