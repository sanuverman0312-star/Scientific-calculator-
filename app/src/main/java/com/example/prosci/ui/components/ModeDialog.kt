package com.example.prosci.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.prosci.ui.CalcMode
import com.example.prosci.ui.CalculatorViewModel
import com.example.prosci.ui.theme.LocalProSciColors

@Composable
fun ModeDialog(
    viewModel: CalculatorViewModel,
    onDismiss: () -> Unit
) {
    val colors = LocalProSciColors.current

    val modes = listOf(
        Pair("1", CalcMode.COMP to "Standard calculation"),
        Pair("2", CalcMode.CMPLX to "Complex numbers"),
        Pair("3", CalcMode.STAT to "Statistics & regression"),
        Pair("4", CalcMode.BASE_N to "Binary, hex, octal, decimal"),
        Pair("5", CalcMode.EQN to "Simultaneous & polynomials"),
        Pair("6", CalcMode.MATRIX to "Matrix arithmetic up to 3×3"),
        Pair("7", CalcMode.TABLE to "Function f(X) tables"),
        Pair("8", CalcMode.VECTOR to "Vector 2D / 3D operations")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("mode_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT MODE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.functionKeyText
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Text("✕", color = colors.functionKeyText, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    modes.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { (num, modeInfo) ->
                                val (m, desc) = modeInfo
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.functionKeyBackground)
                                        .border(1.dp, colors.borderLight, RoundedCornerShape(10.dp))
                                        .clickable {
                                            viewModel.setMode(m)
                                        }
                                        .padding(8.dp)
                                        .testTag("mode_item_${m.name}"),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(colors.accentAction),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = num,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = m.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.functionKeyText
                                            )
                                            Text(
                                                text = desc,
                                                fontSize = 9.sp,
                                                color = colors.functionKeyText.copy(alpha = 0.7f),
                                                lineHeight = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Extra utility pickers: CONSTANTS & CONVERSIONS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.functionKeyBackground)
                                .border(1.dp, colors.borderLight, RoundedCornerShape(10.dp))
                                .clickable {
                                    onDismiss()
                                    viewModel.openConstantsDialog()
                                }
                                .padding(8.dp)
                                .testTag("mode_item_const"),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(colors.goldShift),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("9", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("CONST", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.functionKeyText)
                                    Text("40 CODATA constants", fontSize = 9.sp, color = colors.functionKeyText.copy(alpha = 0.7f))
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.functionKeyBackground)
                                .border(1.dp, colors.borderLight, RoundedCornerShape(10.dp))
                                .clickable {
                                    onDismiss()
                                    viewModel.openConversionDialog()
                                }
                                .padding(8.dp)
                                .testTag("mode_item_conv"),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(colors.redAlpha),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("0", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("CONV", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.functionKeyText)
                                    Text("40 Unit conversions", fontSize = 9.sp, color = colors.functionKeyText.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
