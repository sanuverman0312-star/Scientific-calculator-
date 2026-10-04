package com.example.prosci.data

data class HistoryItem(
    val expression: String,
    val value: Double,
    val formattedResult: String,
    val timestamp: Long = System.currentTimeMillis()
)
