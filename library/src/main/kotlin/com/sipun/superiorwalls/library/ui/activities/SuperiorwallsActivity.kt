package com.sipun.superiorwalls.library.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.sipun.superiorwalls.library.R
import com.sipun.superiorwalls.library.data.Preferences
import com.sipun.superiorwalls.library.data.models.Collection
import com.sipun.superiorwalls.library.data.models.Wallpaper
import com.sipun.superiorwalls.library.data.viewmodels.WallpapersDataViewModel
import com.sipun.superiorwalls.library.extensions.context.drawable
import com.sipun.superiorwalls.library.extensions.context.getAppName
import com.sipun.superiorwalls.library.extensions.context.string
import com.sipun.superiorwalls.library.extensions.context.toast
import com.sipun.superiorwalls.library.extensions.resources.hasContent
import com.sipun.superiorwalls.library.ui.activities.base.BaseBillingActivity
import com.sipun.superiorwalls.library.ui.fragments.CollectionsFragment
import com.sipun.superiorwalls.library.ui.fragments.WallpapersFragment
import com.sipun.superiorwalls.library.ui.fragments.base.BaseWallpaperFragment

@Suppress("LeakingThis", "MemberVisibilityCanBePrivate")
abstract class SuperiorwallsActivity : BaseBillingActivity<Preferences>() {

    override val preferences: Preferences by lazy { Preferences(this) }

    open val wallpapersFragment: WallpapersFragment? by lazy {
        WallpapersFragment.create(ArrayList(wallpapersViewModel.wallpapers), canModifyFavorites())
    }
    open val collectionsFragment: CollectionsFragment? by lazy {
        CollectionsFragment.create(ArrayList(wallpapersViewModel.collections))
    }
    open val favoritesFragment: WallpapersFragment? by lazy {
        WallpapersFragment.createForFavs(
            ArrayList(wallpapersViewModel.favorites),
            canModifyFavorites()
        )
    }

    var currentFragment: Fragment? = null
        private set

    open val initialFragmentTag: String = WallpapersFragment.TAG

    private var currentTag: String = initialFragmentTag
    private var oldTag: String = initialFragmentTag

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = this@SuperiorwallsActivity.handleOnBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        super.onCreate(savedInstanceState)
        setContentView(getLayoutRes())
        onBackPressedDispatcher.addCallback(onBackPressedCallback)

        setSupportActionBar(toolbar)
        changeFragment(initialItemId, force = true)

        bottomNavigation?.selectedItemId = initialItemId
        bottomNavigation?.setOnItemSelectedListener {
            val fragmentChanged = changeFragment(it.itemId)
            enableOnBackPressedCallback(fragmentChanged && isBackPressedCallbackEnabled())
            fragmentChanged
        }

        wallpapersViewModel.observeWallpapers(this, ::handleWallpapersUpdate)
        wallpapersViewModel.observeCollections(this, ::handleCollectionsUpdate)
        wallpapersViewModel.errorListener = ::showDataErrorToastIfNeeded
        loadWallpapersData(true)

