package com.minimalist.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.minimalist.launcher.ui.AppContextMenuDialog
import com.minimalist.launcher.ui.AppDrawerScreen
import com.minimalist.launcher.ui.HomeScreen
import com.minimalist.launcher.ui.MindfulDelayDialog
import com.minimalist.launcher.ui.RenameAppDialog
import com.minimalist.launcher.ui.SettingsScreen
import com.minimalist.launcher.ui.theme.MinimalistLauncherTheme
import com.minimalist.launcher.viewmodel.LauncherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val currentTheme by viewModel.currentTheme.collectAsState()
            val activeScreen by viewModel.activeScreen.collectAsState()
            val mindfulTarget by viewModel.mindfulAppToLaunch.collectAsState()
            val contextMenuApp by viewModel.contextMenuApp.collectAsState()
            val renameTarget by viewModel.renameTarget.collectAsState()

            // System Back Button Handling
            BackHandler(enabled = activeScreen != "HOME") {
                viewModel.openScreen("HOME")
            }

            MinimalistLauncherTheme(themeName = currentTheme) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (activeScreen) {
                        "DRAWER" -> AppDrawerScreen(viewModel = viewModel)
                        "SETTINGS" -> SettingsScreen(viewModel = viewModel)
                        else -> HomeScreen(viewModel = viewModel)
                    }

                    // Mindful Delay (15-second breathing countdown before launching tempting apps)
                    mindfulTarget?.let { app ->
                        MindfulDelayDialog(
                            app = app,
                            themeName = currentTheme,
                            onContinue = {
                                viewModel.dismissMindfulDialog()
                                viewModel.launchAppDirectly(app.packageName)
                            },
                            onCancel = {
                                viewModel.dismissMindfulDialog()
                            }
                        )
                    }

                    // Context Menu for Long-Pressed Apps
                    contextMenuApp?.let { app ->
                        AppContextMenuDialog(
                            app = app,
                            themeName = currentTheme,
                            viewModel = viewModel
                        )
                    }

                    // Rename App Dialog
                    renameTarget?.let { app ->
                        RenameAppDialog(
                            app = app,
                            themeName = currentTheme,
                            onSave = { newName ->
                                viewModel.saveRenamedApp(app, newName)
                            },
                            onDismiss = {
                                viewModel.dismissRenameDialog()
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadInstalledApps()
    }
}
