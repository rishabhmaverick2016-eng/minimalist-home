package com.minimalist.launcher

import android.app.role.RoleManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.minimalist.launcher.helper.LauncherUtils
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

    // ActivityResultLauncher is mandatory for RoleManager.createRequestRoleIntent on Android 10+
    private val roleRequestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkDefaultLauncherStatus()
    }

    fun requestDefaultLauncher() {
        // Method 1: Android 10+ (API 29+) RoleManager using official ActivityResultLauncher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                    try {
                        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                        roleRequestLauncher.launch(intent)
                        return
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        // Method 2: Open System Settings directly to Home App chooser
        val candidateIntents = listOf(
            Intent(Settings.ACTION_HOME_SETTINGS),
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent("android.settings.HOME_SETTINGS"),
            Intent(Settings.ACTION_SETTINGS)
        )

        for (intent in candidateIntents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Toast.makeText(this, "Select 'Minimalist Home' as your Home app", Toast.LENGTH_LONG).show()
                return
            } catch (_: Exception) {}
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Automatically prompt to set as default launcher on first launch if not yet set
        if (!LauncherUtils.isDefaultLauncher(this)) {
            requestDefaultLauncher()
        }

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
        viewModel.checkDefaultLauncherStatus()
        viewModel.loadInstalledApps()
    }
}

fun Context.findMainActivity(): MainActivity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is MainActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
