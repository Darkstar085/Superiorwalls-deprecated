@file:Suppress("DEPRECATION")

package com.sipun.superiorwalls.library.ui.activities

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.GestureDetector
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import coil.dispose
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.ortiz.touchview.TouchImageView
import com.sipun.superiorwalls.library.R
import com.sipun.superiorwalls.library.data.Preferences
import com.sipun.superiorwalls.library.data.models.Wallpaper
import com.sipun.superiorwalls.library.extensions.context.boolean
import com.sipun.superiorwalls.library.extensions.context.color
import com.sipun.superiorwalls.library.extensions.context.compliesWithMinTime
import com.sipun.superiorwalls.library.extensions.context.findView
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
import com.sipun.superiorwalls.library.extensions.resources.hasContent
import com.sipun.superiorwalls.library.extensions.resources.toReadableTime
import com.sipun.superiorwalls.library.extensions.utils.MAX_PALETTE_COLORS
import com.sipun.superiorwalls.library.extensions.utils.bestSwatch
import com.sipun.superiorwalls.library.extensions.views.gone
import com.sipun.superiorwalls.library.extensions.views.loadWallpaperPic
import com.sipun.superiorwalls.library.extensions.views.setPaddingBottom
import com.sipun.superiorwalls.library.extensions.views.setPaddingLeft
import com.sipun.superiorwalls.library.extensions.views.setPaddingRight
import com.sipun.superiorwalls.library.extensions.views.setPaddingTop
import com.sipun.superiorwalls.library.extensions.views.tint
import com.sipun.superiorwalls.library.extensions.views.visible
import com.sipun.superiorwalls.library.extensions.views.visibleIf
import com.sipun.superiorwalls.library.ui.activities.base.BaseWallpaperApplierActivity
import com.sipun.superiorwalls.library.ui.fragments.WallpapersFragment.Companion.WALLPAPER_EXTRA
import com.sipun.superiorwalls.library.ui.fragments.viewer.DetailsFragment
import com.sipun.superiorwalls.library.ui.fragments.viewer.SetAsOptionsDialog
import kotlinx.coroutines.launch

open class ViewerActivity : BaseWallpaperApplierActivity<Preferences>() {

    override val preferences: Preferences by lazy { Preferences(this) }

    private val toolbar: Toolbar? by findView(R.id.toolbar)
    private val imageView: TouchImageView? by findView(R.id.wallpaper)

    private var firstImageLoad: Boolean = true
    private var transitioned: Boolean = false
    private var closing: Boolean = false
    private var favoritesModified: Boolean = false
    private var isInFavorites: Boolean = false
        set(value) {
            field = value
            bottomNavigation?.setSelectedItemId(
                if (value) R.id.favorites else R.id.details,
                false
            )
        }
    private var collectionName: String? = null
    private var isForFavs: Boolean = false

    private val detailsFragment: DetailsFragment by lazy {
        DetailsFragment.create(shouldShowPaletteDetails = shouldShowWallpapersPalette())
    }

