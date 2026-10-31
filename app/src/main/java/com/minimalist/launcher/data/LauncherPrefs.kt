package com.minimalist.launcher.data

import android.content.Context
import android.content.SharedPreferences

class LauncherPrefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("minimalist_launcher_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FAVORITES = "pref_favorites"
        private const val KEY_HIDDEN = "pref_hidden"
        private const val KEY_MINDFUL = "pref_mindful"
        private const val KEY_THEME = "pref_theme"
        private const val KEY_FONT_SIZE = "pref_font_size"
        private const val KEY_COUPLE_NOTE = "pref_couple_note"
        private const val KEY_SHOW_BATTERY = "pref_show_battery"
        private const val KEY_SHOW_DATE = "pref_show_date"
        private const val PREFIX_CUSTOM_NAME = "custom_name_"
    }

    fun getFavorites(): List<String> {
        val raw = prefs.getString(KEY_FAVORITES, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(",").filter { it.isNotBlank() }
    }

    fun saveFavorites(list: List<String>) {
        prefs.edit().putString(KEY_FAVORITES, list.joinToString(",")).apply()
    }

    fun getHiddenApps(): Set<String> {
        return prefs.getStringSet(KEY_HIDDEN, emptySet()) ?: emptySet()
    }

    fun saveHiddenApps(set: Set<String>) {
        prefs.edit().putStringSet(KEY_HIDDEN, set).apply()
    }

    fun getMindfulApps(): Set<String> {
        return prefs.getStringSet(KEY_MINDFUL, emptySet()) ?: emptySet()
    }

    fun saveMindfulApps(set: Set<String>) {
        prefs.edit().putStringSet(KEY_MINDFUL, set).apply()
    }

    fun getCustomName(packageName: String): String? {
        return prefs.getString(PREFIX_CUSTOM_NAME + packageName, null)
    }

    fun setCustomName(packageName: String, customName: String?) {
        val editor = prefs.edit()
        if (customName.isNullOrBlank()) {
            editor.remove(PREFIX_CUSTOM_NAME + packageName)
        } else {
            editor.putString(PREFIX_CUSTOM_NAME + packageName, customName.trim())
        }
        editor.apply()
    }

    fun getTheme(): String {
        return prefs.getString(KEY_THEME, "OLED_BLACK") ?: "OLED_BLACK"
    }

    fun setTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
    }

    fun getFontSize(): String {
        return prefs.getString(KEY_FONT_SIZE, "MEDIUM") ?: "MEDIUM"
    }

    fun setFontSize(size: String) {
        prefs.edit().putString(KEY_FONT_SIZE, size).apply()
    }

    fun getCoupleNote(): String {
        return prefs.getString(KEY_COUPLE_NOTE, "breathe & be present") ?: "breathe & be present"
    }

    fun setCoupleNote(note: String) {
        prefs.edit().putString(KEY_COUPLE_NOTE, note).apply()
    }

    fun isShowBattery(): Boolean {
        return prefs.getBoolean(KEY_SHOW_BATTERY, true)
    }

    fun setShowBattery(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_BATTERY, show).apply()
    }

    fun isShowDate(): Boolean {
        return prefs.getBoolean(KEY_SHOW_DATE, true)
    }

    fun setShowDate(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_DATE, show).apply()
    }
}
