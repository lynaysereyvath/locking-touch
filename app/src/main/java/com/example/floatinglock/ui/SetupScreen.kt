package com.example.floatinglock.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.floatinglock.data.BubbleSettings
import com.example.floatinglock.overlay.FloatingBubble
import com.example.floatinglock.service.LockAccessibilityService
import com.example.floatinglock.ui.theme.LedAmber
import com.example.floatinglock.ui.theme.LedMint
import androidx.core.net.toUri

@Composable
fun SetupScreen() {
    val context = LocalContext.current
    var serviceEnabled by remember { mutableStateOf(LockAccessibilityService.isEnabled(context)) }
    var bubbleOn by remember { mutableStateOf(BubbleSettings.isEnabled(context)) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        serviceEnabled = LockAccessibilityService.isEnabled(context)
        bubbleOn = BubbleSettings.isEnabled(context)
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(28.dp),
            ) {
                FloatingBubble(size = 64.dp, pressed = false, interactionTick = 0, dimWhenIdle = false)
            }

            Spacer(Modifier.height(28.dp))
            Text(
                text = "Lock or capture the screen",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "A small button that floats over every app. Tap it to lock the phone or take a screenshot. Useful when the power and volume keys are hard to reach or worn out.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(36.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            SetupStep(
                number = 1,
                title = "Allow the lock action",
                body = "Android only lets an app lock the screen or take screenshots through an accessibility service. Turn on Floating Lock in the list. It can't read your screen and collects nothing.",
                ready = serviceEnabled,
                readyLabel = "Enabled",
                waitingLabel = "Waiting for you to enable it",
            ) {
                val openSettings = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
                if (serviceEnabled) {
                    OutlinedButton(
                        onClick = openSettings,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) { Text("Manage in settings") }
                } else {
                    Button(
                        onClick = openSettings,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) { Text("Open accessibility settings") }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            SetupStep(
                number = 2,
                title = "Show the floating button",
                body = "Drag it anywhere. It rests against the nearest edge and fades when idle.",
                ready = serviceEnabled && bubbleOn,
                readyLabel = "Showing",
                waitingLabel = if (serviceEnabled) "Hidden" else "Needs step 1",
                modifier = Modifier.alpha(if (serviceEnabled) 1f else 0.5f),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Floating button",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Switch(
                        checked = bubbleOn,
                        enabled = serviceEnabled,
                        onCheckedChange = {
                            bubbleOn = it
                            BubbleSettings.setEnabled(context, it)
                        },
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            // ── Footnote ─────────────────────────────────────────────────────
            Spacer(Modifier.height(24.dp))
            Text(
                text = "The accessibility switch is greyed out?",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "If you installed this app from a file instead of the Play Store, Android 13 and newer blocks it by default. Open App info, tap the three-dot menu, then choose Allow restricted settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(
                onClick = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            "package:${context.packageName}".toUri(),
                        ),
                    )
                },
            ) { Text("Open app info") }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SetupStep(
    number: Int,
    title: String,
    body: String,
    ready: Boolean,
    readyLabel: String,
    waitingLabel: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(14.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            StatusLine(ready = ready, label = if (ready) readyLabel else waitingLabel)
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
private fun StatusLine(ready: Boolean, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (ready) LedMint else LedAmber),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}