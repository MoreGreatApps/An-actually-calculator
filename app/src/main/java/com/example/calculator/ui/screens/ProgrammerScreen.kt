package com.example.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.calculator.engine.NumberBase
import com.example.calculator.engine.ProgrammerEngine
import com.example.calculator.engine.WordSize
import com.example.calculator.ui.CalculatorViewModel
import com.example.calculator.ui.components.ButtonType
import com.example.calculator.ui.components.CalcButton
import com.example.ui.theme.*

@Composable
fun ProgrammerScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val activeRadix by viewModel.activeRadix.collectAsState()
    val wordSize by viewModel.wordSize.collectAsState()
    val programmerValue by viewModel.programmerValue.collectAsState()
    val pendingOp by viewModel.programmerPendingOp.collectAsState()
    val storedValue by viewModel.programmerStoredValue.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CalcDarkBg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- Multi-Base Display Panel ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CalcSurface)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Word size selector header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WORD SIZE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CalcTextSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    WordSize.values().forEach { ws ->
                        val isSelected = ws == wordSize
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) CalcKeypadAltAction else CalcSurfaceElevated,
                            modifier = Modifier.clickable { viewModel.setWordSize(ws) }
                        ) {
                            Text(
                                text = ws.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else CalcTextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = CalcBorder, thickness = 0.5.dp)

            // 4 Base Rows: HEX, DEC, OCT, BIN
            NumberBase.values().forEach { base ->
                val isSelected = base == activeRadix
                val formatted = ProgrammerEngine.formatInBase(programmerValue, base, wordSize)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CalcSurfaceElevated else Color.Transparent)
                        .clickable { viewModel.setRadix(base) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = base.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) CalcTextScientific else CalcTextSecondary,
                        modifier = Modifier.width(36.dp)
                    )
                    Text(
                        text = formatted,
                        fontSize = if (base == NumberBase.BIN) 13.sp else 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) CalcTextPrimary else CalcTextSecondary.copy(alpha = 0.8f),
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            }

            // Pending operation indicator if any
            if (pendingOp != null && storedValue != null) {
                Text(
                    text = "${ProgrammerEngine.formatInBase(storedValue!!, activeRadix, wordSize)} $pendingOp",
                    fontSize = 12.sp,
                    color = CalcTextAccent,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- Bitwise Operations Row ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val ops = listOf("AND", "OR", "XOR", "NOT", "LSH", "RSH")
            ops.forEach { op ->
                val isOpActive = pendingOp == op
                CalcButton(
                    text = op,
                    onClick = { viewModel.programmerBitwiseOp(op) },
                    type = if (isOpActive) ButtonType.TOGGLE_ACTIVE else ButtonType.SCIENTIFIC,
                    fontSize = 12.sp,
                    height = 36.dp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- Keypad: Hex Letters + Numbers (5 Columns) ---
        val radix = activeRadix.radix
        fun isDigitEnabled(digitChar: Char): Boolean {
            val v = digitChar.digitToIntOrNull(16) ?: return false
            return v < radix
        }

        val btnHeight = 44.dp

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Row 1: A, B, C, D, E
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf('A', 'B', 'C', 'D', 'E').forEach { ch ->
                    CalcButton(
                        text = ch.toString(),
                        onClick = { viewModel.programmerInputDigit(ch) },
                        type = ButtonType.SCIENTIFIC,
                        enabled = isDigitEnabled(ch),
                        height = btnHeight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Row 2: F, AC, ⌫, mod, ÷
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcButton(
                    text = "F",
                    onClick = { viewModel.programmerInputDigit('F') },
                    type = ButtonType.SCIENTIFIC,
                    enabled = isDigitEnabled('F'),
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "AC",
                    onClick = { viewModel.programmerClear() },
                    type = ButtonType.CLEAR,
                    fontSize = 15.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "⌫",
                    onClick = { viewModel.programmerBackspace() },
                    type = ButtonType.OPERATOR,
                    fontSize = 16.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "MOD",
                    onClick = { viewModel.programmerBitwiseOp("MOD") },
                    type = ButtonType.SCIENTIFIC,
                    fontSize = 12.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "÷",
                    onClick = { viewModel.programmerBitwiseOp("/") },
                    type = ButtonType.OPERATOR,
                    fontSize = 20.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 3: 7, 8, 9, NOR, ×
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcButton(
                    text = "7",
                    onClick = { viewModel.programmerInputDigit('7') },
                    type = ButtonType.NUMBER,
                    enabled = isDigitEnabled('7'),
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "8",
                    onClick = { viewModel.programmerInputDigit('8') },
                    type = ButtonType.NUMBER,
                    enabled = isDigitEnabled('8'),
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "9",
                    onClick = { viewModel.programmerInputDigit('9') },
                    type = ButtonType.NUMBER,
                    enabled = isDigitEnabled('9'),
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "NOR",
                    onClick = { viewModel.programmerBitwiseOp("NOR") },
                    type = ButtonType.SCIENTIFIC,
                    fontSize = 12.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "×",
                    onClick = { viewModel.programmerBitwiseOp("*") },
                    type = ButtonType.OPERATOR,
                    fontSize = 20.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 4: 4, 5, 6, NAND, −
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcButton(
                    text = "4",
                    onClick = { viewModel.programmerInputDigit('4') },
                    type = ButtonType.NUMBER,
                    enabled = isDigitEnabled('4'),
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "5",
                    onClick = { viewModel.programmerInputDigit('5') },
                    type = ButtonType.NUMBER,
                    enabled = isDigitEnabled('5'),
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "6",
                    onClick = { viewModel.programmerInputDigit('6') },
                    type = ButtonType.NUMBER,
                    enabled = isDigitEnabled('6'),
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "NAND",
                    onClick = { viewModel.programmerBitwiseOp("NAND") },
                    type = ButtonType.SCIENTIFIC,
                    fontSize = 11.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "−",
                    onClick = { viewModel.programmerBitwiseOp("-") },
                    type = ButtonType.OPERATOR,
                    fontSize = 20.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 5: 1, 2, 3, +, =
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcButton(
                    text = "1",
                    onClick = { viewModel.programmerInputDigit('1') },
                    type = ButtonType.NUMBER,
                    enabled = isDigitEnabled('1'),
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "2",
                    onClick = { viewModel.programmerInputDigit('2') },
                    type = ButtonType.NUMBER,
                    enabled = isDigitEnabled('2'),
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "3",
                    onClick = { viewModel.programmerInputDigit('3') },
                    type = ButtonType.NUMBER,
                    enabled = isDigitEnabled('3'),
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "+",
                    onClick = { viewModel.programmerBitwiseOp("+") },
                    type = ButtonType.OPERATOR,
                    fontSize = 20.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "=",
                    onClick = { viewModel.programmerCalculate() },
                    type = ButtonType.ACTION,
                    fontSize = 22.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 6: 0 (wide or with 00)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcButton(
                    text = "0",
                    onClick = { viewModel.programmerInputDigit('0') },
                    type = ButtonType.NUMBER,
                    enabled = isDigitEnabled('0'),
                    height = btnHeight,
                    modifier = Modifier.weight(2f)
                )
                CalcButton(
                    text = "00",
                    onClick = {
                        viewModel.programmerInputDigit('0')
                        viewModel.programmerInputDigit('0')
                    },
                    type = ButtonType.NUMBER,
                    enabled = isDigitEnabled('0'),
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "±",
                    onClick = {
                        // 2's complement negation
                        val current = programmerValue and wordSize.mask
                        viewModel.programmerBitwiseOp("NOT")
                        viewModel.programmerCalculate()
                    },
                    type = ButtonType.OPERATOR,
                    height = btnHeight,
                    modifier = Modifier.weight(2f)
                )
            }
        }
    }
}
