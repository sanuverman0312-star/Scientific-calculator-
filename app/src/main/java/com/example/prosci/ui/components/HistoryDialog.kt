package com.example.prosci.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.prosci.data.HistoryItem
import com.example.prosci.ui.CalculatorViewModel
import com.example.prosci.ui.theme.LocalProSciColors

@Composable
fun HistoryDialog(
    history: List<HistoryItem>,
    viewModel: CalculatorViewModel,
    onDismiss: () -> Unit
) {
    val colors = LocalProSciColors.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(12.dp)
                .testTag("history_dialog"),
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
                        text = "CALCULATION HISTORY",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.functionKeyText
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Text("✕", color = colors.functionKeyText, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (history.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No history yet",
                            color = colors.functionKeyText.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(history.reversed()) { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.functionKeyBackground)
                                    .border(1.dp, colors.borderLight, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.loadHistoryItem(item) }
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.expression,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 14.sp,
                                        color = colors.functionKeyText,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "= ${item.formattedResult}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.goldShift
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.clearHistory() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clear_history_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accentAction)
                    ) {
                        Text("Clear History", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
