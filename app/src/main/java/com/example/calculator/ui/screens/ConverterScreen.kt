package com.example.calculator.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.engine.UnitCategory
import com.example.calculator.engine.UnitConverterEngine
import com.example.calculator.engine.UnitDefinition
import com.example.calculator.ui.CalculatorViewModel
import com.example.calculator.ui.components.ButtonType
import com.example.calculator.ui.components.CalcButton
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val category by viewModel.converterCategory.collectAsState()
    val fromUnit by viewModel.fromUnit.collectAsState()
    val toUnit by viewModel.toUnit.collectAsState()
    val input by viewModel.converterInput.collectAsState()
    val result by viewModel.converterResult.collectAsState()

    var showFromMenu by remember { mutableStateOf(false) }
    var showToMenu by remember { mutableStateOf(false) }

    val units = remember(category) { UnitConverterEngine.getUnitsForCategory(category) }
    val categoryScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CalcDarkBg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Category Selector Chips (Horizontally scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(categoryScrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                UnitCategory.values().forEach { cat ->
                    val isSelected = cat == category
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) CalcKeypadAction else CalcSurfaceElevated,
                        modifier = Modifier.clickable { viewModel.setConverterCategory(cat) }
                    ) {
                        Text(
                            text = cat.displayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else CalcTextSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // From Unit Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CalcSurface)
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FROM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CalcTextSecondary
                        )

                        // From Dropdown Trigger
                        Box {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CalcSurfaceElevated,
                                modifier = Modifier.clickable { showFromMenu = true }
                            ) {
                                Text(
                                    text = "${fromUnit.name} (${fromUnit.symbol}) ▼",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CalcTextScientific,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showFromMenu,
                                onDismissRequest = { showFromMenu = false },
                                modifier = Modifier.background(CalcSurfaceElevated)
                            ) {
                                units.forEach { u ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "${u.name} (${u.symbol})",
                                                color = if (u == fromUnit) CalcTextAccent else CalcTextPrimary
                                            )
                                        },
                                        onClick = {
                                            viewModel.setFromUnit(u)
                                            showFromMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (input.isEmpty()) "0" else input,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = CalcTextPrimary,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.testTag("converter_input_display")
                    )
                }
            }

            // Swap Button Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = { viewModel.swapUnits() },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(19.dp))
                        .background(CalcSurfaceElevated)
                        .testTag("swap_units_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap Units",
                        tint = CalcTextAccent
                    )
                }
            }

            // To Unit Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CalcSurface)
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CalcTextSecondary
                        )

                        // To Dropdown Trigger
                        Box {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CalcSurfaceElevated,
                                modifier = Modifier.clickable { showToMenu = true }
                            ) {
                                Text(
                                    text = "${toUnit.name} (${toUnit.symbol}) ▼",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CalcTextScientific,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showToMenu,
                                onDismissRequest = { showToMenu = false },
                                modifier = Modifier.background(CalcSurfaceElevated)
                            ) {
                                units.forEach { u ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "${u.name} (${u.symbol})",
                                                color = if (u == toUnit) CalcTextAccent else CalcTextPrimary
                                            )
                                        },
                                        onClick = {
                                            viewModel.setToUnit(u)
                                            showToMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (result.isEmpty()) "0" else result,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = CalcTextAccent,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f).testTag("converter_result_display")
                        )

                        if (result.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Converted Value", result))
                                    Toast.makeText(context, "Copied $result", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Result",
                                    tint = CalcTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Numeric Keypad for Unit Converter
        val btnHeight = 46.dp
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                CalcButton("7", { appendConverter("7", viewModel, input) }, type = ButtonType.NUMBER, height = btnHeight, modifier = Modifier.weight(1f))
                CalcButton("8", { appendConverter("8", viewModel, input) }, type = ButtonType.NUMBER, height = btnHeight, modifier = Modifier.weight(1f))
                CalcButton("9", { appendConverter("9", viewModel, input) }, type = ButtonType.NUMBER, height = btnHeight, modifier = Modifier.weight(1f))
                CalcButton("AC", { viewModel.setConverterInput("") }, type = ButtonType.CLEAR, height = btnHeight, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                CalcButton("4", { appendConverter("4", viewModel, input) }, type = ButtonType.NUMBER, height = btnHeight, modifier = Modifier.weight(1f))
                CalcButton("5", { appendConverter("5", viewModel, input) }, type = ButtonType.NUMBER, height = btnHeight, modifier = Modifier.weight(1f))
                CalcButton("6", { appendConverter("6", viewModel, input) }, type = ButtonType.NUMBER, height = btnHeight, modifier = Modifier.weight(1f))
                CalcButton("⌫", {
                    if (input.isNotEmpty()) viewModel.setConverterInput(input.dropLast(1))
                }, type = ButtonType.OPERATOR, height = btnHeight, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                CalcButton("1", { appendConverter("1", viewModel, input) }, type = ButtonType.NUMBER, height = btnHeight, modifier = Modifier.weight(1f))
                CalcButton("2", { appendConverter("2", viewModel, input) }, type = ButtonType.NUMBER, height = btnHeight, modifier = Modifier.weight(1f))
                CalcButton("3", { appendConverter("3", viewModel, input) }, type = ButtonType.NUMBER, height = btnHeight, modifier = Modifier.weight(1f))
                CalcButton("±", {
                    if (input.startsWith("-")) {
                        viewModel.setConverterInput(input.removePrefix("-"))
                    } else if (input.isNotEmpty()) {
                        viewModel.setConverterInput("-$input")
                    }
                }, type = ButtonType.OPERATOR, height = btnHeight, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                CalcButton("0", { appendConverter("0", viewModel, input) }, type = ButtonType.NUMBER, height = btnHeight, modifier = Modifier.weight(2f))
                CalcButton(".", {
                    if (!input.contains(".")) {
                        viewModel.setConverterInput(if (input.isEmpty()) "0." else "$input.")
                    }
                }, type = ButtonType.NUMBER, height = btnHeight, modifier = Modifier.weight(1f))
                CalcButton("1", { viewModel.setConverterInput("1") }, type = ButtonType.SCIENTIFIC, subText = "reset", height = btnHeight, modifier = Modifier.weight(1f))
            }
        }
    }
}

private fun appendConverter(digit: String, vm: CalculatorViewModel, current: String) {
    if (current == "0") {
        vm.setConverterInput(digit)
    } else {
        vm.setConverterInput(current + digit)
    }
}
