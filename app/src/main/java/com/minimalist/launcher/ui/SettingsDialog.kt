package com.minimalist.launcher.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.minimalist.launcher.data.AppInfo
import com.minimalist.launcher.ui.theme.getThemePalette
import com.minimalist.launcher.viewmodel.LauncherViewModel

@Composable
fun SettingsScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeName by viewModel.currentTheme.collectAsState()
    val palette = getThemePalette(themeName)
    val fontSizePref by viewModel.fontSize.collectAsState()
    val coupleNote by viewModel.coupleNote.collectAsState()
    val allApps by viewModel.allApps.collectAsState()
    val hiddenApps = allApps.filter { it.isHidden }

    var editingNote by remember { mutableStateOf(coupleNote) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                IconButton(
                    onClick = { viewModel.openScreen("HOME") },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.textSecondary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "preferences",
                    color = palette.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(28.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // SET AS DEFAULT LAUNCHER BUTTON
                item {
                    Button(
                        onClick = {
                            openDefaultLauncherSettings(context)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.textPrimary,
                            contentColor = palette.background
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Set as Default Home App",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "Makes this your phone's main home screen when pressing home",
                        color = palette.textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                    )
                }

                // THEME SELECTOR
                item {
                    Text(
                        text = "THEME",
                        color = palette.textSecondary,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThemeOptionCard(
                            label = "OLED Black",
                            selected = themeName == "OLED_BLACK",
                            palette = palette,
                            onClick = { viewModel.setTheme("OLED_BLACK") },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionCard(
                            label = "Clean Light",
                            selected = themeName == "LIGHT_CLEAN",
                            palette = palette,
                            onClick = { viewModel.setTheme("LIGHT_CLEAN") },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionCard(
                            label = "Warm Paper",
                            selected = themeName == "WARM_PAPER",
                            palette = palette,
                            onClick = { viewModel.setTheme("WARM_PAPER") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // FONT SIZE
                item {
                    Text(
                        text = "FONT SIZE",
                        color = palette.textSecondary,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FontSizeOptionCard(
                            label = "Small",
                            selected = fontSizePref == "SMALL",
                            palette = palette,
                            onClick = { viewModel.setFontSize("SMALL") },
                            modifier = Modifier.weight(1f)
                        )
                        FontSizeOptionCard(
                            label = "Medium",
                            selected = fontSizePref == "MEDIUM",
                            palette = palette,
                            onClick = { viewModel.setFontSize("MEDIUM") },
                            modifier = Modifier.weight(1f)
                        )
                        FontSizeOptionCard(
                            label = "Large",
                            selected = fontSizePref == "LARGE",
                            palette = palette,
                            onClick = { viewModel.setFontSize("LARGE") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // COUPLE MANTRA / DAILY NOTE
                item {
                    Text(
                        text = "COUPLE MANTRA / DAILY NOTE",
                        color = palette.textSecondary,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editingNote,
                        onValueChange = {
                            editingNote = it
                            viewModel.setCoupleNote(it)
                        },
                        placeholder = { Text("e.g. together & mindful", color = palette.textSecondary) },
                        textStyle = TextStyle(color = palette.textPrimary, fontSize = 15.sp),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text(
                        text = "Displays as a gentle mindful note under the clock on your home screen",
                        color = palette.textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                // HIDDEN APPS SECTION
                item {
                    Text(
                        text = "HIDDEN APPS (${hiddenApps.size})",
                        color = palette.textSecondary,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (hiddenApps.isEmpty()) {
                        Text(
                            text = "No apps are hidden. (Long-press any app in all apps to hide it)",
                            color = palette.textSecondary,
                            fontSize = 14.sp
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            hiddenApps.forEach { app ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(palette.surface, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = app.displayName,
                                        color = palette.textPrimary,
                                        fontSize = 15.sp
                                    )
                                    TextButton(onClick = { viewModel.toggleHideApp(app) }) {
                                        Text("Unhide", color = palette.textPrimary, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // FOOTER / ABOUT
                item {
                    Divider(color = palette.divider)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Minimalist Home • Couple Edition\n100% Free • Zero Ads • No Paywall",
                        color = palette.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionCard(
    label: String,
    selected: Boolean,
    palette: com.minimalist.launcher.ui.theme.MinimalistColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .background(
                if (selected) palette.textPrimary else palette.surface,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) palette.background else palette.textPrimary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun FontSizeOptionCard(
    label: String,
    selected: Boolean,
    palette: com.minimalist.launcher.ui.theme.MinimalistColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .background(
                if (selected) palette.textPrimary else palette.surface,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) palette.background else palette.textPrimary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// CONTEXT MENU DIALOG (Long press on any app)
@Composable
fun AppContextMenuDialog(
    app: AppInfo,
    themeName: String,
    viewModel: LauncherViewModel
) {
    val context = LocalContext.current
    val palette = getThemePalette(themeName)

    Dialog(onDismissRequest = { viewModel.dismissContextMenu() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.surface, RoundedCornerShape(18.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = app.displayName,
                    color = palette.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Divider(color = palette.divider)

                // 1. Favorite toggle
                ContextMenuItem(
                    icon = Icons.Outlined.Star,
                    label = if (app.isFavorite) "Remove from Home favorites" else "Add to Home favorites",
                    palette = palette,
                    onClick = { viewModel.toggleFavorite(app) }
                )

                // 2. Mindful Delay toggle (15-sec breathing pause)
                ContextMenuItem(
                    icon = Icons.Outlined.HourglassEmpty,
                    label = if (app.hasMindfulDelay) "Remove 15s mindful pause" else "Add 15s mindful pause (delay temptation)",
                    palette = palette,
                    onClick = { viewModel.toggleMindfulDelay(app) }
                )

                // 3. Rename app
                ContextMenuItem(
                    icon = Icons.Outlined.Edit,
                    label = "Rename app",
                    palette = palette,
                    onClick = { viewModel.openRenameDialog(app) }
                )

                // 4. Hide app
                ContextMenuItem(
                    icon = Icons.Outlined.VisibilityOff,
                    label = if (app.isHidden) "Unhide app" else "Hide app from drawer",
                    palette = palette,
                    onClick = { viewModel.toggleHideApp(app) }
                )

                // 5. System app info
                ContextMenuItem(
                    icon = Icons.Outlined.Info,
                    label = "App info in Android settings",
                    palette = palette,
                    onClick = {
                        viewModel.dismissContextMenu()
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", app.packageName, null)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                )
            }
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    palette: com.minimalist.launcher.ui.theme.MinimalistColors,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = palette.textSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            color = palette.textPrimary,
            fontSize = 15.sp
        )
    }
}

// RENAME APP DIALOG
@Composable
fun RenameAppDialog(
    app: AppInfo,
    themeName: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = getThemePalette(themeName)
    var text by remember { mutableStateOf(app.displayName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename ${app.originalLabel}", color = palette.textPrimary) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                placeholder = { Text(app.originalLabel) },
                textStyle = TextStyle(color = palette.textPrimary)
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }) {
                Text("Save", color = palette.textPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = palette.textSecondary)
            }
        },
        containerColor = palette.surface
    )
}

fun openDefaultLauncherSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
