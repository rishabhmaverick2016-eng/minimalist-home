package com.minimalist.launcher.data

data class AppInfo(
    val packageName: String,
    val activityName: String,
    val originalLabel: String,
    val customLabel: String = originalLabel,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val hasMindfulDelay: Boolean = false,
    val delaySeconds: Int = 15
) {
    val displayName: String
        get() = customLabel.ifBlank { originalLabel }
}
