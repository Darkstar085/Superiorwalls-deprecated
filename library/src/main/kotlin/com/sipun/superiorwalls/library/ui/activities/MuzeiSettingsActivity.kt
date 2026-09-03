package com.sipun.superiorwalls.library.ui.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.sipun.superiorwalls.library.R
import com.sipun.superiorwalls.library.data.Preferences
import com.sipun.superiorwalls.library.data.models.Collection
import com.sipun.superiorwalls.library.data.viewmodels.WallpapersDataViewModel
import com.sipun.superiorwalls.library.extensions.context.isNetworkAvailable
import com.sipun.superiorwalls.library.extensions.context.string
import com.sipun.superiorwalls.library.extensions.utils.lazyViewModel
import com.sipun.superiorwalls.library.muzei.SuperiorwallsArtProvider
import com.sipun.superiorwalls.library.ui.activities.base.BaseThemedActivity
import com.sipun.superiorwalls.library.ui.compose.CollectionSelectionDialog
import com.sipun.superiorwalls.library.ui.compose.MuzeiSettingsScreen
import com.sipun.superiorwalls.library.ui.compose.SuperiorwallsTheme
import kotlinx.coroutines.delay

open class MuzeiSettingsActivity : BaseThemedActivity<Preferences>() {

    override val preferences: Preferences by lazy { Preferences(this) }

    private var selectedCollections by mutableStateOf("")
    private var refreshOnWifiOnly by mutableStateOf(false)
    private var showCollectionDialog by mutableStateOf(false)
    private var finishing = false

    open val viewModel: WallpapersDataViewModel by lazyViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedCollections = preferences.muzeiCollections
        refreshOnWifiOnly = preferences.refreshMuzeiOnWiFiOnly
        viewModel.loadData(string(R.string.json_url), loadCollections = true, loadFavorites = false, force = false)

        setContent {
            var collections by remember { mutableStateOf<List<Collection>>(emptyList()) }
            LaunchedEffect(Unit) {
                repeat(40) {
                    collections = viewModel.collections
                    if (collections.isNotEmpty()) return@LaunchedEffect
                    delay(50)
                }
                collections = viewModel.collections
            }
            val darkTheme = when (preferences.currentTheme.value) {
                Preferences.ThemeKey.DARK.value -> true
                Preferences.ThemeKey.LIGHT.value -> false
                else -> resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            }
            SuperiorwallsTheme(darkTheme = darkTheme, dynamicColor = preferences.useMaterialYou, amoled = preferences.usesAmoledTheme) {
                MuzeiSettingsScreen(
                    selectedCollections = selectedCollections,
                    refreshOnWifiOnly = refreshOnWifiOnly,
                    collections = collections,
                    showCollections = shouldShowCollections(),
                    onSelectedCollectionsChange = { selectedCollections = it; saveChanges() },
                    onRefreshOnWifiOnlyChange = { refreshOnWifiOnly = it; saveChanges() },
                    onChooseCollections = {
                        if (isNetworkAvailable()) {
                            if (collections.isNotEmpty()) showCollectionDialog = true
                        } else showNotConnectedDialog()
                    },
                    onBack = ::doFinish,
                )
                if (showCollectionDialog) {
                    CollectionSelectionDialog(
                        collections = collections,
                        selectedCollections = selectedCollections,
                        onDismiss = { showCollectionDialog = false },
                        onConfirm = { selectedCollections = it; saveChanges(); showCollectionDialog = false },
                    )
                }
            }
        }
    }

    private fun saveChanges() {
        preferences.refreshMuzeiOnWiFiOnly = refreshOnWifiOnly
        preferences.muzeiCollections = selectedCollections
    }

    private fun showNotConnectedDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setMessage(R.string.muzei_not_connected_content)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun doFinish() {
        if (finishing) return
        finishing = true
        showCollectionDialog = false
        saveChanges()
        try { viewModel.destroy(this) } catch (_: Exception) { }
        try { startService(getProviderIntent()?.apply { putExtra("restart", true) }) } catch (_: Exception) { }
        supportFinishAfterTransition()
    }

    override fun onDestroy() {
        if (!finishing) doFinish()
        super.onDestroy()
    }

    open fun shouldShowCollections(): Boolean = true

    open fun getProviderIntent(): Intent? = Intent(this, SuperiorwallsArtProvider::class.java)
}
