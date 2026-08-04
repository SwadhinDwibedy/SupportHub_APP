package com.example.supporthub.features.shared.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val PremiumCanvasBackground = Color.White
private val PremiumGreenAura = Color(0xFFBDF2E4)
private val PremiumBlueAura = Color(0xFFDDEBFF)
private val PremiumPurpleAura = Color(0xFFE9D7FF)
private val PremiumPinkAura = Color(0xFFF7C9E6)
private val PremiumGlassWhite = Color.White

@Composable
fun SupportHubPremiumBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val transition = rememberInfiniteTransition(label = "supporthub_premium_background")

    val driftX by transition.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "premium_background_drift_x"
    )

    val driftY by transition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "premium_background_drift_y"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PremiumCanvasBackground)
    ) {
        Box(
            modifier = Modifier
                .offset(x = (-90).dp, y = 120.dp)
                .size(360.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PremiumGreenAura.copy(alpha = 0.42f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .offset(x = 50.dp, y = 200.dp)
                .size(410.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PremiumBlueAura.copy(alpha = 0.58f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .offset(x = (-10).dp, y = 430.dp)
                .size(420.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PremiumPurpleAura.copy(alpha = 0.50f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .offset(x = 70.dp, y = 540.dp)
                .size(340.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PremiumPinkAura.copy(alpha = 0.34f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .offset(x = (120 + driftX).dp, y = (160 + driftY).dp)
                .size(170.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PremiumGreenAura.copy(alpha = 0.14f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .offset(x = 38.dp, y = 44.dp)
                .size(width = 98.dp, height = 126.dp)
                .background(
                    color = PremiumGlassWhite.copy(alpha = 0.42f),
                    shape = RoundedCornerShape(32.dp)
                )
        )

        Box(
            modifier = Modifier
                .offset(x = 286.dp, y = 82.dp)
                .size(width = 82.dp, height = 104.dp)
                .background(
                    color = PremiumGlassWhite.copy(alpha = 0.32f),
                    shape = RoundedCornerShape(28.dp)
                )
        )

        content()
    }
}
