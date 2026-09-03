package com.sipun.superiorwalls.library.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.sipun.superiorwalls.library.R
import com.sipun.superiorwalls.library.data.models.Wallpaper
import com.sipun.superiorwalls.library.extensions.fragments.preferences
import com.sipun.superiorwalls.library.extensions.resources.lower
import com.sipun.superiorwalls.library.ui.activities.CollectionActivity
import com.sipun.superiorwalls.library.ui.activities.ViewerActivity
import com.sipun.superiorwalls.library.ui.activities.base.BaseFavoritesConnectedActivity
import com.sipun.superiorwalls.library.ui.compose.WallpaperBrowser
import com.sipun.superiorwalls.library.ui.fragments.base.BaseWallpaperFragment

open class WallpapersFragment : BaseWallpaperFragment<Wallpaper>() {

    var isForFavs: Boolean = false
    open var canShowFavoritesButton: Boolean = true
    private var openActivityLauncher: ActivityResultLauncher<Intent?>? = null
    private var collectionName: String? = null
    private val composeItems = mutableStateListOf<Wallpaper>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == ViewerActivity.FAVORITES_MODIFIED_RESULT) {
                    (activity as? CollectionActivity)?.setFavoritesModified()
                    (activity as? BaseFavoritesConnectedActivity<*>)?.loadWallpapersData(true)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val root = view as? ViewGroup ?: return
        root.removeAllViews()
        root.addView(
            ComposeView(requireContext()).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    MaterialTheme {
                        WallpaperBrowser(
                            wallpapers = composeItems,
                            canModifyFavorites =
                                canShowFavoritesButton &&
                                    ((activity as? BaseFavoritesConnectedActivity<*>)?.canModifyFavorites() ?: true),
                            onWallpaperClick = ::launchViewer,
                            onFavoriteClick = ::onFavClick,
                        )
                    }
                }
            },
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        (activity as? BaseFavoritesConnectedActivity<*>)?.loadWallpapersData(
            triggerErrorListener = !isForFavs
        )
    }

    override fun loadData() {
        (activity as? BaseFavoritesConnectedActivity<*>)?.loadWallpapersData(true)
    }

    override fun updateItemsInAdapter(items: List<Wallpaper>) {
        composeItems.clear()
        composeItems.addAll(items)
    }

    override fun getFilteredItems(
        originalItems: ArrayList<Wallpaper>,
        filter: String
    ): ArrayList<Wallpaper> =
        ArrayList(originalItems.filter {
            it.name.lower().contains(filter.lower()) ||
                it.collections.orEmpty().lower().contains(filter.lower()) ||
                it.author.lower().contains(filter.lower())
        })

    private fun onFavClick(checked: Boolean, wallpaper: Wallpaper) {
        var updated = false
        (activity as? BaseFavoritesConnectedActivity<*>)?.let {
            if (it.canModifyFavorites()) {
                updated = if (checked) it.addToFavorites(wallpaper) else it.removeFromFavorites(wallpaper)
            } else {
                it.onFavoritesLocked()
            }
        }
        if (updated) {
            wallpaper.isInFavorites = checked
            (activity as? CollectionActivity)?.setFavoritesModified()
            val index = composeItems.indexOfFirst { it.url == wallpaper.url }
            if (index >= 0) composeItems[index] = wallpaper
        }
    }

    private fun launchViewer(wallpaper: Wallpaper) {
        val intent = getTargetActivityIntent().apply {
            putExtra(ViewerActivity.CAN_TOGGLE_SYSTEMUI_VISIBILITY_KEY, canToggleSystemUIVisibility())
            putExtra(WALLPAPER_EXTRA, wallpaper)
            putExtra(WALLPAPER_IN_FAVS_EXTRA, wallpaper.isInFavorites)
            putExtra(CollectionActivity.COLLECTION_NAME_KEY, collectionName)
            putExtra(ViewerActivity.IS_FOR_FAVS, isForFavs)
        }
        openActivityLauncher?.launch(intent)
    }

    fun setCollectionName(newCollectionName: String?) {
        collectionName = newCollectionName
    }

    override fun getTargetActivityIntent(): Intent = Intent(activity, ViewerActivity::class.java)

    open fun notifyCanModifyFavorites(canModify: Boolean = true) {
        canShowFavoritesButton = canModify
        composeItems.replaceAll { it }
    }

    override fun getEmptyText(): Int =
        if (isForFavs) R.string.no_favorites_found else R.string.no_wallpapers_found

    override fun getEmptyDrawable(): Int =
        if (isForFavs) R.drawable.ic_empty_favorites else super.getEmptyDrawable()

    open fun canToggleSystemUIVisibility(): Boolean = true
    override fun allowCheckingFirstRun(): Boolean = true

    companion object {
        const val TAG = "wallpapers"
        const val FAVS_TAG = "favorites"
        const val WALLPAPER_EXTRA = "wallpaper"
        const val WALLPAPER_IN_FAVS_EXTRA = "wallpaper_in_favs"

        fun create(
            wallpapers: ArrayList<Wallpaper> = ArrayList(),
            canModifyFavorites: Boolean = true,
        ) = WallpapersFragment().apply {
            this.canShowFavoritesButton = canModifyFavorites
            this.isForFavs = false
            this.composeItems.addAll(wallpapers)
        }

        fun createForFavs(
            wallpapers: ArrayList<Wallpaper> = ArrayList(),
            canModifyFavorites: Boolean = true,
        ) = WallpapersFragment().apply {
            this.canShowFavoritesButton = canModifyFavorites
            this.isForFavs = true
            this.composeItems.addAll(wallpapers)
        }
    }
}
