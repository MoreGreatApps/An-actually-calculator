package com.example.calculator.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class ButtonType {
    NUMBER,
    SCIENTIFIC,
    OPERATOR,
    ACTION,
    TOGGLE_ACTIVE,
    TOGGLE_INACTIVE,
    CLEAR
}

@Composable
fun CalcButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: ButtonType = ButtonType.NUMBER,
    subText: String? = null,
    fontSize: TextUnit = 18.sp,
    height: Dp = 52.dp,
    enabled: Boolean = true,
    testTag: String = text.lowercase().replace(" ", "_").replace("(", "").replace(")", "")
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.93f else 1f, label = "button_scale")

    val (bgColor, textColor) = when (type) {
        ButtonType.NUMBER -> Pair(CalcKeypadNumber, CalcTextPrimary)
        ButtonType.SCIENTIFIC -> Pair(CalcKeypadScientific, CalcTextScientific)
        ButtonType.OPERATOR -> Pair(CalcKeypadOperator, CalcTextOperator)
        ButtonType.ACTION -> Pair(CalcKeypadAction, Color.White)
        ButtonType.TOGGLE_ACTIVE -> Pair(CalcKeypadAltAction, Color.White)
        ButtonType.TOGGLE_INACTIVE -> Pair(CalcKeypadScientific, CalcTextSecondary)
        ButtonType.CLEAR -> Pair(Color(0xFF3B1D22), Color(0xFFFFA39E))
    }

    val finalBgColor = if (enabled) bgColor else bgColor.copy(alpha = 0.35f)
    val finalTextColor = if (enabled) textColor else textColor.copy(alpha = 0.35f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(height)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(finalBgColor)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
            .padding(horizontal = 2.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (subText != null) {
                Text(
                    text = subText,
                    fontSize = 9.sp,
                    color = finalTextColor.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = if (type == ButtonType.ACTION || type == ButtonType.OPERATOR) FontWeight.Bold else FontWeight.SemiBold,
                color = finalTextColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
