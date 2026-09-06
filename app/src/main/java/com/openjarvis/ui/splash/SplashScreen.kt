package com.openjarvis.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openjarvis.R
import kotlinx.coroutines.delay

/**
 * Animated splash screen for Lara AI.
 * Shows a spinning/scaling 3D-style logo, the app name, and developer credits,
 * then calls [onFinished] once the intro animation completes.
 */
@Composable
fun LaraSplashScreen(onFinished: () -> Unit) {
    val scale = remember { Animatable(0f) }
    val rotationY = remember { Animatable(180f) }
    val textAlpha = remember { Animatable(0f) }
    var showContent by remember { mutableStateOf(true) }

    // Gentle continuous glow pulse behind the logo
    val infiniteTransition = rememberInfiniteTransition(label = "splash_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    LaunchedEffect(Unit) {
        // "3D" spin-in: logo rotates in on the Y axis while scaling up
        scale.animateTo(1f, animationSpec = tween(700, easing = EaseOutBack))
        rotationY.animateTo(0f, animationSpec = tween(700, easing = EaseOutBack))
        textAlpha.animateTo(1f, animationSpec = tween(500))

        delay(1200)
        showContent = false
        delay(300)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0F)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(if (showContent) 1f else 0f)
        ) {
            // Glow behind the logo
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .alpha(glowAlpha)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF8B5CF6), Color.Transparent),
                            center = Offset.Unspecified
                        ),
                        shape = RoundedCornerShape(80.dp)
                    )
            )

            // The 3D-style rotating logo card
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        this.rotationY = rotationY.value
                        cameraDistance = 16f * density
                    }
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF8B5CF6), Color(0xFF22D3EE))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "L",
                    color = Color.White,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Lara AI",
                color = Color(0xFFF5F5F7),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Your on-device AI agent",
                color = Color(0xFFA1A1AA),
                fontSize = 14.sp,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = stringResourceCompat(R.string.developer_credit),
                color = Color(0xFF52525B),
                fontSize = 12.sp,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
private fun stringResourceCompat(id: Int): String =
    androidx.compose.ui.res.stringResource(id = id)
