package com.example.supporthub.features.splash

import android.os.Handler
import android.os.Looper
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supporthub.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    onLaunchComplete: () -> Unit = {}
) {
    val transition = rememberInfiniteTransition(label = "splash")

    val bgPulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgPulse"
    )

    val globalFloat by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "globalFloat"
    )

    var launched by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            launched = true
            onLaunchComplete()
        }, 1800)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F8FC))
    ) {
        BackgroundGlowLayer(pulse = bgPulse)
        AnimatedNetworkLayer()

        FloatingInfoChip(
            text = "AI Ready",
            iconType = ChipIconType.Spark,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 14.dp, y = (-58).dp)
        )

        FloatingInfoChip(
            text = "100+ Teams",
            iconType = ChipIconType.MultiDot,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-8).dp, y = 210.dp)
        )

        FloatingInfoChip(
            text = "Secure",
            iconType = ChipIconType.Shield,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = (-250).dp)
        )
        CenterHero(globalFloat = globalFloat, launching = launched)

        CreativeLaunchPulse(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 58.dp)
        )
    }
}

@Composable
private fun BackgroundGlowLayer(pulse: Float) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(340.dp)
                .offset(x = 82.dp, y = 64.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFD7ECFF).copy(alpha = 0.96f * pulse),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
                .blur(86.dp)
        )

        Box(
            modifier = Modifier
                .size(310.dp)
                .offset(x = 10.dp, y = 348.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFEFD9FF).copy(alpha = 0.75f * pulse),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
                .blur(92.dp)
        )

        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = 110.dp, y = 490.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFD9F5EE).copy(alpha = 0.75f * pulse),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
                .blur(90.dp)
        )
    }
}

@Composable
private fun CenterHero(globalFloat: Float, launching: Boolean) {
    val drift = sin(globalFloat * PI).toFloat() * 2.5f
    val rotate = cos(globalFloat * PI).toFloat() * 0.6f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .graphicsLayer {
                alpha = if (launching) 1f else 0.98f
                translationY = drift
                rotationZ = rotate
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(84.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE9EEF4),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Support Hub Logo",
                    modifier = Modifier.size(58.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "SupportHub",
            fontSize = 27.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1A2233)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Enterprise Helpdesk, Reimagined",
            fontSize = 13.sp,
            color = Color(0xFF748096),
            fontWeight = FontWeight.Medium
        )
    }
}

private enum class ChipIconType {
    Spark,
    MultiDot,
    Shield
}

@Composable
private fun FloatingInfoChip(
    text: String,
    iconType: ChipIconType,
    modifier: Modifier = Modifier
) {
    val chipTransition = rememberInfiniteTransition(label = "chip_motion")
    val chipFloat by chipTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chipFloat"
    )

    Surface(
        modifier = modifier.graphicsLayer {
            translationY = sin(chipFloat * PI).toFloat() * 2.2f
            rotationZ = cos(chipFloat * PI).toFloat() * 0.35f
        },
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.88f),
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(18.dp)
                )
                .background(Color.White.copy(alpha = 0.88f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (iconType) {
                    ChipIconType.Spark -> {
                        Text(
                            text = "✳",
                            fontSize = 12.sp,
                            color = Color(0xFF14837A)
                        )
                    }

                    ChipIconType.MultiDot -> {
                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .background(Color(0xFF1D9E92), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Box(
                            modifier = Modifier
                                .offset(x = (-4).dp)
                                .size(13.dp)
                                .background(Color(0xFF2B7DE9), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Box(
                            modifier = Modifier
                                .offset(x = (-8).dp)
                                .size(13.dp)
                                .background(Color(0xFF8A4DFF), CircleShape)
                        )
                    }

                    ChipIconType.Shield -> {
                        Text(
                            text = "🛡",
                            fontSize = 12.sp,
                            color = Color(0xFF14837A)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = text,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF243043)
                )
            }
        }
    }
}

@Composable
private fun CreativeLaunchPulse(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "launch_pulse")
    val widthPulse by transition.animateFloat(
        initialValue = 18f,
        targetValue = 34f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "widthPulse"
    )

    Box(
        modifier = modifier
            .width(56.dp)
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFE8ECF3))
    ) {
        Box(
            modifier = Modifier
                .width(widthPulse.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0F8B7B),
                            Color(0xFF36B39C),
                            Color(0xFF0F8B7B)
                        )
                    )
                )
        )
    }
}

