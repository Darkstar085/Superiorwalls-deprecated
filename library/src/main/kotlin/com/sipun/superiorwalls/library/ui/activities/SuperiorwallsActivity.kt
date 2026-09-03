package com.sipun.superiorwalls.library.ui.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sipun.superiorwalls.library.R
import com.sipun.superiorwalls.library.data.Preferences
import com.sipun.superiorwalls.library.data.models.Collection
import com.sipun.superiorwalls.library.data.models.Wallpaper
import com.sipun.superiorwalls.library.data.viewmodels.WallpapersDataViewModel
import com.sipun.superiorwalls.library.ui.activities.base.BaseFavoritesConnectedActivity
import com.sipun.superiorwalls.library.ui.compose.SuperiorwallsHome
import com.sipun.superiorwalls.library.ui.fragments.buildChangelogEntries
import com.sipun.superiorwalls.library.ui.fragments.ChangelogType

@Suppress("LeakingThis", "MemberVisibilityCanBePrivate")
abstract class SuperiorwallsActivity : BaseFavoritesConnectedActivity<Preferences>() {

    override val preferences: Preferences by lazy { Preferences(this) }

    private var wallpaperItems by mutableStateOf<List<Wallpaper>>(emptyList())
    private var collectionItems by mutableStateOf<List<Collection>>(emptyList())
    private var favoriteItems by mutableStateOf<List<Wallpaper>>(emptyList())
    private var showChangelog by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val dark = when (preferences.currentTheme) {
                Preferences.ThemeKey.DARK -> true
                Preferences.ThemeKey.LIGHT -> false
                Preferences.ThemeKey.FOLLOW_SYSTEM ->
                    (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            }
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
                onChangelogClick = { showChangelog = true },
            )
            if (showChangelog) {
                val entries = buildChangelogEntries()
                AlertDialog(
                    onDismissRequest = { showChangelog = false },
                    title = { Text(getString(R.string.changelog)) },
                    text = {
                        LazyColumn {
                            itemsIndexed(entries) { _, entry ->
                                Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                    Text(
                                        entry.text,
                                        style = if (entry.type == ChangelogType.TITLE) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                                        color = if (entry.type == ChangelogType.TITLE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showChangelog = false }) { Text(getString(android.R.string.ok)) }
                    },
                )
            }
        }

        wallpapersViewModel.observeWallpapers(this) { wallpaperItems = it }
        wallpapersViewModel.observeCollections(this) { collectionItems = it }
        wallpapersViewModel.observeFavorites(this) { favoriteItems = it }
        wallpapersViewModel.errorListener = ::showDataErrorToastIfNeeded
        loadWallpapersData(true)
        requestNotificationsPermission()
    }

    private fun openWallpaper(wallpaper: Wallpaper) {
        startActivity(Intent(this, ViewerActivity::class.java).apply {
            putExtra(ViewerActivity.WALLPAPER_EXTRA, wallpaper)
            putExtra(ViewerActivity.WALLPAPER_IN_FAVS_EXTRA, wallpaper.isInFavorites)
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
        if (!canModifyFavorites()) {
            onFavoritesLocked()
            return
        }
        val changed = if (checked) addToFavorites(wallpaper) else removeFromFavorites(wallpaper)
        if (changed) wallpaper.isInFavorites = checked
    }

    override fun onFavoritesUpdated(favorites: List<Wallpaper>) {
        favoriteItems = favorites
    }

    override val snackbarAnchorId: Int = android.R.id.content

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
