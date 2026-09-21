package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.AICoreState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryGold
import com.example.ui.theme.SoftGold
import com.example.ui.theme.SuccessGreen

@Composable
fun TarhiNooAICore(
    state: AICoreState = AICoreState.IDLE,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_core_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AICoreState.IDLE -> 2400
                    AICoreState.THINKING -> 1100
                    AICoreState.GENERATING -> 650
                    AICoreState.SUCCESS -> 1500
                    AICoreState.ERROR -> 800
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AICoreState.GENERATING -> 2200
                    AICoreState.THINKING -> 4000
                    else -> 9000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "core_rotation"
    )

    val primaryColor = when (state) {
        AICoreState.IDLE -> PrimaryGold
        AICoreState.THINKING -> AccentCyan
        AICoreState.GENERATING -> SoftGold
        AICoreState.SUCCESS -> SuccessGreen
        AICoreState.ERROR -> ErrorRed
    }

    val glowColor = when (state) {
        AICoreState.IDLE -> BrandGreen
        AICoreState.THINKING -> AccentCyan.copy(alpha = 0.6f)
        AICoreState.GENERATING -> PrimaryGold.copy(alpha = 0.8f)
        AICoreState.SUCCESS -> SuccessGreen.copy(alpha = 0.5f)
        AICoreState.ERROR -> ErrorRed.copy(alpha = 0.6f)
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val centerOffset = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (this.size.minDimension / 2f) * 0.72f

            // Outer soft glow halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.45f), Color.Transparent),
                    center = centerOffset,
                    radius = baseRadius * 1.5f * pulseScale
                ),
                radius = baseRadius * 1.5f * pulseScale,
                center = centerOffset
            )

            // Dynamic orbital energy ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.5f),
                radius = baseRadius * 1.15f,
                center = centerOffset,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Inner Core Orb with Luxury Linear Gradient
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(primaryColor, glowColor),
                    start = Offset(centerOffset.x - baseRadius, centerOffset.y - baseRadius),
                    end = Offset(centerOffset.x + baseRadius, centerOffset.y + baseRadius)
                ),
                radius = baseRadius * pulseScale,
                center = centerOffset
            )

            // Core center highlight spark
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = baseRadius * 0.28f,
                center = Offset(centerOffset.x - baseRadius * 0.22f, centerOffset.y - baseRadius * 0.22f)
            )
        }
    }
}
