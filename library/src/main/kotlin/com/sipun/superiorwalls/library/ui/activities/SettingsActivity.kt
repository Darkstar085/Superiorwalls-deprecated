package com.sipun.superiorwalls.library.ui.activities

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import com.sipun.superiorwalls.library.BuildConfig
import com.sipun.superiorwalls.library.data.Preferences
import com.sipun.superiorwalls.library.extensions.context.setDefaultDashboardTheme
import com.sipun.superiorwalls.library.ui.activities.base.BasePermissionsRequestActivity
import com.sipun.superiorwalls.library.ui.compose.SettingsScreen
import com.sipun.superiorwalls.library.ui.compose.SuperiorwallsTheme

open class SettingsActivity : BasePermissionsRequestActivity<Preferences>() {

    override val preferences: Preferences by lazy { Preferences(this) }

    private val preferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, prefKey ->
            when (prefKey) {
                Preferences.CURRENT_THEME -> {
                    setDefaultDashboardTheme()
                    onThemeChanged()
                }
                Preferences.USES_AMOLED_THEME,
                Preferences.MATERIAL_YOU_ENABLED,
                Preferences.SHOULD_COLOR_NAVBAR -> onThemeChanged()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences.registerOnSharedPreferenceChangeListener(preferencesListener)

        setContent {
            val themeKey = preferences.currentTheme.value
            val darkTheme = when (themeKey) {
                Preferences.ThemeKey.DARK.value -> true
                Preferences.ThemeKey.LIGHT.value -> false
                else -> resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            }
            SuperiorwallsTheme(
                darkTheme = darkTheme,
                dynamicColor = preferences.useMaterialYou,
                amoled = preferences.usesAmoledTheme,
            ) {
                SettingsScreen(
                    preferences = preferences,
                    dashboardName = dashboardName,
                    dashboardVersion = dashboardVersion,
                    onBack = { supportFinishAfterTransition() },
                    onThemeChanged = { onThemeChanged(); recreate() },
                    onRequestNotificationsPermission = { requestNotificationsPermission() },
                )
            }
        }
    }

    override fun onDestroy() {
        preferences.unregisterOnSharedPreferenceChangeListener(preferencesListener)
        super.onDestroy()
    }

    open val dashboardName: String = BuildConfig.DASHBOARD_NAME
    open val dashboardVersion: String = BuildConfig.DASHBOARD_VERSION
}
