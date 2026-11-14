package com.example.floatinglock.data

import android.content.Context
import android.content.SharedPreferences

object BubbleSettings {

    const val KEY_ENABLED = "bubble_enabled"
    private const val PREFS = "bubble_settings"
    private const val KEY_ON_RIGHT = "anchor_on_right"
    private const val KEY_Y_FRACTION = "anchor_y_fraction"

    fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENABLED, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    data class Anchor(val onRight: Boolean, val yFraction: Float)

    fun anchor(context: Context): Anchor = prefs(context).let {
        Anchor(
            onRight = it.getBoolean(KEY_ON_RIGHT, true),
            yFraction = it.getFloat(KEY_Y_FRACTION, 0.4f),
        )
    }

    fun saveAnchor(context: Context, anchor: Anchor) {
        prefs(context).edit()
            .putBoolean(KEY_ON_RIGHT, anchor.onRight)
            .putFloat(KEY_Y_FRACTION, anchor.yFraction)
            .apply()
    }
}
