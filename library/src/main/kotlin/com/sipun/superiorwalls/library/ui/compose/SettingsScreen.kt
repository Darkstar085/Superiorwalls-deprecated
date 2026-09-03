package com.sipun.superiorwalls.library.ui.compose

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipun.superiorwalls.library.R
import com.sipun.superiorwalls.library.data.Preferences
import com.sipun.superiorwalls.library.data.viewmodels.LocalesViewModel
import com.sipun.superiorwalls.library.data.viewmodels.ReadableLocale
import com.sipun.superiorwalls.library.extensions.context.clearDataAndCache
import com.sipun.superiorwalls.library.extensions.context.currentVersionCode
import com.sipun.superiorwalls.library.extensions.context.currentVersionName
import com.sipun.superiorwalls.library.extensions.context.dataCacheSize
import com.sipun.superiorwalls.library.extensions.context.getAppName
import com.sipun.superiorwalls.library.extensions.context.hasNotificationsPermission
import com.sipun.superiorwalls.library.extensions.context.openLink
import java.util.Locale

@Composable
fun SettingsScreen(
    preferences: Preferences,
    dashboardName: String,
    dashboardVersion: String,
    onBack: () -> Unit,
    onThemeChanged: () -> Unit,
    onRequestNotificationsPermission: () -> Unit,
) {
    val context = LocalContext.current
    val localesViewModel: LocalesViewModel = viewModel()
    var locales by remember { mutableStateOf<List<ReadableLocale>>(emptyList()) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        localesViewModel.loadAppLocales()
        locales = localesViewModel.locales
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.padding(padding),
        ) {
            item { SettingsHeader(stringResource(R.string.interface_title), Icons.Rounded.Palette) }
            item {
                SettingsAction(
                    Icons.Rounded.Language,
                    stringResource(R.string.app_language),
                    currentLocaleName(),
                ) { showLanguageDialog = true }
            }
            item {
                SettingsAction(
                    Icons.Rounded.DarkMode,
                    stringResource(R.string.app_theme),
                    stringResource(Preferences.ThemeKey.fromValue(preferences.currentTheme.value).stringResId),
                ) { showThemeDialog = true }
            }
            item {
                SettingsSwitch(
                    Icons.Rounded.ColorLens,
                    stringResource(R.string.use_amoled_theme),
                    stringResource(R.string.use_amoled_theme_summary),
                    preferences.usesAmoledTheme,
                ) {
                    preferences.usesAmoledTheme = it
                    onThemeChanged()
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item {
                    SettingsSwitch(
                        Icons.Rounded.Palette,
                        "Material You",
                        "Use dynamic system colors",
                        preferences.useMaterialYou,
                    ) {
                        preferences.useMaterialYou = it
                        onThemeChanged()
                    }
                }
            }
            item {
                SettingsSwitch(
                    Icons.Rounded.ColorLens,
                    stringResource(R.string.colored_navigation_bar),
                    "Match the app theme",
                    preferences.shouldColorNavbar,
                ) {
                    preferences.shouldColorNavbar = it
                    onThemeChanged()
                }
            }
            item {
                SettingsSwitch(
                    Icons.Rounded.Palette,
                    stringResource(R.string.interface_animations),
                    stringResource(R.string.interface_animations_summary),
                    preferences.animationsEnabled,
                ) { preferences.animationsEnabled = it }
            }

            item { SettingsHeader(stringResource(R.string.data_and_storage), Icons.Rounded.Storage) }
            item {
                SettingsSwitch(
                    Icons.Rounded.Storage,
                    stringResource(R.string.display_full_res_previews),
                    stringResource(R.string.display_full_res_previews_summary),
                    preferences.shouldLoadFullResPictures,
                ) { preferences.shouldLoadFullResPictures = it }
            }
            item {
                SettingsSwitch(
                    Icons.Rounded.Wifi,
                    stringResource(R.string.download_on_wifi_only),
                    stringResource(R.string.download_on_wifi_only_summary),
                    preferences.shouldDownloadOnWiFiOnly,
                ) { preferences.shouldDownloadOnWiFiOnly = it }
            }
            item {
                SettingsSwitch(
                    Icons.Rounded.Palette,
                    stringResource(R.string.crop_pictures),
                    stringResource(R.string.crop_pictures_summary),
                    preferences.shouldCropWallpaperBeforeApply,
                ) { preferences.shouldCropWallpaperBeforeApply = it }
            }
            item {
                SettingsAction(
                    Icons.Rounded.Storage,
                    stringResource(R.string.download_location),
                    preferences.downloadsFolder.toString(),
                )
            }
            item {
                SettingsAction(Icons.Rounded.Cached, stringResource(R.string.clear_data_cache), context.dataCacheSize) {
                    showClearCacheDialog = true
                }
            }

            item { SettingsHeader(stringResource(R.string.notifications), Icons.Rounded.Notifications) }
            item {
                SettingsSwitch(
                    Icons.Rounded.Notifications,
                    stringResource(R.string.enable_notifications),
                    "Get notified about new wallpapers",
                    preferences.notificationsEnabled,
                ) {
                    preferences.notificationsEnabled = it
                    if (it && !context.hasNotificationsPermission) onRequestNotificationsPermission()
                }
            }

            item { SettingsHeader(stringResource(R.string.credits), Icons.Rounded.ColorLens) }
            item { SettingsAction(Icons.Rounded.ColorLens, dashboardName, dashboardVersion) }
            item {
                SettingsAction(Icons.Rounded.ColorLens, "Dashboard developer", "Jahir Fiquitiva") {
                    context.openLink("https://github.com/jahirfiquitiva/")
                }
            }
            item {
                SettingsAction(Icons.Rounded.ColorLens, "App developer", "Sipun Ku Mahanta") {
                    context.openLink("https://github.com/Darkstar085/")
                }
            }
            item {
                SettingsAction(Icons.Rounded.ColorLens, stringResource(R.string.privacy_policy), "") {
                    context.getString(R.string.privacy_policy_link).takeIf(String::isNotBlank)?.let(context::openLink)
                }
            }
            item {
                SettingsAction(Icons.Rounded.ColorLens, stringResource(R.string.terms_conditions), "") {
                    context.getString(R.string.terms_conditions_link).takeIf(String::isNotBlank)?.let(context::openLink)
                }
            }
            item {
                SettingsAction(
                    Icons.Rounded.ColorLens,
                    context.getAppName(),
                    "${context.currentVersionName} (${context.currentVersionCode})",
                )
            }
        }
    }

    if (showThemeDialog) {
        ThemeDialog(preferences.currentTheme.value) {
            preferences.currentTheme = Preferences.ThemeKey.fromValue(it)
            showThemeDialog = false
            onThemeChanged()
        }
    }
    if (showLanguageDialog) {
        LanguageDialog(locales, AppCompatDelegate.getApplicationLocales().get(0)?.toLanguageTag()) {
            AppCompatDelegate.setApplicationLocales(androidx.core.os.LocaleListCompat.forLanguageTags(it))
            showLanguageDialog = false
        }
    }
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text(stringResource(R.string.clear_data_cache)) },
            text = { Text("Cached wallpaper data will be removed. Your favorites are kept.") },
            confirmButton = {
                TextButton(onClick = {
                    context.clearDataAndCache()
                    showClearCacheDialog = false
                }) { Text(stringResource(R.string.clear)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun SettingsHeader(title: String, icon: ImageVector) {
    Row(
        Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun SettingsSwitch(
    icon: ImageVector,
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(summary) },
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent = { Switch(checked, onCheckedChange) },
    )
    HorizontalDivider(Modifier.padding(horizontal = 20.dp))
}

@Composable
private fun SettingsAction(
    icon: ImageVector,
    title: String,
    summary: String = "",
    onClick: (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { if (summary.isNotBlank()) Text(summary, maxLines = 2) },
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier),
    )
    HorizontalDivider(Modifier.padding(horizontal = 20.dp))
}

@Composable
private fun ThemeDialog(selected: Int, onSelect: (Int) -> Unit) {
    val themes = listOf(Preferences.ThemeKey.LIGHT, Preferences.ThemeKey.DARK, Preferences.ThemeKey.FOLLOW_SYSTEM)
    AlertDialog(
        onDismissRequest = { onSelect(selected) },
        title = { Text(stringResource(R.string.app_theme)) },
        text = {
            Column(Modifier.selectableGroup()) {
                themes.forEach { theme ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected == theme.value, role = Role.RadioButton) { onSelect(theme.value) },
                    ) {
                        RadioButton(selected == theme.value, onClick = { onSelect(theme.value) })
                        Text(stringResource(theme.stringResId))
                    }
                }
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun LanguageDialog(locales: List<ReadableLocale>, selected: String?, onSelect: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = { onSelect(selected ?: Locale.getDefault().toLanguageTag()) },
        title = { Text(stringResource(R.string.app_language)) },
        text = {
            Column(Modifier.selectableGroup()) {
                locales.forEach { locale ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected == locale.tag, role = Role.RadioButton) { onSelect(locale.tag) },
                    ) {
                        RadioButton(selected == locale.tag, onClick = { onSelect(locale.tag) })
                        Text(locale.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSelect(selected ?: Locale.getDefault().toLanguageTag()) }) {
                Text(stringResource(R.string.close))
            }
        },
    )
}

@Composable
private fun currentLocaleName(): String {
    val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
    return locale.getDisplayName(locale).ifBlank { locale.displayName }
}
