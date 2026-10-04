package com.example.prosci.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.prosci.ui.theme.LocalProSciColors

@Composable
fun HelpDialog(
    onDismiss: () -> Unit
) {
    val colors = LocalProSciColors.current
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(12.dp)
                .testTag("help_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FUNCTION REFERENCE",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.functionKeyText
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Text("✕", color = colors.functionKeyText, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ProSci fx-991ES Natural-V.P.A.M. Reference",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.goldShift
                    )

                    ReferenceSection("MODES", listOf(
                        "1: COMP" to "Standard math calculation",
                        "2: CMPLX" to "Complex numbers with imaginary unit i",
                        "3: STAT" to "1-var statistics & 2-var linear regression",
                        "4: BASE-N" to "DEC, HEX, BIN, OCT arithmetic & base conversions",
                        "5: EQN" to "2 & 3 unknown simultaneous equations, Quadratic, Cubic",
                        "6: MATRIX" to "Matrix A+B, A−B, A×B, Det A, A⁻¹, Aᵀ up to 3×3",
                        "7: TABLE" to "Generate f(X) function tables with Step",
                        "8: VECTOR" to "Vector A+B, A−B, A·B, A×B, |A|, Angle in 2D & 3D",
                        "9: CONST" to "40 CODATA physical constants",
                        "0: CONV" to "40 scientific & engineering unit conversions"
                    ), colors)

                    ReferenceSection("KEYPAD SHORTCUTS & MODIFIERS", listOf(
                        "SHIFT (Gold)" to "Enables yellow functions printed above top-left of keys",
                        "ALPHA (Red)" to "Enables red variables A–F, X, Y, M and Σ",
                        "S ⇔ D" to "Toggles exact fraction/radical format and decimal",
                        "STO / RCL" to "Store current result in A–F, X, Y, M / Recall variable",
                        "M+ / M−" to "Add/subtract evaluated value to/from M memory register",
                        "hyp" to "Hyperbolic functions (sinh, cosh, tanh, etc.)",
                        "Ans" to "Recalls previous calculation answer",
                        "Ran#" to "Generates a pseudorandom number between 0 and 1"
                    ), colors)

                    ReferenceSection("CALCULUS & ALGEBRA", listOf(
                        "∫" to "Numerical integration via Simpson's rule: ∫(f(X), lower, upper)",
                        "d/dx" to "Numerical derivative via central difference: d/dx(f(X), x)",
                        "Σ" to "Discrete summation: Σ(f(X), start, end)",
                        "nPr / nCr" to "Permutations and Combinations",
                        "x!" to "Factorial (Gamma extension)"
                    ), colors)

                    Text(
                        text = "Natural display presents fractions, roots (√), and π textbook-style. In wizard modes, press [=] to advance parameters or restart, and [AC] to reset.",
                        fontSize = 11.sp,
                        color = colors.functionKeyText.copy(alpha = 0.75f),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ReferenceSection(
    title: String,
    items: List<Pair<String, String>>,
    colors: com.example.prosci.ui.theme.ProSciColorScheme
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colors.accentAction,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items.forEach { (key, desc) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = key,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.goldShift,
                        modifier = Modifier.weight(0.35f)
                    )
                    Text(
                        text = desc,
                        fontSize = 11.sp,
                        color = colors.functionKeyText,
                        modifier = Modifier.weight(0.65f)
                    )
                }
            }
        }
    }
}
