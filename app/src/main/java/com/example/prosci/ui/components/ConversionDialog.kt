package com.example.prosci.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.prosci.data.ConstantsData
import com.example.prosci.data.UnitConversion
import com.example.prosci.ui.CalculatorViewModel
import com.example.prosci.ui.theme.LocalProSciColors

@Composable
fun ConversionDialog(
    viewModel: CalculatorViewModel,
    onDismiss: () -> Unit
) {
    val colors = LocalProSciColors.current
    var selectedConversion by remember { mutableStateOf(ConstantsData.CONVERSIONS.first()) }
    var inputValue by remember { mutableStateOf("1") }
    var resultText by remember { mutableStateOf("") }

    fun calculate(conv: UnitConversion, inVal: String) {
        val x = inVal.toDoubleOrNull()
        if (x == null) {
            resultText = "Invalid number"
            return
        }
        val res = if (conv.isTemperature) {
            if (conv.isForward) (x - 32.0) / 1.8 else x * 1.8 + 32.0
        } else {
            if (conv.isForward) x * conv.factor else x / conv.factor
        }
        val cleanRes = (res * 1e8).toLong() / 1e8
        resultText = "$x ${conv.fromUnit} = $cleanRes ${conv.toUnit}"
    }

    LaunchedEffect(selectedConversion, inputValue) {
        calculate(selectedConversion, inputValue)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(12.dp)
                .testTag("conversion_dialog"),
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
                        text = "UNIT CONVERSION (40)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.functionKeyText
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Text("✕", color = colors.functionKeyText, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Input value field
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    label = { Text("Value to convert") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("conversion_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.functionKeyText,
                        unfocusedTextColor = colors.functionKeyText,
                        focusedBorderColor = colors.accentAction,
                        unfocusedBorderColor = colors.borderLight
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Result Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.lcdBackground)
                        .padding(10.dp)
                ) {
                    Text(
                        text = resultText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.lcdText
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val numPart = resultText.substringAfter("= ").substringBefore(" ")
                        viewModel.insertConversionResult(numPart)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("conversion_insert_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentAction)
                ) {
                    Text("Insert Result into Calculator", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Select conversion type:",
                    fontSize = 12.sp,
                    color = colors.functionKeyText.copy(alpha = 0.7f)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(ConstantsData.CONVERSIONS) { conv ->
                        val isSelected = conv == selectedConversion
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) colors.accentAction else colors.functionKeyBackground)
                                .border(1.dp, colors.borderLight, RoundedCornerShape(6.dp))
                                .clickable { selectedConversion = conv }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${conv.fromUnit}  ➔  ${conv.toUnit}",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else colors.functionKeyText
                            )
                        }
                    }
                }
            }
        }
    }
}
