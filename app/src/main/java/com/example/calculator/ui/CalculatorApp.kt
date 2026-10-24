package com.example.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculator.model.CalcMode
import com.example.calculator.ui.components.HistoryBottomSheet
import com.example.calculator.ui.screens.ConstantsScreen
import com.example.calculator.ui.screens.ConverterScreen
import com.example.calculator.ui.screens.ProgrammerScreen
import com.example.calculator.ui.screens.ScientificScreen
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorApp(
    viewModel: CalculatorViewModel = viewModel()
) {
    val currentMode by viewModel.currentMode.collectAsState()
    val history by viewModel.history.collectAsState()
    var showHistorySheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CalcDarkBg,
        contentColor = CalcTextPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            color = CalcKeypadAction,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "f(x)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Text(
                            text = "Scientific Calculator",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CalcTextPrimary
                        )
                    }
                },
                actions = {
                    // History Icon with Badge
                    BadgedBox(
                        badge = {
                            if (history.isNotEmpty()) {
                                Badge(
                                    containerColor = CalcKeypadAction,
                                    contentColor = Color.White
                                ) {
                                    Text(history.size.toString(), fontSize = 10.sp)
                                }
                            }
                        },
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        IconButton(
                            onClick = { showHistorySheet = true },
                            modifier = Modifier.testTag("open_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Calculation History",
                                tint = CalcTextPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CalcDarkBg,
                    titleContentColor = CalcTextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CalcSurface,
                tonalElevation = 4.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val navItems = listOf(
                    Triple(CalcMode.SCIENTIFIC, "Scientific", Icons.Default.Calculate),
                    Triple(CalcMode.PROGRAMMER, "Programmer", Icons.Default.Terminal),
                    Triple(CalcMode.CONVERTER, "Converter", Icons.Default.SyncAlt),
                    Triple(CalcMode.CONSTANTS, "Constants", Icons.Default.Functions)
                )

                navItems.forEach { (mode, label, icon) ->
                    val isSelected = currentMode == mode
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setMode(mode) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = CalcTextAccent,
                            indicatorColor = CalcKeypadAction,
                            unselectedIconColor = CalcTextSecondary,
                            unselectedTextColor = CalcTextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_${mode.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentMode) {
                CalcMode.SCIENTIFIC -> ScientificScreen(viewModel = viewModel)
                CalcMode.PROGRAMMER -> ProgrammerScreen(viewModel = viewModel)
                CalcMode.CONVERTER -> ConverterScreen(viewModel = viewModel)
                CalcMode.CONSTANTS -> ConstantsScreen(viewModel = viewModel)
            }
        }

        if (showHistorySheet) {
            HistoryBottomSheet(
                historyList = history,
                onSelectExpression = { item ->
                    viewModel.recallHistory(item)
                    viewModel.setMode(CalcMode.SCIENTIFIC)
                    showHistorySheet = false
                },
                onInsertResult = { res ->
                    viewModel.insertHistoryResult(res)
                    viewModel.setMode(CalcMode.SCIENTIFIC)
                    showHistorySheet = false
                },
                onClearHistory = {
                    viewModel.clearHistory()
                },
                onDismiss = { showHistorySheet = false }
            )
        }
    }
}