    private var downloadBlockedDialog: AlertDialog? = null
    private var applierDialog: DialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.decorView.setBackgroundColor(0)
        findViewById<View>(android.R.id.content).transitionName = TRANSITION_NAME
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = ENTER_TRANSITION_DURATION
        }

        window.sharedElementReturnTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = RETURN_TRANSITION_DURATION
        }

        super.onCreate(savedInstanceState)
        statusBarLight = false
        navigationBarLight = false
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        );
        setContentView(R.layout.activity_viewer)
        bottomNavigation?.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED

        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }
        initWindow()
        toolbar?.tint(color(R.color.white))

        imageView?.setOnDoubleTapListener(object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                toggleSystemUI()
                return super.onSingleTapConfirmed(e)
            }
        })

        wallpapersViewModel.observeFavorites(this) {
            this.isInFavorites = it.any { wall -> wall.url == wallpaperDownloadUrl }
        }

        // WALLPAPER SPECIFIC RELATED SETUP ↓
        collectionName = intent?.extras?.getString(CollectionActivity.COLLECTION_NAME_KEY)
        isForFavs = intent?.extras?.getBoolean(IS_FOR_FAVS, false) ?: false

        val lastWallpaper = savedInstanceState?.getString(WALLPAPER_URL_KEY)
        val wallpaperFromIntent = intent?.extras?.getParcelable<Wallpaper?>(WALLPAPER_EXTRA)?.url

        lifecycleScope.launch {
            configureUIForWallpaper(
                wallpapersViewModel.findWallpaper(
                    lastWallpaper ?: wallpaperFromIntent
                )
            )
        }
    }

    private fun configureUIForWallpaper(wallpaper: Wallpaper?) {
        if (wallpaper == null) {
            finish()
            return
        }

        bottomNavigation?.setItemVisible(
            R.id.download,
            !(wallpaper.downloadable == false || !shouldShowDownloadOption())
        )

        findViewById<View?>(R.id.toolbar_title)?.let {
            (it as? TextView)?.text = wallpaper.name
        }
        findViewById<View?>(R.id.toolbar_subtitle)?.let {
            (it as? TextView)?.text = wallpaper.author
            it.visibleIf(wallpaper.author.hasContent())
        }

        initWallpaperFetcher(wallpaper)
        detailsFragment.wallpaper = wallpaper
        loadWallpaper(wallpaper)

        isInFavorites = wallpaper.isInFavorites
        loadWallpapersData()

        bottomNavigation?.setOnNavigationItemSelectedListener {
            handleNavigationItemSelected(it.itemId, wallpaper)
        }

        findViewById<AppCompatImageButton>(R.id.go_previous)?.setOnClickListener {
            lifecycleScope.launch {
                val previousWallpaper = if (isForFavs) {
                    wallpapersViewModel.getPreviousFavoriteWallpaper(wallpaper.url)
                } else {
                    wallpapersViewModel.getPreviousWallpaper(wallpaper.url, collectionName)
                }
                configureUIForWallpaper(previousWallpaper)
            }
        }

        findViewById<AppCompatImageButton>(R.id.go_next)?.setOnClickListener {
            lifecycleScope.launch {
                val nextWallpaper = if (isForFavs) {
                    wallpapersViewModel.getNextFavoriteWallpaper(wallpaper.url)
                } else {
                    wallpapersViewModel.getNextWallpaper(wallpaper.url, collectionName)
                }
                configureUIForWallpaper(nextWallpaper)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(CLOSING_KEY, closing)
        outState.putBoolean(TRANSITIONED_KEY, transitioned)
        outState.putBoolean(IS_IN_FAVORITES_KEY, isInFavorites)
        outState.putBoolean(FAVORITES_MODIFIED, favoritesModified)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        this.closing = savedInstanceState.getBoolean(CLOSING_KEY, false)
        this.transitioned = savedInstanceState.getBoolean(TRANSITIONED_KEY, false)
        this.isInFavorites = savedInstanceState.getBoolean(IS_IN_FAVORITES_KEY, false)
        this.favoritesModified = savedInstanceState.getBoolean(FAVORITES_MODIFIED, false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) supportFinishAfterTransition()
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        imageView?.setZoom(1F)
        setResult(
            if (favoritesModified) FAVORITES_MODIFIED_RESULT
            else FAVORITES_NOT_MODIFIED_RESULT,
            Intent().apply {
                putExtra(FAVORITES_MODIFIED, favoritesModified)
            }
        )
        super.finish()
    }

    private fun dismissApplierDialog() {
        try {
            applierDialog?.dismiss()
        } catch (_: Exception) {
        }
        applierDialog = null
    }

    private fun dismissDownloadBlockedDialog() {
        try {
            downloadBlockedDialog?.dismiss()
        } catch (_: Exception) {
        }
        downloadBlockedDialog = null
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissApplierDialog()
        dismissDownloadBlockedDialog()
    }

    private fun generatePalette(drawable: Drawable? = null) {
        findViewById<View?>(R.id.loading)?.gone()
        if (!shouldShowWallpapersPalette()) {
            setBackgroundColor()
            return
        }
        (drawable ?: imageView?.drawable)?.asBitmap()?.let { bitmap ->
            Palette.from(bitmap)
                .maximumColorCount(MAX_PALETTE_COLORS * 2)
                .generate {
                    setBackgroundColor(it?.bestSwatch?.rgb ?: 0)
                    detailsFragment.palette = it
                }
        } ?: run {
            setBackgroundColor()
        }
    }

    private fun setBackgroundColor(@ColorInt color: Int? = null) {
        findViewById<View?>(R.id.activity_root_view)?.setBackgroundColor(
            color ?: resolveColor(android.R.attr.colorBackground)
        )
    }

    private fun loadWallpaper(wallpaper: Wallpaper) {
        findViewById<View?>(R.id.loading)?.visible()
        var placeholder: Drawable? = null
        val wallpaperFromIntent = intent?.extras?.getParcelable<Wallpaper?>(WALLPAPER_EXTRA)?.url
        try {
            if (wallpaperFromIntent == wallpaper.url) {
                openFileInput(SHARED_IMAGE_NAME)?.use {
                    placeholder = BitmapDrawable(resources, it)
                }
            } else {
                imageView?.dispose()
                placeholder = Color.TRANSPARENT.toDrawable()
                firstImageLoad = true
                setBackgroundColor()
            }
        } catch (_: Exception) {
        }
        imageView?.loadWallpaperPic(
            wallpaper.url,
            wallpaper.thumbnail,
            placeholder,
            forceLoadFullRes = true,
            cropAsCircle = false,
            saturate = false
        ) { w ->
            if (firstImageLoad) {
                firstImageLoad = false
                imageView?.resetZoomAnimated()
            }
            generatePalette(w)
        }
    }

    private fun initWindow() {
        window.decorView.systemUiVisibility =
            SYSTEM_UI_FLAG_LAYOUT_STABLE or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        val params: WindowManager.LayoutParams = window.attributes
        params.flags = params.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS.inv()
        window.attributes = params

        appbar?.let { appbar ->
            ViewCompat.setOnApplyWindowInsetsListener(appbar) { _, insets ->
                appbar.setPaddingTop(insets.systemWindowInsetTop)
                appbar.setPaddingLeft(
                    if (boolean(R.bool.is_landscape)) insets.systemWindowInsetLeft
                    else 0
                )
                appbar.setPaddingRight(
                    if (boolean(R.bool.is_landscape)) insets.systemWindowInsetRight
                    else 0
                )
                insets
            }
        }

        bottomNavigation?.let { bottomNavigation ->
            ViewCompat.setOnApplyWindowInsetsListener(bottomNavigation) { _, insets ->
                bottomNavigation.setPaddingBottom(insets.systemWindowInsetBottom)
                insets
            }
        }

        window.statusBarColor = color(R.color.viewer_bars_colors)
        window.navigationBarColor = color(R.color.viewer_bars_colors)
    }

    open fun handleNavigationItemSelected(itemId: Int, wallpaper: Wallpaper?): Boolean {
        wallpaper ?: return false
        when (itemId) {
            R.id.details -> detailsFragment.show(this, "DETAILS_FRAG")
            R.id.download -> checkForDownload()
            R.id.apply -> applyWallpaper(wallpaper)
            R.id.favorites -> {
                if (canModifyFavorites()) {
                    this.favoritesModified = true
                    if (isInFavorites) removeFromFavorites(wallpaper)
                    else addToFavorites(wallpaper)
                } else onFavoritesLocked()
            }
        }
        return false
    }

    private fun hasValidNetworkAvailable(): Boolean {
        val downloadUsingWiFiOnly = preferences.shouldDownloadOnWiFiOnly
        val isConnected = isNetworkAvailable()
        val usingMobileData = (downloadUsingWiFiOnly && !isWifiConnected) && isConnected
        val shouldShowNetworkDialog = !isConnected || usingMobileData
        if (shouldShowNetworkDialog) {
            dismissDownloadBlockedDialog()
            downloadBlockedDialog = mdDialog {
                title(R.string.error)
                message(
                    if (usingMobileData) R.string.data_error_network_wifi_only
                    else R.string.data_error_network
                )
                positiveButton(android.R.string.ok) { it.dismiss() }
            }
            downloadBlockedDialog?.show()
            return false
        }
        return true
    }

    private fun checkForDownload() {
        if (!shouldShowDownloadOption()) return
        val actuallyComplies =
            boolean(R.bool.allow_immediate_downloads) ||
                System.currentTimeMillis() - firstInstallTime >= MIN_TIME
        if (actuallyComplies) {
            if (!hasValidNetworkAvailable()) return
            requestStoragePermission()
        } else {
            val elapsedTime = System.currentTimeMillis() - firstInstallTime
            val timeLeft = MIN_TIME - elapsedTime
            val timeLeftText = timeLeft.toReadableTime()

            dismissDownloadBlockedDialog()
            downloadBlockedDialog = mdDialog {
                title(R.string.prevent_download_title)
                message(string(R.string.prevent_download_content, timeLeftText))
                positiveButton(android.R.string.ok) { it.dismiss() }
            }
            downloadBlockedDialog?.show()
        }
    }

    override fun internalOnPermissionsGranted(permission: String) {
        super.internalOnPermissionsGranted(permission)
        if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE)
            startDownload()
    }

    private fun applyWallpaper(wallpaper: Wallpaper?) {
        wallpaper ?: return
        dismissApplierDialog()
        applierDialog = SetAsOptionsDialog()
        applierDialog?.show(supportFragmentManager, SetAsOptionsDialog.TAG)
    }

    private fun shouldShowWallpapersPalette(): Boolean =
        boolean(R.bool.show_wallpaper_palette_details, true)

    open fun shouldShowDownloadOption() = true
    override fun shouldLoadCollections(): Boolean = false
    override val shouldChangeStatusBarLightStatus: Boolean = false
    override val shouldChangeNavigationBarLightStatus: Boolean = false

    override fun canToggleSystemUIVisibility(): Boolean =
        intent?.getBooleanExtra(CAN_TOGGLE_SYSTEMUI_VISIBILITY_KEY, true) ?: true

    companion object {
        internal const val MIN_TIME: Long = 3L * 60L * 60000L
        internal const val FAVORITES_MODIFIED = "favorites_modified"
        internal const val FAVORITES_MODIFIED_RESULT = 1
        internal const val FAVORITES_NOT_MODIFIED_RESULT = 0
        internal const val CAN_TOGGLE_SYSTEMUI_VISIBILITY_KEY = "can_toggle_visibility"
        internal const val SHARED_IMAGE_NAME = "thumb.jpg"
        internal const val TRANSITION_NAME = "wallpaper_transition_container"
        internal const val IS_FOR_FAVS = "viewer_is_for_favs"
        private const val ENTER_TRANSITION_DURATION = 300L
        private const val RETURN_TRANSITION_DURATION = 250L
        private const val CLOSING_KEY = "closing"
        private const val TRANSITIONED_KEY = "transitioned"
        private const val IS_IN_FAVORITES_KEY = "is_in_favorites"
    }
}
