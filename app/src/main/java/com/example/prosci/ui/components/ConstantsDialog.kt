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
import com.example.prosci.data.CodataConstant
import com.example.prosci.data.ConstantsData
import com.example.prosci.ui.CalculatorViewModel
import com.example.prosci.ui.theme.LocalProSciColors

@Composable
fun ConstantsDialog(
    viewModel: CalculatorViewModel,
    onDismiss: () -> Unit
) {
    val colors = LocalProSciColors.current
    var searchQuery by remember { mutableStateOf("") }

    val filteredConstants = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            ConstantsData.CONSTANTS
        } else {
            ConstantsData.CONSTANTS.filter {
                it.symbol.contains(searchQuery, ignoreCase = true) ||
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(12.dp)
                .testTag("constants_dialog"),
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
                        text = "CODATA CONSTANTS (40)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.functionKeyText
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Text("✕", color = colors.functionKeyText, fontSize = 16.sp)
                    }
                }

                Text(
                    text = "Tap any constant to insert value into expression.",
                    fontSize = 11.sp,
                    color = colors.functionKeyText.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by symbol or name...", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("constants_search"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.functionKeyText,
                        unfocusedTextColor = colors.functionKeyText,
                        focusedBorderColor = colors.accentAction,
                        unfocusedBorderColor = colors.borderLight
                    )
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredConstants) { c ->
                        ConstantItem(
                            constant = c,
                            colors = colors,
                            onClick = { viewModel.insertConstant(c) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConstantItem(
    constant: CodataConstant,
    colors: com.example.prosci.ui.theme.ProSciColorScheme,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.functionKeyBackground)
            .border(1.dp, colors.borderLight, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("constant_${constant.symbol}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = constant.symbol,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.goldShift,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = constant.name,
                        fontSize = 12.sp,
                        color = colors.functionKeyText
                    )
                }
                if (constant.unit.isNotEmpty()) {
                    Text(
                        text = "Unit: ${constant.unit}",
                        fontSize = 10.sp,
                        color = colors.functionKeyText.copy(alpha = 0.6f)
                    )
                }
            }

            Text(
                text = String.format(java.util.Locale.US, "%.5e", constant.value),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = colors.accentAction
            )
        }
    }
}
