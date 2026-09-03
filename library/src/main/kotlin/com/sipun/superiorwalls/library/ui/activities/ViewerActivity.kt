package com.sipun.superiorwalls.library.ui.activities

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import com.sipun.superiorwalls.library.R
import com.sipun.superiorwalls.library.data.Preferences
import com.sipun.superiorwalls.library.data.models.Wallpaper
import com.sipun.superiorwalls.library.extensions.context.boolean
import com.sipun.superiorwalls.library.extensions.context.firstInstallTime
import com.sipun.superiorwalls.library.extensions.context.isNetworkAvailable
import com.sipun.superiorwalls.library.extensions.context.isWifiConnected
import com.sipun.superiorwalls.library.extensions.context.navigationBarLight
import com.sipun.superiorwalls.library.extensions.context.resolveColor
import com.sipun.superiorwalls.library.extensions.context.statusBarLight
import com.sipun.superiorwalls.library.extensions.context.string
import com.sipun.superiorwalls.library.extensions.fragments.mdDialog
import com.sipun.superiorwalls.library.extensions.fragments.message
import com.sipun.superiorwalls.library.extensions.fragments.positiveButton
import com.sipun.superiorwalls.library.extensions.fragments.title
import com.sipun.superiorwalls.library.extensions.resources.asBitmap
import com.sipun.superiorwalls.library.extensions.resources.toReadableTime
import com.sipun.superiorwalls.library.extensions.utils.MAX_PALETTE_COLORS
import com.sipun.superiorwalls.library.ui.activities.base.BaseWallpaperApplierActivity
import com.sipun.superiorwalls.library.ui.compose.SuperiorwallsTheme
import com.sipun.superiorwalls.library.ui.compose.ViewerScreen
import com.sipun.superiorwalls.library.ui.fragments.WallpapersFragment.Companion.WALLPAPER_EXTRA
import com.sipun.superiorwalls.library.ui.fragments.viewer.DetailsFragment
import com.sipun.superiorwalls.library.ui.fragments.viewer.SetAsOptionsDialog
import kotlinx.coroutines.launch

