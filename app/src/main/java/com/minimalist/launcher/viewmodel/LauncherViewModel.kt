package com.minimalist.launcher.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.minimalist.launcher.data.AppInfo
import com.minimalist.launcher.data.LauncherPrefs
import com.minimalist.launcher.helper.LauncherUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LauncherViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context get() = getApplication<Application>().applicationContext
    val prefs = LauncherPrefs(context)

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val allApps: StateFlow<List<AppInfo>> = _allApps.asStateFlow()

    private val _favoriteApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val favoriteApps: StateFlow<List<AppInfo>> = _favoriteApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentTime = MutableStateFlow("")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _activeScreen = MutableStateFlow("HOME") // "HOME", "DRAWER", "SETTINGS"
    val activeScreen: StateFlow<String> = _activeScreen.asStateFlow()

    private val _mindfulAppToLaunch = MutableStateFlow<AppInfo?>(null)
    val mindfulAppToLaunch: StateFlow<AppInfo?> = _mindfulAppToLaunch.asStateFlow()

    private val _contextMenuApp = MutableStateFlow<AppInfo?>(null)
    val contextMenuApp: StateFlow<AppInfo?> = _contextMenuApp.asStateFlow()

    private val _renameTarget = MutableStateFlow<AppInfo?>(null)
    val renameTarget: StateFlow<AppInfo?> = _renameTarget.asStateFlow()

    private val _currentTheme = MutableStateFlow(prefs.getTheme())
    val currentTheme: StateFlow<String> = _currentTheme.asStateFlow()

    private val _fontSize = MutableStateFlow(prefs.getFontSize())
    val fontSize: StateFlow<String> = _fontSize.asStateFlow()

    private val _coupleNote = MutableStateFlow(prefs.getCoupleNote())
    val coupleNote: StateFlow<String> = _coupleNote.asStateFlow()

    private val _isDefaultLauncher = MutableStateFlow(true)
    val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher.asStateFlow()

    init {
        checkDefaultLauncherStatus()
        loadInstalledApps()
        startClockAndBatteryUpdater()
    }

    fun checkDefaultLauncherStatus() {
        _isDefaultLauncher.value = LauncherUtils.isDefaultLauncher(context)
    }

    fun loadInstalledApps() {
        checkDefaultLauncherStatus()
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)

        val favoritesList = prefs.getFavorites().toMutableList()
        val hiddenSet = prefs.getHiddenApps()
        val mindfulSet = prefs.getMindfulApps()

        val parsedApps = resolveInfos.mapNotNull { resolveInfo ->
            val pkg = resolveInfo.activityInfo.packageName
            // Exclude this launcher itself from the app list
            if (pkg == context.packageName) return@mapNotNull null

            val originalLabel = resolveInfo.loadLabel(pm).toString()
            val customLabel = prefs.getCustomName(pkg) ?: originalLabel
            val isFav = favoritesList.contains(pkg)
            val isHidden = hiddenSet.contains(pkg)
            val isMindful = mindfulSet.contains(pkg)

            AppInfo(
                packageName = pkg,
                activityName = resolveInfo.activityInfo.name,
                originalLabel = originalLabel,
                customLabel = customLabel,
                isFavorite = isFav,
                isHidden = isHidden,
                hasMindfulDelay = isMindful
            )
        }.sortedBy { it.displayName.lowercase(Locale.getDefault()) }

        // If favorites is totally empty on initial first run, default to a few popular apps
        if (favoritesList.isEmpty() && parsedApps.isNotEmpty()) {
            val defaults = parsedApps.filter { app ->
                val p = app.packageName.lowercase()
                p.contains("dialer") || p.contains("phone") ||
                p.contains("messaging") || p.contains("whatsapp") ||
                p.contains("camera") || p.contains("chrome")
            }.take(5)

            if (defaults.isNotEmpty()) {
                val defaultPkgs = defaults.map { it.packageName }
                prefs.saveFavorites(defaultPkgs)
                favoritesList.addAll(defaultPkgs)
            }
        }

        _allApps.value = parsedApps
        updateFavoritesFlow(parsedApps, favoritesList)
    }

    private fun updateFavoritesFlow(apps: List<AppInfo>, favPkgs: List<String>) {
        val appMap = apps.associateBy { it.packageName }
        val orderedFavs = favPkgs.mapNotNull { pkg -> appMap[pkg] }
        _favoriteApps.value = orderedFavs
    }

    private fun startClockAndBatteryUpdater() {
        viewModelScope.launch {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
            while (isActive) {
                val now = Date()
                _currentTime.value = timeFormat.format(now)
                _currentDate.value = dateFormat.format(now)
                _batteryLevel.value = fetchBatteryLevel()
                delay(1000)
            }
        }
    }

    private fun fetchBatteryLevel(): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: run {
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                context.registerReceiver(null, filter)
            }
            batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 100
        }
    }

    fun openScreen(screen: String) {
        _activeScreen.value = screen
        if (screen == "DRAWER") {
            _searchQuery.value = ""
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun onAppClick(app: AppInfo) {
        if (app.hasMindfulDelay) {
            _mindfulAppToLaunch.value = app
        } else {
            launchAppDirectly(app.packageName)
        }
    }

    fun launchAppDirectly(packageName: String) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            }
        } catch (_: Exception) {
            // App might have been uninstalled
        }
    }

    fun dismissMindfulDialog() {
        _mindfulAppToLaunch.value = null
    }

    fun openContextMenu(app: AppInfo) {
        _contextMenuApp.value = app
    }

    fun dismissContextMenu() {
        _contextMenuApp.value = null
    }

    fun toggleFavorite(app: AppInfo) {
        val currentFavs = prefs.getFavorites().toMutableList()
        if (currentFavs.contains(app.packageName)) {
            currentFavs.remove(app.packageName)
        } else {
            currentFavs.add(app.packageName)
        }
        prefs.saveFavorites(currentFavs)
        loadInstalledApps()
        dismissContextMenu()
    }

    fun toggleHideApp(app: AppInfo) {
        val hidden = prefs.getHiddenApps().toMutableSet()
        if (hidden.contains(app.packageName)) {
            hidden.remove(app.packageName)
        } else {
            hidden.add(app.packageName)
            // also remove from favorites if hiding
            val currentFavs = prefs.getFavorites().toMutableList()
            currentFavs.remove(app.packageName)
            prefs.saveFavorites(currentFavs)
        }
        prefs.saveHiddenApps(hidden)
        loadInstalledApps()
        dismissContextMenu()
    }

    fun toggleMindfulDelay(app: AppInfo) {
        val mindful = prefs.getMindfulApps().toMutableSet()
        if (mindful.contains(app.packageName)) {
            mindful.remove(app.packageName)
        } else {
            mindful.add(app.packageName)
        }
        prefs.saveMindfulApps(mindful)
        loadInstalledApps()
        dismissContextMenu()
    }

    fun openRenameDialog(app: AppInfo) {
        _contextMenuApp.value = null
        _renameTarget.value = app
    }

    fun saveRenamedApp(app: AppInfo, newName: String) {
        prefs.setCustomName(app.packageName, newName)
        _renameTarget.value = null
        loadInstalledApps()
    }

    fun dismissRenameDialog() {
        _renameTarget.value = null
    }

    fun setTheme(theme: String) {
        prefs.setTheme(theme)
        _currentTheme.value = theme
    }

    fun setFontSize(size: String) {
        prefs.setFontSize(size)
        _fontSize.value = size
    }

    fun setCoupleNote(note: String) {
        prefs.setCoupleNote(note)
        _coupleNote.value = note
    }
}
