package com.sipun.superiorwalls.library.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.sipun.superiorwalls.library.R
import com.sipun.superiorwalls.library.data.models.Collection
import com.sipun.superiorwalls.library.extensions.context.integer
import com.sipun.superiorwalls.library.extensions.resources.lower
import com.sipun.superiorwalls.library.ui.activities.CollectionActivity
import com.sipun.superiorwalls.library.ui.activities.ViewerActivity
import com.sipun.superiorwalls.library.ui.activities.base.BaseFavoritesConnectedActivity
import com.sipun.superiorwalls.library.ui.adapters.CollectionsAdapter
import com.sipun.superiorwalls.library.ui.fragments.base.BaseWallpaperFragment

open class CollectionsFragment : BaseWallpaperFragment<Collection>() {

    private val collectionsAdapter: CollectionsAdapter by lazy { CollectionsAdapter { onClicked(it) } }
    private var openActivityLauncher: ActivityResultLauncher<Intent?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == ViewerActivity.FAVORITES_MODIFIED_RESULT) {
                    (activity as? BaseFavoritesConnectedActivity<*>)?.loadWallpapersData(true)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val columnsCount = context?.integer(R.integer.collections_columns_count, 1) ?: 1
        recyclerView?.layoutManager =
            GridLayoutManager(context, columnsCount, GridLayoutManager.VERTICAL, false)
        recyclerView?.adapter = collectionsAdapter
        (activity as? BaseFavoritesConnectedActivity<*>)?.loadWallpapersData(
            triggerErrorListener = false
        )
    }

    override fun updateItemsInAdapter(items: List<Collection>) {
        collectionsAdapter.collections = items
    }

    override fun getFilteredItems(
        originalItems: ArrayList<Collection>,
        filter: String
    ): ArrayList<Collection> =
        ArrayList(originalItems.filter { it.name.lower().contains(filter.lower()) })

    open fun onClicked(collection: Collection) {
        val intent = getTargetActivityIntent()
            .apply {
                putExtra(CollectionActivity.COLLECTION_KEY, collection)
                putExtra(CollectionActivity.COLLECTION_NAME_KEY, collection.name)
            }
        try {
            openActivityLauncher?.launch(intent)
        } catch (e: Exception) {
        }
    }

    override fun loadData() {
        (activity as? BaseFavoritesConnectedActivity<*>)?.loadWallpapersData(true)
    }

    override fun getTargetActivityIntent(): Intent =
        Intent(activity, CollectionActivity::class.java)

    override fun getEmptyText(): Int = R.string.no_collections_found
    override fun allowCheckingFirstRun(): Boolean = true

    companion object {
        const val TAG = "collections_fragment"

        @JvmStatic
        fun create(list: ArrayList<Collection> = ArrayList()) =
            CollectionsFragment().apply { updateItemsInAdapter(list) }
    }
}
