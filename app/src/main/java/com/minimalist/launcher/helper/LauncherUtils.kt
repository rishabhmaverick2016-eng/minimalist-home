package com.minimalist.launcher.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import com.minimalist.launcher.findMainActivity

object LauncherUtils {

    fun isDefaultLauncher(context: Context): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val resolveInfo = context.packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            val currentDefaultPkg = resolveInfo?.activityInfo?.packageName
            currentDefaultPkg == context.packageName
        } catch (_: Exception) {
            false
        }
    }

    fun promptSetDefaultLauncher(context: Context) {
        val activity = context.findMainActivity()
        if (activity != null) {
            activity.requestDefaultLauncher()
            return
        }

        // Direct Settings Fallback if context is not MainActivity
        val candidateIntents = listOf(
            Intent(Settings.ACTION_HOME_SETTINGS),
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent("android.settings.HOME_SETTINGS"),
            Intent(Settings.ACTION_SETTINGS)
        )

        for (intent in candidateIntents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Toast.makeText(context, "Select 'Minimalist Home' as your Home app", Toast.LENGTH_LONG).show()
                return
            } catch (_: Exception) {}
        }
    }
}
