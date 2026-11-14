package com.example.floatinglock.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import com.example.floatinglock.data.BubbleSettings
import com.example.floatinglock.overlay.OverlayController

class LockAccessibilityService : AccessibilityService() {

    private var controller: OverlayController? = null

    // SharedPreferences keeps listeners in a weak set, so this must stay a field.
    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == BubbleSettings.KEY_ENABLED) syncBubble()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        BubbleSettings.prefs(this).registerOnSharedPreferenceChangeListener(prefsListener)
        syncBubble()
    }

    private fun syncBubble() {
        if (BubbleSettings.isEnabled(this)) {
            if (controller == null) {
                controller = OverlayController(this, ::lockScreen, ::captureScreen).also { it.show() }
            }
        } else {
            controller?.hide()
            controller = null
        }
    }

    private fun lockScreen() {
        performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
    }

    private fun captureScreen() {
        performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        controller?.onScreenChanged()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        tearDown()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        tearDown()
        super.onDestroy()
    }

    private fun tearDown() {
        BubbleSettings.prefs(this).unregisterOnSharedPreferenceChangeListener(prefsListener)
        controller?.hide()
        controller = null
    }

    companion object {
        fun isEnabled(context: Context): Boolean {
            val manager = context.getSystemService(AccessibilityManager::class.java) ?: return false
            return manager
                .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
                .any {
                    val info = it.resolveInfo.serviceInfo
                    info.packageName == context.packageName &&
                            info.name == LockAccessibilityService::class.java.name
                }
        }
    }
}