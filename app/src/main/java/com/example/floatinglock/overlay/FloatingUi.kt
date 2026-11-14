package com.example.floatinglock.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatinglock.ui.theme.Graphite
import com.example.floatinglock.ui.theme.Steel
import com.example.floatinglock.ui.theme.SteelDim
import kotlinx.coroutines.delay

private const val IDLE_DELAY_MS = 3_000L

@Composable
fun FloatingBubble(
    size: Dp,
    pressed: Boolean,
    interactionTick: Int,
    modifier: Modifier = Modifier,
    dimWhenIdle: Boolean = true,
) {
    var idle by remember { mutableStateOf(false) }
    LaunchedEffect(interactionTick) {
        idle = false
        if (dimWhenIdle) {
            delay(IDLE_DELAY_MS)
            idle = true
        }
    }

    val bubbleAlpha by animateFloatAsState(
        targetValue = if (idle && !pressed) 0.45f else 1f,
        animationSpec = tween(350),
        label = "bubbleAlpha",
    )
    val bubbleScale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "bubbleScale",
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                this.alpha = bubbleAlpha
                scaleX = bubbleScale
                scaleY = bubbleScale
            }
            .clip(CircleShape)
            .background(Graphite.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.74f)
                .clip(CircleShape)
                .background(Steel.copy(alpha = 0.24f)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(size * 0.52f)
                    .clip(CircleShape)
                    .background(Steel),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = Graphite,
                    modifier = Modifier.size(size * 0.27f),
                )
            }
        }
    }
}

@Composable
fun ActionMenu(
    visible: Boolean,
    onLock: () -> Unit,
    onScreenshot: () -> Unit,
    onDismiss: () -> Unit,
) {
    val visibleState = remember { MutableTransitionState(false) }
    LaunchedEffect(visible) { visibleState.targetState = visible }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(tween(160)),
            exit = fadeOut(tween(140)),
        ) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
        }

        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(tween(160)) + scaleIn(
                initialScale = 0.86f,
                animationSpec = tween(220, easing = FastOutSlowInEasing),
            ),
            exit = fadeOut(tween(120)) + scaleOut(targetScale = 0.92f, animationSpec = tween(140)),
        ) {
            MenuPanel(onLock = onLock, onScreenshot = onScreenshot)
        }
    }
}

@Composable
private fun MenuPanel(
    onLock: () -> Unit,
    onScreenshot: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(34.dp))
            .background(Graphite)
            // swallow taps on the panel's padding so they don't dismiss the menu
            .pointerInput(Unit) { detectTapGestures { } }
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row {
            MenuTile(label = "Lock screen", onClick = onLock) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = Graphite,
                    modifier = Modifier.size(30.dp),
                )
            }
            MenuTile(label = "Screenshot", onClick = onScreenshot) {
                ScreenshotGlyph(tint = Graphite, glyphSize = 30.dp)
            }
        }
        Text(
            text = "Tap outside to close",
            color = SteelDim,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
        )
    }
}

@Composable
private fun MenuTile(
    label: String,
    onClick: () -> Unit,
    glyph: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Steel),
            contentAlignment = Alignment.Center,
        ) {
            glyph()
        }
        Text(
            text = label,
            color = Steel,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ScreenshotGlyph(tint: Color, glyphSize: Dp) {
    Canvas(Modifier.size(glyphSize)) {
        val w = this.size.width
        val strokeWidth = w * 0.11f
        val inset = strokeWidth / 2f
        val arm = w * 0.3f
        val left = inset
        val top = inset
        val right = w - inset
        val bottom = w - inset

        val corners = Path().apply {
            moveTo(left, top + arm);      lineTo(left, top);        lineTo(left + arm, top)         // top-left
            moveTo(right - arm, top);     lineTo(right, top);       lineTo(right, top + arm)        // top-right
            moveTo(right, bottom - arm);  lineTo(right, bottom);    lineTo(right - arm, bottom)     // bottom-right
            moveTo(left + arm, bottom);   lineTo(left, bottom);     lineTo(left, bottom - arm)      // bottom-left
        }
        drawPath(
            path = corners,
            color = tint,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        drawCircle(color = tint, radius = w * 0.11f, center = center)
    }
}