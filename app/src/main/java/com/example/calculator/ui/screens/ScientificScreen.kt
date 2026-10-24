package com.example.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.CalculatorViewModel
import com.example.calculator.ui.components.ButtonType
import com.example.calculator.ui.components.CalcButton
import com.example.ui.theme.*

@Composable
fun ScientificScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val expression by viewModel.expression.collectAsState()
    val livePreview by viewModel.livePreview.collectAsState()
    val evaluatedResult by viewModel.evaluatedResult.collectAsState()
    val isDegMode by viewModel.isDegMode.collectAsState()
    val isSecondMode by viewModel.isSecondMode.collectAsState()
    val isHypMode by viewModel.isHypMode.collectAsState()
    val memoryValue by viewModel.memoryValue.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val exprScrollState = rememberScrollState()

    // Auto scroll to end of expression when it changes
    LaunchedEffect(expression) {
        exprScrollState.animateScrollTo(exprScrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CalcDarkBg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- Display Area ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .clip(RoundedCornerShape(16.dp))
                .background(CalcSurface)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Status Badges (DEG/RAD, 2nd, HYP, Memory)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // DEG/RAD Toggle Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isDegMode) CalcKeypadScientific else CalcKeypadOperator,
                        modifier = Modifier.clickable { viewModel.toggleDegRad() }
                    ) {
                        Text(
                            text = if (isDegMode) "DEG" else "RAD",
                            color = if (isDegMode) CalcTextScientific else CalcTextOperator,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    if (isSecondMode) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CalcKeypadAltAction
                        ) {
                            Text(
                                text = "2ND",
                                color = androidx.compose.ui.graphics.Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (isHypMode) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CalcKeypadAltAction
                        ) {
                            Text(
                                text = "HYP",
                                color = androidx.compose.ui.graphics.Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (memoryValue != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CalcKeypadOperator
                        ) {
                            Text(
                                text = "M",
                                color = CalcTextOperator,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Expression Character Count or Helper
                Text(
                    text = if (expression.isNotEmpty()) "${expression.length} chars" else "",
                    fontSize = 11.sp,
                    color = CalcTextSecondary.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Expression Display (Scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(exprScrollState),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = if (expression.isEmpty() && evaluatedResult == null) "0" else expression,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (expression.isEmpty() && evaluatedResult == null) CalcTextSecondary.copy(alpha = 0.4f) else CalcTextPrimary,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.End,
                    modifier = Modifier.testTag("expression_display")
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Evaluated Result, Live Preview, or Error Message
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                when {
                    errorMessage != null -> {
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CalcTextError,
                            textAlign = TextAlign.End,
                            modifier = Modifier.testTag("error_display")
                        )
                    }
                    evaluatedResult != null -> {
                        Text(
                            text = "= $evaluatedResult",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = CalcTextAccent,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.End,
                            modifier = Modifier.testTag("evaluated_result_display")
                        )
                    }
                    livePreview != null -> {
                        Text(
                            text = "= $livePreview",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = CalcTextSecondary,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.End,
                            modifier = Modifier.testTag("live_preview_display")
                        )
                    }
                    else -> {
                        // Empty spacer to maintain layout height
                        Text(
                            text = " ",
                            fontSize = 22.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- Memory Quick Controls & Mode Toggles ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CalcButton(
                text = "2nd",
                onClick = { viewModel.toggleSecondMode() },
                type = if (isSecondMode) ButtonType.TOGGLE_ACTIVE else ButtonType.TOGGLE_INACTIVE,
                fontSize = 13.sp,
                height = 36.dp,
                modifier = Modifier.weight(1f)
            )
            CalcButton(
                text = "hyp",
                onClick = { viewModel.toggleHypMode() },
                type = if (isHypMode) ButtonType.TOGGLE_ACTIVE else ButtonType.TOGGLE_INACTIVE,
                fontSize = 13.sp,
                height = 36.dp,
                modifier = Modifier.weight(1f)
            )
            CalcButton(
                text = "MC",
                onClick = { viewModel.memoryClear() },
                type = ButtonType.TOGGLE_INACTIVE,
                fontSize = 12.sp,
                height = 36.dp,
                enabled = memoryValue != null,
                modifier = Modifier.weight(1f)
            )
            CalcButton(
                text = "MR",
                onClick = { viewModel.memoryRecall() },
                type = ButtonType.TOGGLE_INACTIVE,
                fontSize = 12.sp,
                height = 36.dp,
                enabled = memoryValue != null,
                modifier = Modifier.weight(1f)
            )
            CalcButton(
                text = "M+",
                onClick = { viewModel.memoryAdd() },
                type = ButtonType.TOGGLE_INACTIVE,
                fontSize = 12.sp,
                height = 36.dp,
                modifier = Modifier.weight(1f)
            )
            CalcButton(
                text = "M−",
                onClick = { viewModel.memorySubtract() },
                type = ButtonType.TOGGLE_INACTIVE,
                fontSize = 12.sp,
                height = 36.dp,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- Scientific & Numeric Keypad (5 Columns) ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val btnHeight = 44.dp

            // Row 1: Trig functions
            val sinText = if (isHypMode) (if (isSecondMode) "asinh" else "sinh") else (if (isSecondMode) "sin⁻¹" else "sin")
            val sinFunc = if (isHypMode) (if (isSecondMode) "asinh" else "sinh") else (if (isSecondMode) "asin" else "sin")

            val cosText = if (isHypMode) (if (isSecondMode) "acosh" else "cosh") else (if (isSecondMode) "cos⁻¹" else "cos")
            val cosFunc = if (isHypMode) (if (isSecondMode) "acosh" else "cosh") else (if (isSecondMode) "acos" else "cos")

            val tanText = if (isHypMode) (if (isSecondMode) "atanh" else "tanh") else (if (isSecondMode) "tan⁻¹" else "tan")
            val tanFunc = if (isHypMode) (if (isSecondMode) "atanh" else "tanh") else (if (isSecondMode) "atan" else "tan")

            val lnText = if (isSecondMode) "eˣ" else "ln"
            val logText = if (isSecondMode) "10ˣ" else "log"

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcButton(
                    text = sinText,
                    onClick = { viewModel.appendFunction(sinFunc) },
                    type = ButtonType.SCIENTIFIC,
                    fontSize = 14.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = cosText,
                    onClick = { viewModel.appendFunction(cosFunc) },
                    type = ButtonType.SCIENTIFIC,
                    fontSize = 14.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = tanText,
                    onClick = { viewModel.appendFunction(tanFunc) },
                    type = ButtonType.SCIENTIFIC,
                    fontSize = 14.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = lnText,
                    onClick = {
                        if (isSecondMode) viewModel.appendFunction("exp") else viewModel.appendFunction("ln")
                    },
                    type = ButtonType.SCIENTIFIC,
                    fontSize = 14.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = logText,
                    onClick = {
                        if (isSecondMode) viewModel.appendInput("10^") else viewModel.appendFunction("log")
                    },
                    type = ButtonType.SCIENTIFIC,
                    fontSize = 14.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: Powers, Roots, Parentheses, Factorial / Combinatorics
            val sqrtText = if (isSecondMode) "x²" else "√"
            val powerText = if (isSecondMode) "ʸ√x" else "xʸ"
            val factText = if (isSecondMode) "nPr" else "x!"

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcButton(
                    text = sqrtText,
                    onClick = {
                        if (isSecondMode) viewModel.appendInput("^2") else viewModel.appendFunction("sqrt")
                    },
                    type = ButtonType.SCIENTIFIC,
                    fontSize = 14.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = powerText,
                    onClick = {
                        if (isSecondMode) viewModel.appendFunction("root") else viewModel.appendInput("^")
                    },
                    type = ButtonType.SCIENTIFIC,
                    fontSize = 14.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "(",
                    onClick = { viewModel.appendInput("(") },
                    type = ButtonType.SCIENTIFIC,
                    fontSize = 15.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = ")",
                    onClick = { viewModel.appendInput(")") },
                    type = ButtonType.SCIENTIFIC,
                    fontSize = 15.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = factText,
                    onClick = {
                        if (isSecondMode) viewModel.appendFunction("npr") else viewModel.appendInput("!")
                    },
                    type = ButtonType.SCIENTIFIC,
                    fontSize = 14.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 3: Reciprocal/Cube, Constants, Combinatorics, Clear
            val recipText = if (isSecondMode) "x³" else "1/x"
            val piText = if (isSecondMode) "φ" else "π"
            val eText = if (isSecondMode) "|x|" else "e"
            val combText = if (isSecondMode) "nCr" else "%"

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcButton(
                    text = recipText,
                    onClick = {
                        if (isSecondMode) viewModel.appendInput("^3") else viewModel.appendInput("1/")
                    },
                    type = ButtonType.SCIENTIFIC,
                    fontSize = 14.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = piText,
                    onClick = {
                        if (isSecondMode) viewModel.appendInput("φ") else viewModel.appendInput("π")
                    },
                    type = ButtonType.SCIENTIFIC,
                    fontSize = 15.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = eText,
                    onClick = {
                        if (isSecondMode) viewModel.appendFunction("abs") else viewModel.appendInput("e")
                    },
                    type = ButtonType.SCIENTIFIC,
                    fontSize = 14.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = combText,
                    onClick = {
                        if (isSecondMode) viewModel.appendFunction("ncr") else viewModel.appendInput("%")
                    },
                    type = ButtonType.SCIENTIFIC,
                    fontSize = 14.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "AC",
                    onClick = { viewModel.clearAll() },
                    type = ButtonType.CLEAR,
                    fontSize = 15.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 4: 7, 8, 9, ÷, ⌫
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcButton(
                    text = "7",
                    onClick = { viewModel.appendInput("7") },
                    type = ButtonType.NUMBER,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "8",
                    onClick = { viewModel.appendInput("8") },
                    type = ButtonType.NUMBER,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "9",
                    onClick = { viewModel.appendInput("9") },
                    type = ButtonType.NUMBER,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "÷",
                    onClick = { viewModel.appendInput("÷") },
                    type = ButtonType.OPERATOR,
                    fontSize = 20.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "⌫",
                    onClick = { viewModel.backspace() },
                    type = ButtonType.OPERATOR,
                    fontSize = 17.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 5: 4, 5, 6, ×, mod
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcButton(
                    text = "4",
                    onClick = { viewModel.appendInput("4") },
                    type = ButtonType.NUMBER,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "5",
                    onClick = { viewModel.appendInput("5") },
                    type = ButtonType.NUMBER,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "6",
                    onClick = { viewModel.appendInput("6") },
                    type = ButtonType.NUMBER,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "×",
                    onClick = { viewModel.appendInput("×") },
                    type = ButtonType.OPERATOR,
                    fontSize = 20.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "mod",
                    onClick = { viewModel.appendInput("%") },
                    type = ButtonType.OPERATOR,
                    fontSize = 13.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 6: 1, 2, 3, −, ±
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcButton(
                    text = "1",
                    onClick = { viewModel.appendInput("1") },
                    type = ButtonType.NUMBER,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "2",
                    onClick = { viewModel.appendInput("2") },
                    type = ButtonType.NUMBER,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "3",
                    onClick = { viewModel.appendInput("3") },
                    type = ButtonType.NUMBER,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "−",
                    onClick = { viewModel.appendInput("−") },
                    type = ButtonType.OPERATOR,
                    fontSize = 20.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "±",
                    onClick = {
                        // Negate expression or prepend minus
                        if (expression.startsWith("−")) {
                            viewModel.backspace() // or handle toggle
                        } else {
                            viewModel.appendInput("−")
                        }
                    },
                    type = ButtonType.OPERATOR,
                    fontSize = 16.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 7: 0, ., rand, +, =
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcButton(
                    text = "0",
                    onClick = { viewModel.appendInput("0") },
                    type = ButtonType.NUMBER,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = ".",
                    onClick = { viewModel.appendInput(".") },
                    type = ButtonType.NUMBER,
                    fontSize = 20.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "rand",
                    onClick = { viewModel.appendFunction("rand") },
                    type = ButtonType.SCIENTIFIC,
                    fontSize = 12.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "+",
                    onClick = { viewModel.appendInput("+") },
                    type = ButtonType.OPERATOR,
                    fontSize = 20.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
                CalcButton(
                    text = "=",
                    onClick = { viewModel.calculate() },
                    type = ButtonType.ACTION,
                    fontSize = 22.sp,
                    height = btnHeight,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
