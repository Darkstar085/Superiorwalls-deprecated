package com.sipun.superiorwalls.library.ui.activities.base

import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.sipun.superiorwalls.library.data.Preferences

abstract class BaseSystemUIVisibilityActivity<out P : Preferences> :
    BasePermissionsRequestActivity<P>() {

    private var visibleSystemUI = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        visibleSystemUI = savedInstanceState?.getBoolean(VISIBLE_SYSTEM_UI_KEY, true) ?: true
        if (canToggleSystemUIVisibility()) applySystemUIVisibility(visibleSystemUI)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(VISIBLE_SYSTEM_UI_KEY, visibleSystemUI)
        super.onSaveInstanceState(outState)
    }

    internal fun toggleSystemUI() {
        if (!canToggleSystemUIVisibility()) return
        applySystemUIVisibility(!visibleSystemUI)
    }

    private fun applySystemUIVisibility(visible: Boolean) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (visible) {
            controller.show(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        } else {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        visibleSystemUI = visible
    }

    open fun canToggleSystemUIVisibility(): Boolean = false

    companion object {
        private const val VISIBLE_SYSTEM_UI_KEY = "visible_system_ui"
    }
}
