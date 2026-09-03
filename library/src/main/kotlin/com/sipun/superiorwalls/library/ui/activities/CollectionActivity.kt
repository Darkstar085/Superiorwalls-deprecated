package com.sipun.superiorwalls.library.ui.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sipun.superiorwalls.library.data.Preferences
import com.sipun.superiorwalls.library.data.models.Collection
import com.sipun.superiorwalls.library.data.models.Wallpaper
import com.sipun.superiorwalls.library.extensions.context.string
import com.sipun.superiorwalls.library.extensions.resources.hasContent
import com.sipun.superiorwalls.library.extensions.utils.parcelable
import com.sipun.superiorwalls.library.ui.activities.base.BaseChangelogDialogActivity
import com.sipun.superiorwalls.library.ui.compose.CollectionScreen
import com.sipun.superiorwalls.library.ui.compose.SuperiorwallsTheme

open class CollectionActivity : BaseChangelogDialogActivity<Preferences>() {

    override val preferences: Preferences by lazy { Preferences(this) }

    private var collection: Collection? = null
    private var collectionName: String = ""
    private var favoritesModified: Boolean = false
    private var wallpapers by mutableStateOf<List<Wallpaper>>(emptyList())
    private var searchQuery by mutableStateOf("")
    private var searchOpen by mutableStateOf(false)
    private lateinit var openViewerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        collection = intent?.extras?.parcelable(COLLECTION_KEY)
        collectionName = intent?.extras?.getString(COLLECTION_NAME_KEY, "") ?: ""
        if (!collectionName.hasContent()) {
            finish()
            return
        }
        savedInstanceState?.getString(COLLECTION_NAME_KEY)?.let { collectionName = it }

        openViewerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == ViewerActivity.FAVORITES_MODIFIED_RESULT) {
                favoritesModified = true
                loadWallpapersData(true)
            }
        }

        wallpapersViewModel.observeCollections(this) { collections ->
            val rightCollection = collections.firstOrNull {
                it.name == (collection?.name ?: collectionName)
            }
            wallpapers = rightCollection?.wallpapers.orEmpty()
        }
        loadWallpapersData()

        setContent {
            val themeKey = preferences.currentTheme.value
            val darkTheme = when (themeKey) {
                Preferences.ThemeKey.DARK.value -> true
                Preferences.ThemeKey.LIGHT.value -> false
                else -> resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            }
            val filteredWallpapers = if (searchQuery.isBlank()) {
                wallpapers
            } else {
                wallpapers.filter {
                    it.name.contains(searchQuery, true) ||
                        it.collections.orEmpty().contains(searchQuery, true) ||
                        it.author.orEmpty().contains(searchQuery, true)
                }
            }

            SuperiorwallsTheme(
                darkTheme = darkTheme,
                dynamicColor = preferences.useMaterialYou,
                amoled = preferences.usesAmoledTheme,
            ) {
                CollectionScreen(
                    title = collection?.displayName ?: collectionName,
                    wallpapers = filteredWallpapers,
                    canModifyFavorites = canModifyFavorites(),
                    searchQuery = searchQuery,
                    searchOpen = searchOpen,
                    onSearchQueryChange = { searchQuery = it },
                    onSearchOpen = { searchOpen = true },
                    onSearchClose = {
                        searchQuery = ""
                        searchOpen = false
                    },
                    onBack = { supportFinishAfterTransition() },
                    onWallpaperClick = ::openViewer,
                    onFavoriteClick = ::toggleFavorite,
                )
            }
        }
    }

    override fun getSearchHint(itemId: Int): String = string(com.sipun.superiorwalls.library.R.string.search_wallpapers)

    private fun openViewer(wallpaper: Wallpaper) {
        openViewerLauncher.launch(
            Intent(this, ViewerActivity::class.java).apply {
                putExtra(ViewerActivity.CAN_TOGGLE_SYSTEMUI_VISIBILITY_KEY, true)
                putExtra(WALLPAPER_EXTRA, wallpaper)
                putExtra(WALLPAPER_IN_FAVS_EXTRA, wallpaper.isInFavorites)
                putExtra(COLLECTION_NAME_KEY, collectionName)
                putExtra(ViewerActivity.IS_FOR_FAVS, false)
            }
        )
    }

    private fun toggleFavorite(wallpaper: Wallpaper, checked: Boolean) {
        val changed = if (checked) addToFavorites(wallpaper) else removeFromFavorites(wallpaper)
        if (changed) {
            wallpaper.isInFavorites = checked
            wallpapers = wallpapers.map { if (it.url == wallpaper.url) wallpaper else it }
            favoritesModified = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(COLLECTION_NAME_KEY, collectionName)
        super.onSaveInstanceState(outState)
    }

    override fun finish() {
        setResult(
            if (favoritesModified) ViewerActivity.FAVORITES_MODIFIED_RESULT
            else ViewerActivity.FAVORITES_NOT_MODIFIED_RESULT,
            Intent().apply {
                putExtra(ViewerActivity.FAVORITES_MODIFIED, favoritesModified)
            },
        )
        super.finish()
    }

    internal fun setFavoritesModified() {
        favoritesModified = true
    }

    companion object {
        internal const val REQUEST_CODE = 11
        internal const val COLLECTION_KEY = "collection"
        internal const val COLLECTION_NAME_KEY = "collection_name"
        private const val WALLPAPER_EXTRA = "wallpaper"
        private const val WALLPAPER_IN_FAVS_EXTRA = "wallpaper_in_favs"
    }
}
