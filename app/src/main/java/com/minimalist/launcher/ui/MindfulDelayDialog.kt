package com.minimalist.launcher.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.minimalist.launcher.data.AppInfo
import com.minimalist.launcher.ui.theme.getThemePalette
import kotlinx.coroutines.delay

@Composable
fun MindfulDelayDialog(
    app: AppInfo,
    themeName: String,
    onContinue: () -> Unit,
    onCancel: () -> Unit
) {
    val palette = getThemePalette(themeName)
    val totalSeconds = 15
    var secondsLeft by remember { mutableIntStateOf(totalSeconds) }
    var isReady by remember { mutableStateOf(false) }
    val progress = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = totalSeconds * 1000, easing = LinearEasing)
        )
    }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
        }
        isReady = true
    }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.background.copy(alpha = 0.96f))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "take a breath",
                    color = palette.textSecondary,
                    fontSize = 16.sp,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Do you really need to open\n${app.displayName}?",
                    color = palette.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(140.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { progress.value },
                        modifier = Modifier.fillMaxSize(),
                        color = palette.textPrimary,
                        trackColor = palette.divider,
                        strokeWidth = 3.dp
                    )
                    Text(
                        text = if (isReady) "✓" else "${secondsLeft}s",
                        color = palette.textPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Light
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Primary Cancel Button (Encourages putting phone down)
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.textPrimary,
                        contentColor = palette.background
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(52.dp)
                ) {
                    Text(
                        text = "Nevermind, stay present",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Continue Button (Enabled only after 15s delay)
                OutlinedButton(
                    onClick = onContinue,
                    enabled = isReady,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(48.dp)
                ) {
                    Text(
                        text = if (isReady) "Continue to ${app.displayName}" else "Please wait ${secondsLeft}s...",
                        color = if (isReady) palette.textPrimary else palette.textSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
