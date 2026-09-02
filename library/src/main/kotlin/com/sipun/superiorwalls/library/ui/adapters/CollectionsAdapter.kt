package com.sipun.superiorwalls.library.ui.adapters

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sipun.superiorwalls.library.R
import com.sipun.superiorwalls.library.data.models.Collection
import com.sipun.superiorwalls.library.extensions.context.boolean
import com.sipun.superiorwalls.library.extensions.views.inflate
import com.sipun.superiorwalls.library.ui.viewholders.CollectionViewHolder

class CollectionsAdapter(private val onClick: ((collection: Collection) -> Unit)? = null) :
    RecyclerView.Adapter<CollectionViewHolder>() {

    var collections: List<Collection> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        holder.bind(collections[position], onClick)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val shouldBeFilled = parent.context.boolean(R.bool.enable_filled_collection_preview)
        return CollectionViewHolder(
            parent.inflate(
                if (shouldBeFilled) R.layout.item_collection_filled
                else R.layout.item_collection
            )
        )
    }

    override fun getItemCount(): Int = collections.size
}