        requestNotificationsPermission()
    }

    fun enableOnBackPressedCallback(enabled: Boolean = isBackPressedCallbackEnabled()) {
        onBackPressedCallback.isEnabled = enabled
    }

    open fun handleOnBackPressed() {
        if (currentItemId != initialItemId)
            bottomNavigation?.selectedItemId = initialItemId
        else supportFinishAfterTransition()
    }

    open fun isBackPressedCallbackEnabled(): Boolean = currentItemId != initialItemId

    fun updateToolbarTitle(itemId: Int = currentItemId) {
        var logoSet = false
        if (shouldShowToolbarLogo(itemId)) {
            drawable(string(R.string.toolbar_logo))?.let {
                supportActionBar?.setDisplayShowTitleEnabled(false)
                supportActionBar?.setLogo(it)
                logoSet = true
            }
        }
        if (!logoSet) {
            supportActionBar?.setLogo(null)
            supportActionBar?.title = getToolbarTitleForItem(itemId) ?: getAppName()
            supportActionBar?.setDisplayShowTitleEnabled(true)
        }
    }

    open fun shouldShowToolbarLogo(itemId: Int): Boolean = itemId == initialItemId

    @LayoutRes
    open fun getLayoutRes(): Int = R.layout.activity_fragments_bottom_navigation

    override fun onFavoritesUpdated(favorites: List<Wallpaper>) {
        super.onFavoritesUpdated(favorites)
        favoritesFragment?.updateItems(ArrayList(favorites))
    }

    override fun getMenuRes(): Int = R.menu.toolbar_menu

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> startActivity(Intent(this, SettingsActivity::class.java))
            /**R.id.donate -> showDonationsDialog()**/
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getSearchHint(itemId: Int): String = when (itemId) {
        R.id.wallpapers -> string(R.string.search_wallpapers)
        R.id.collections -> string(R.string.search_collections)
        R.id.favorites -> string(R.string.search_favorites)
        else -> string(R.string.search_x)
    }

    open fun getToolbarTitleForItem(itemId: Int): String? =
        when (itemId) {
            R.id.collections -> string(R.string.collections)
            R.id.favorites -> string(R.string.favorites)
            else -> null
        }

    open fun getNextFragment(itemId: Int): Pair<Pair<String?, Fragment?>?, Boolean>? =
        when (itemId) {
            R.id.wallpapers -> Pair(Pair(WallpapersFragment.TAG, wallpapersFragment), true)
            R.id.collections -> Pair(Pair(CollectionsFragment.TAG, collectionsFragment), true)
            R.id.favorites -> Pair(Pair(WallpapersFragment.FAVS_TAG, favoritesFragment), true)
            else -> null
        }

    @Suppress("MemberVisibilityCanBePrivate")
    fun changeFragment(itemId: Int, force: Boolean = false, animate: Boolean = true): Boolean {
        if (currentItemId != itemId || force) {
            val next = getNextFragment(itemId)
            // Pair ( Pair ( fragmentTag, fragment ) , shouldShowItemAsSelected )
            val nextFragmentTag = next?.first?.first.orEmpty()
            if (!nextFragmentTag.hasContent()) return false
            val nextFragment = next?.first?.second
            val shouldSelectItem = next?.second == true
            return nextFragment?.let {
                if (shouldSelectItem) {
                    oldTag = currentTag
                    currentItemId = itemId
                    currentTag = nextFragmentTag
                    loadFragment(nextFragment, currentTag, force, animate)
                    updateToolbarTitle(itemId)
                }
                shouldSelectItem
            } ?: false
        }
        return false
    }

    private fun loadFragment(
        fragment: Fragment?,
        tag: String,
        force: Boolean = false,
        animate: Boolean = true
    ) {
        fragment ?: return
        if (currentFragment !== fragment || force) {
            replaceFragment(fragment, tag, animate = animate)
            currentFragment = fragment
            invalidateOptionsMenu()
            updateSearchHint()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CURRENT_FRAGMENT_KEY, currentTag)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentTag = savedInstanceState.getString(CURRENT_FRAGMENT_KEY, currentTag) ?: currentTag
        changeFragment(currentItemId, true)
        enableOnBackPressedCallback()
    }

    override fun internalDoSearch(filter: String, closed: Boolean) {
        super.internalDoSearch(filter, closed)
        (currentFragment as? BaseWallpaperFragment<*>)?.let {
            it.setRefreshEnabled(!filter.hasContent())
            it.applyFilter(filter, closed)
        }
    }

    private fun showDataErrorToastIfNeeded(error: WallpapersDataViewModel.DataError) {
        val message: Int? = when (error) {
            WallpapersDataViewModel.DataError.None -> null
            WallpapersDataViewModel.DataError.Unknown -> R.string.unexpected_error_occurred
            WallpapersDataViewModel.DataError.MalformedJson -> R.string.data_error_format
            WallpapersDataViewModel.DataError.NoNetwork -> R.string.data_error_network
        }
        message?.let {
            toast(it, Toast.LENGTH_LONG)
        }
    }

    open fun handleWallpapersUpdate(wallpapers: List<Wallpaper>) {
        wallpapersFragment?.updateItems(ArrayList(wallpapers))
    }

    open fun handleCollectionsUpdate(collections: List<Collection>) {
        collectionsFragment?.updateItems(collections)
    }

    companion object {
        private const val CURRENT_FRAGMENT_KEY = "current_fragment"
    }
}
