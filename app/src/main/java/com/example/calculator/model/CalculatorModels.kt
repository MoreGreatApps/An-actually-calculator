package com.example.calculator.model

enum class CalcMode(val title: String) {
    SCIENTIFIC("Scientific"),
    PROGRAMMER("Programmer"),
    CONVERTER("Unit Converter"),
    CONSTANTS("Constants")
}

data class HistoryItem(
    val id: Long = System.currentTimeMillis(),
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)
