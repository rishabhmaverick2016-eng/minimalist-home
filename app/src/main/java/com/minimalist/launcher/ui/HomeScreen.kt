package com.minimalist.launcher.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalist.launcher.data.AppInfo
import com.minimalist.launcher.ui.theme.getThemePalette
import com.minimalist.launcher.viewmodel.LauncherViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val themeName by viewModel.currentTheme.collectAsState()
    val palette = getThemePalette(themeName)
    val time by viewModel.currentTime.collectAsState()
    val date by viewModel.currentDate.collectAsState()
    val battery by viewModel.batteryLevel.collectAsState()
    val coupleNote by viewModel.coupleNote.collectAsState()
    val favorites by viewModel.favoriteApps.collectAsState()
    val fontSizePref by viewModel.fontSize.collectAsState()

    val itemFontSize = when (fontSizePref) {
        "SMALL" -> 18.sp
        "LARGE" -> 26.sp
        else -> 22.sp // MEDIUM
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    // Swipe up opens app drawer
                    if (dragAmount < -25) {
                        viewModel.openScreen("DRAWER")
                    }
                }
            }
            .padding(horizontal = 28.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP SECTION: Status, Time, Date & Mindful Note
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                // Header with battery and settings icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$battery%",
                        color = palette.textSecondary,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )

                    IconButton(
                        onClick = { viewModel.openScreen("SETTINGS") },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Settings",
                            tint = palette.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Clock
                Text(
                    text = time,
                    color = palette.textPrimary,
                    fontSize = 62.sp,
                    fontWeight = FontWeight.ExtraLight,
                    letterSpacing = (-1).sp
                )

                // Date
                Text(
                    text = date,
                    color = palette.textSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Couple Note / Daily Mindful Mantra
                if (coupleNote.isNotBlank()) {
                    Text(
                        text = "— $coupleNote",
                        color = palette.textSecondary.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }

            // MIDDLE SECTION: Pinned Clean Text Favorite Apps
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                if (favorites.isEmpty()) {
                    Text(
                        text = "no favorite apps yet\nswipe up to browse & hold to pin",
                        color = palette.textSecondary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                } else {
                    favorites.forEach { app ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { viewModel.onAppClick(app) },
                                    onLongClick = { viewModel.openContextMenu(app) }
                                )
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = app.displayName,
                                color = palette.textPrimary,
                                fontSize = itemFontSize,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.5.sp
                            )
                            if (app.hasMindfulDelay) {
                                Text(
                                    text = "  ⏳",
                                    fontSize = 12.sp,
                                    color = palette.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // BOTTOM SECTION: "All Apps" indicator & swipe cue
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openScreen("DRAWER") }
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowUp,
                    contentDescription = "Swipe up for all apps",
                    tint = palette.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "all apps",
                    color = palette.textSecondary,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}