@Composable
private fun AnimatedNetworkLayer() {
    val transition = rememberInfiniteTransition(label = "network")
    val anim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "network_anim"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 0.dp, vertical = 0.dp)
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        fun wobble(base: Float, amount: Float, phase: Float): Float {
            return base + sin((anim * 2f * PI).toFloat() + phase) * amount
        }

        fun p(x: Float, y: Float, dx: Float, dy: Float, phase: Float): Offset {
            return Offset(
                wobble(x, dx, phase),
                wobble(y, dy, phase + 1.6f)
            )
        }

        // Extra large network
        val p1 = p(cx - 500f, cy - 340f, 6f, 5f, 0.1f)
        val p2 = p(cx - 255f, cy - 430f, 5f, 6f, 0.8f)
        val p3 = p(cx + 255f, cy - 385f, 6f, 5f, 1.3f)

        val p4 = p(cx - 590f, cy - 25f, 5f, 6f, 2.0f)
        val p5 = p(cx - 18f, cy - 10f, 3f, 3f, 2.3f)
        val p6 = p(cx + 460f, cy + 10f, 6f, 5f, 2.8f)

        val p7 = p(cx - 345f, cy + 470f, 5f, 6f, 3.2f)
        val p8 = p(cx + 80f, cy + 510f, 6f, 5f, 3.7f)
        val p9 = p(cx + 575f, cy + 245f, 5f, 5f, 4.2f)

        val p10 = p(cx - 490f, cy + 185f, 5f, 5f, 4.8f)
        val p11 = p(cx + 360f, cy + 410f, 5f, 5f, 5.1f)

        val points = listOf(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11)

        val lineColor = Color(0xFFB7C5D6).copy(alpha = 0.22f)
        val nodeGreen = Color(0xFF0F7A74).copy(alpha = 0.76f)
        val nodeGray = Color(0xFF9AA7B8).copy(alpha = 0.56f)

        fun line(a: Offset, b: Offset) {
            drawLine(
                color = lineColor,
                start = a,
                end = b,
                strokeWidth = 1.6f,
                cap = StrokeCap.Round
            )
        }

        // Main mesh
        line(p1, p2)
        line(p2, p3)
        line(p1, p4)
        line(p2, p5)
        line(p3, p6)
        line(p4, p5)
        line(p5, p6)
        line(p4, p7)
        line(p5, p8)
        line(p6, p9)
        line(p7, p8)
        line(p8, p9)

        // Extra cross-links
        line(p1, p5)
        line(p2, p4)
        line(p2, p7)
        line(p3, p5)
        line(p5, p7)
        line(p5, p9)
        line(p10, p5)
        line(p11, p5)
        line(p10, p7)
        line(p11, p9)
        line(p1, p10)
        line(p3, p9)
        line(p2, p10)
        line(p6, p11)
        line(p4, p8)
        line(p3, p11)


        // Moving particles along lines
        val t = anim

        fun lerp(a: Offset, b: Offset, f: Float): Offset {
            return Offset(
                a.x + (b.x - a.x) * f,
                a.y + (b.y - a.y) * f
            )
        }

        val particles = listOf(
            lerp(p1, p2, t),
            lerp(p2, p5, (t + 0.12f) % 1f),
            lerp(p5, p6, (t + 0.24f) % 1f),
            lerp(p4, p5, (t + 0.36f) % 1f),
            lerp(p7, p8, (t + 0.48f) % 1f),
            lerp(p8, p9, (t + 0.60f) % 1f),
            lerp(p10, p5, (t + 0.72f) % 1f),
            lerp(p11, p5, (t + 0.84f) % 1f)
        )

        particles.forEach {
            drawCircle(
                color = Color.White.copy(alpha = 0.55f),
                radius = 2.6f,
                center = it
            )
        }

        points.forEachIndexed { index, point ->
            drawCircle(
                color = if (index in listOf(1, 2, 4, 5, 7, 8)) nodeGreen else nodeGray,
                radius = if (index in listOf(1, 4, 5, 7, 8)) 6.8f else 5.2f,
                center = point
            )
        }
    }
}