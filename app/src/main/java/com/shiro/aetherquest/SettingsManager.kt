package com.shiro.aetherquest

import android.content.Context

object SettingsManager {
    private const val PREFS = "aetherquest_settings"
    private const val KEY_MUSIC_ENABLED = "music_enabled"
    private const val KEY_SFX_ENABLED = "sfx_enabled"
    private const val KEY_MASTER_VOLUME = "master_volume"

    fun isMusicEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_MUSIC_ENABLED, true)
    }

    fun isSfxEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_SFX_ENABLED, true)
    }

    fun getMasterVolume(context: Context): Float {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getFloat(KEY_MASTER_VOLUME, 0.75f)
            .coerceIn(0f, 1f)
    }

    fun setMusicEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_MUSIC_ENABLED, value).apply()
    }

    fun setSfxEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_SFX_ENABLED, value).apply()
    }

    fun setMasterVolume(context: Context, value: Float) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putFloat(KEY_MASTER_VOLUME, value.coerceIn(0f, 1f)).apply()
    }
}
