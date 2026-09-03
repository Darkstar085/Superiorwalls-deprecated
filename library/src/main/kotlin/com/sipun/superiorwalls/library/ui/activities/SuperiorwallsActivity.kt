package com.sipun.superiorwalls.library.ui.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.sipun.superiorwalls.library.R
import com.sipun.superiorwalls.library.data.Preferences
import com.sipun.superiorwalls.library.data.models.Collection
import com.sipun.superiorwalls.library.data.models.Wallpaper
import com.sipun.superiorwalls.library.data.viewmodels.WallpapersDataViewModel
import com.sipun.superiorwalls.library.ui.activities.base.BaseChangelogDialogActivity
import com.sipun.superiorwalls.library.ui.compose.SuperiorwallsHome
import com.sipun.superiorwalls.library.ui.fragments.WallpapersFragment

@Suppress("LeakingThis", "MemberVisibilityCanBePrivate")
abstract class SuperiorwallsActivity : BaseChangelogDialogActivity<Preferences>() {

    override val preferences: Preferences by lazy { Preferences(this) }

    private var composeView: ComposeView? = null
    private var wallpaperItems: List<Wallpaper> = emptyList()
    private var collectionItems: List<Collection> = emptyList()
    private var favoriteItems: List<Wallpaper> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragments_bottom_navigation)
        findViewById<View>(R.id.toolbar)?.visibility = View.GONE
        findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
        val container = findViewById<ViewGroup>(R.id.fragments_container)
        composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        }
        container.addView(composeView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        wallpapersViewModel.observeWallpapers(this) { wallpaperItems = it; renderHome() }
        wallpapersViewModel.observeCollections(this) { collectionItems = it; renderHome() }
        wallpapersViewModel.observeFavorites(this) { favoriteItems = it; renderHome() }
        wallpapersViewModel.errorListener = ::showDataErrorToastIfNeeded
        renderHome()
        loadWallpapersData(true)
        requestNotificationsPermission()
    }

    private fun renderHome() {
        val dark = when (preferences.currentTheme) {
            Preferences.ThemeKey.DARK -> true
            Preferences.ThemeKey.LIGHT -> false
            Preferences.ThemeKey.FOLLOW_SYSTEM -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }
        composeView?.setContent {
            SuperiorwallsHome(
                wallpapers = wallpaperItems,
                collections = collectionItems,
                favorites = favoriteItems,
                canModifyFavorites = canModifyFavorites(),
                useDarkTheme = dark,
                useDynamicColor = preferences.useMaterialYou,
                useAmoled = preferences.usesAmoledTheme,
                onWallpaperClick = ::openWallpaper,
                onFavoriteClick = ::toggleFavorite,
                onCollectionClick = ::openCollection,
                onSettingsClick = { startActivity(Intent(this, SettingsActivity::class.java)) },
            )
        }
    }

    private fun openWallpaper(wallpaper: Wallpaper) {
        startActivity(Intent(this, ViewerActivity::class.java).apply {
            putExtra(WallpapersFragment.WALLPAPER_EXTRA, wallpaper)
            putExtra(WallpapersFragment.WALLPAPER_IN_FAVS_EXTRA, wallpaper.isInFavorites)
            putExtra(ViewerActivity.CAN_TOGGLE_SYSTEMUI_VISIBILITY_KEY, true)
            putExtra(ViewerActivity.IS_FOR_FAVS, false)
        })
    }

    private fun openCollection(collection: Collection) {
        startActivity(Intent(this, CollectionActivity::class.java).apply {
            putExtra(CollectionActivity.COLLECTION_KEY, collection)
            putExtra(CollectionActivity.COLLECTION_NAME_KEY, collection.name)
        })
    }

    private fun toggleFavorite(wallpaper: Wallpaper, checked: Boolean) {
        val changed = if (checked) addToFavorites(wallpaper) else removeFromFavorites(wallpaper)
        if (changed) { wallpaper.isInFavorites = checked; renderHome() }
    }

    override fun onFavoritesUpdated(favorites: List<Wallpaper>) {
        favoriteItems = favorites
        renderHome()
    }

    override fun onDestroy() {
        composeView = null
        super.onDestroy()
    }

    private fun showDataErrorToastIfNeeded(error: WallpapersDataViewModel.DataError) {
        val message = when (error) {
            WallpapersDataViewModel.DataError.None -> null
            WallpapersDataViewModel.DataError.Unknown -> R.string.unexpected_error_occurred
            WallpapersDataViewModel.DataError.MalformedJson -> R.string.data_error_format
            WallpapersDataViewModel.DataError.NoNetwork -> R.string.data_error_network
        }
        message?.let { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
    }
}
