package com.mrcoder20.portx.presentation.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.mrcoder20.portx.presentation.ui.theme.*

@Composable
fun LiquidGlowBackground() {
    val accent = LocalAccentColor.current
    val isDark = LocalAppSettings.current.theme == "DARK"
    var pointerOffset by remember { mutableStateOf(Offset.Zero) }
    var isTouching by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition()
    
    val breathAnim by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearOutSlowInEasing), RepeatMode.Reverse)
    )

    val ringScale by animateFloatAsState(
        targetValue = if (isTouching) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
    )
    val ringAlpha by animateFloatAsState(
        targetValue = if (isTouching) 0.4f else 0f,
        animationSpec = tween(300)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        isTouching = event.type != PointerEventType.Exit && event.type != PointerEventType.Release
                        if (event.changes.isNotEmpty()) {
                            pointerOffset = event.changes.first().position
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size
            val centerOffset = Offset(canvasSize.width / 2, canvasSize.height / 2)

            val gridSpacing = 60.dp.toPx()
            val gridAlpha = if (isDark) 0.03f else 0.08f
            val gridColor = if (isDark) Color.White else Color.Black
            
            for (x in 0..(canvasSize.width / gridSpacing).toInt()) {
                drawLine(
                    color = gridColor.copy(alpha = gridAlpha),
                    start = Offset(x * gridSpacing, 0f),
                    end = Offset(x * gridSpacing, canvasSize.height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(canvasSize.height / gridSpacing).toInt()) {
                drawLine(
                    color = gridColor.copy(alpha = gridAlpha),
                    start = Offset(0f, y * gridSpacing),
                    end = Offset(canvasSize.width, y * gridSpacing),
                    strokeWidth = 1f
                )
            }

            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to accent.copy(alpha = if (isDark) 0.08f else 0.18f),
                    1.0f to Color.Transparent,
                    center = centerOffset,
                    radius = canvasSize.width * 0.7f * breathAnim
                )
            )

            if (isTouching || ringAlpha > 0f) {
                val lineLength = 20.dp.toPx()
                val color = accent.copy(alpha = ringAlpha)

                drawLine(color, Offset(pointerOffset.x, pointerOffset.y - lineLength), Offset(pointerOffset.x, pointerOffset.y + lineLength), 2f)
                drawLine(color, Offset(pointerOffset.x - lineLength, pointerOffset.y), Offset(pointerOffset.x + lineLength, pointerOffset.y), 2f)

                drawCircle(
                    color = accent.copy(alpha = ringAlpha * 0.5f),
                    center = pointerOffset,
                    radius = 100.dp.toPx() * ringScale,
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = LocalAppSettings.current.theme == "DARK"
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .border(
                1.dp, 
                if (isDark) GlassBorder else GlassBorderLight, 
                RoundedCornerShape(24.dp)
            ),
        color = if (isDark) GlassBackground else GlassLight
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Composable
fun PremiumSnackbar(message: String) {
    val accent = LocalAccentColor.current
    val isDark = LocalAppSettings.current.theme == "DARK"
    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(
                1.dp,
                Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.5f))),
                RoundedCornerShape(20.dp)
            ),
        color = if (isDark) Color(0xCC050505) else Color(0xCCF5F7FA)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = TertiaryNeon, modifier = Modifier.size(20.dp))
            Text(
                message, 
                color = if (isDark) Color.White else Color.Black,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                maxLines = 1
            )
        }
    }
}