open class ViewerActivity : BaseWallpaperApplierActivity<Preferences>() {
    override val preferences: Preferences by lazy { Preferences(this) }
    private var wallpaper by androidx.compose.runtime.mutableStateOf<Wallpaper?>(null)
    private var favoritesModified = false
    private var isInFavorites = false
    private var collectionName: String? = null
    private var isForFavs = false
    private val detailsFragment: DetailsFragment by lazy { DetailsFragment.create(shouldShowPaletteDetails = shouldShowWallpapersPalette()) }
    private var downloadBlockedDialog: AlertDialog? = null
    private var applierDialog: DialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        statusBarLight = false
        navigationBarLight = false
        window.statusBarColor = resolveColor(android.R.attr.colorBackground)
        window.navigationBarColor = resolveColor(android.R.attr.colorBackground)
        collectionName = intent?.getStringExtra(CollectionActivity.COLLECTION_NAME_KEY)
        isForFavs = intent?.getBooleanExtra(IS_FOR_FAVS, false) ?: false
        val requestedUrl = savedInstanceState?.getString(WALLPAPER_URL_KEY) ?: intent?.extras?.getParcelable<Wallpaper?>(WALLPAPER_EXTRA)?.url
        wallpapersViewModel.observeFavorites(this) { favorites ->
            isInFavorites = favorites.any { it.url == wallpaperDownloadUrl }
            wallpaper?.let { current -> current.isInFavorites = isInFavorites; wallpaper = current }
        }
        lifecycleScope.launch { configureWallpaper(wallpapersViewModel.findWallpaper(requestedUrl)) }
    }

    private fun configureWallpaper(value: Wallpaper?) {
        if (value == null) { finish(); return }
        value.isInFavorites = isInFavorites || value.isInFavorites
        wallpaper = value
        isInFavorites = value.isInFavorites
        initWallpaperFetcher(value)
        detailsFragment.wallpaper = value
        loadWallpapersData()
        render()
    }

    private fun render() {
        val current = wallpaper ?: return
        setContent {
            val themeKey = preferences.currentTheme.value
            val darkTheme = when (themeKey) {
                Preferences.ThemeKey.DARK.value -> true
                Preferences.ThemeKey.LIGHT.value -> false
                else -> resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            }
            SuperiorwallsTheme(darkTheme = darkTheme, dynamicColor = preferences.useMaterialYou, amoled = preferences.usesAmoledTheme) {
                ViewerScreen(
                    wallpaper = current,
                    canModifyFavorites = canModifyFavorites(),
                    showDownload = current.downloadable != false && shouldShowDownloadOption(),
                    onBack = { supportFinishAfterTransition() },
                    onPrevious = { navigatePrevious(current) },
                    onNext = { navigateNext(current) },
                    onDetails = { detailsFragment.show(supportFragmentManager, DETAILS_TAG) },
                    onDownload = ::checkForDownload,
                    onApply = ::showApplyDialog,
                    onFavorite = { toggleFavorite(current) },
                    onToggleSystemUi = { toggleSystemUI() },
                    onImageLoaded = ::generatePalette,
                )
            }
        }
    }

    private fun navigatePrevious(current: Wallpaper) = lifecycleScope.launch {
        configureWallpaper(if (isForFavs) wallpapersViewModel.getPreviousFavoriteWallpaper(current.url) else wallpapersViewModel.getPreviousWallpaper(current.url, collectionName))
    }

    private fun navigateNext(current: Wallpaper) = lifecycleScope.launch {
        configureWallpaper(if (isForFavs) wallpapersViewModel.getNextFavoriteWallpaper(current.url) else wallpapersViewModel.getNextWallpaper(current.url, collectionName))
    }

    private fun toggleFavorite(current: Wallpaper) {
        if (!canModifyFavorites()) { onFavoritesLocked(); return }
        val newValue = !isInFavorites
        favoritesModified = true
        if (newValue) addToFavorites(current) else removeFromFavorites(current)
        isInFavorites = newValue
        current.isInFavorites = newValue
        wallpaper = current
    }

    private fun generatePalette(drawable: Drawable?) {
        if (!shouldShowWallpapersPalette()) return
        drawable?.asBitmap()?.let { bitmap ->
            Palette.from(bitmap).maximumColorCount(MAX_PALETTE_COLORS * 2).generate { palette ->
                detailsFragment.palette = palette
                palette?.bestSwatch?.rgb?.let { window.statusBarColor = it; window.navigationBarColor = it }
            }
        }
    }

    private fun hasValidNetworkAvailable(): Boolean {
        val wifiOnly = preferences.shouldDownloadOnWiFiOnly
        val connected = isNetworkAvailable()
        val mobileData = wifiOnly && !isWifiConnected && connected
        if (!connected || mobileData) {
            dismissDownloadBlockedDialog()
            downloadBlockedDialog = mdDialog {
                title(R.string.error)
                message(if (mobileData) R.string.data_error_network_wifi_only else R.string.data_error_network)
                positiveButton(android.R.string.ok) { it.dismiss() }
            }
            downloadBlockedDialog?.show()
            return false
        }
        return true
    }

    private fun checkForDownload() {
        if (!shouldShowDownloadOption()) return
        val allowed = boolean(R.bool.allow_immediate_downloads) || System.currentTimeMillis() - firstInstallTime >= MIN_TIME
        if (allowed) { if (hasValidNetworkAvailable()) requestStoragePermission(); return }
        val timeLeft = (MIN_TIME - (System.currentTimeMillis() - firstInstallTime)).toReadableTime()
        dismissDownloadBlockedDialog()
        downloadBlockedDialog = mdDialog {
            title(R.string.prevent_download_title)
            message(string(R.string.prevent_download_content, timeLeft))
            positiveButton(android.R.string.ok) { it.dismiss() }
        }
        downloadBlockedDialog?.show()
    }

    private fun showApplyDialog() {
        dismissApplierDialog()
        applierDialog = SetAsOptionsDialog().also { it.show(supportFragmentManager, SetAsOptionsDialog.TAG) }
    }

    override fun internalOnPermissionsGranted(permission: String) {
        super.internalOnPermissionsGranted(permission)
        if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE) startDownload()
    }

    open fun handleNavigationItemSelected(itemId: Int, wallpaper: Wallpaper?): Boolean {
        wallpaper ?: return false
        when (itemId) {
            R.id.details -> detailsFragment.show(supportFragmentManager, DETAILS_TAG)
            R.id.download -> checkForDownload()
            R.id.apply -> showApplyDialog()
            R.id.favorites -> toggleFavorite(wallpaper)
        }
        return false
    }

    private fun dismissApplierDialog() { try { applierDialog?.dismiss() } catch (_: Exception) {}; applierDialog = null }
    private fun dismissDownloadBlockedDialog() { try { downloadBlockedDialog?.dismiss() } catch (_: Exception) {}; downloadBlockedDialog = null }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(WALLPAPER_URL_KEY, wallpaper?.url ?: wallpaperDownloadUrl)
        outState.putBoolean(FAVORITES_MODIFIED, favoritesModified)
        outState.putBoolean(IS_IN_FAVORITES_KEY, isInFavorites)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        favoritesModified = savedInstanceState.getBoolean(FAVORITES_MODIFIED, false)
        isInFavorites = savedInstanceState.getBoolean(IS_IN_FAVORITES_KEY, false)
    }

    override fun finish() {
        setResult(if (favoritesModified) FAVORITES_MODIFIED_RESULT else FAVORITES_NOT_MODIFIED_RESULT, Intent().putExtra(FAVORITES_MODIFIED, favoritesModified))
        super.finish()
    }

    override fun onDestroy() {
        dismissApplierDialog(); dismissDownloadBlockedDialog(); super.onDestroy()
    }

    private fun shouldShowWallpapersPalette(): Boolean = boolean(R.bool.show_wallpaper_palette_details, true)
    open fun shouldShowDownloadOption() = true
    override fun shouldLoadCollections(): Boolean = false
    override val shouldChangeStatusBarLightStatus: Boolean = false
    override val shouldChangeNavigationBarLightStatus: Boolean = false
    override fun canToggleSystemUIVisibility(): Boolean = intent?.getBooleanExtra(CAN_TOGGLE_SYSTEMUI_VISIBILITY_KEY, true) ?: true

    companion object {
        internal const val MIN_TIME: Long = 3L * 60L * 60000L
        internal const val FAVORITES_MODIFIED = "favorites_modified"
        internal const val FAVORITES_MODIFIED_RESULT = 1
        internal const val FAVORITES_NOT_MODIFIED_RESULT = 0
        internal const val CAN_TOGGLE_SYSTEMUI_VISIBILITY_KEY = "can_toggle_visibility"
        internal const val SHARED_IMAGE_NAME = "thumb.jpg"
        internal const val TRANSITION_NAME = "wallpaper_transition_container"
        internal const val IS_FOR_FAVS = "viewer_is_for_favs"
        private const val DETAILS_TAG = "DETAILS_FRAG"
        private const val IS_IN_FAVORITES_KEY = "is_in_favorites"
    }
}
