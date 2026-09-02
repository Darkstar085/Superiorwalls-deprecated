package com.sipun.superiorwalls.library.ui.viewholders

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import com.sipun.superiorwalls.library.R
import com.sipun.superiorwalls.library.data.models.AboutItem
import com.sipun.superiorwalls.library.extensions.resources.hasContent
import com.sipun.superiorwalls.library.extensions.views.findView
import com.sipun.superiorwalls.library.extensions.views.loadWallpaperPic
import com.sipun.superiorwalls.library.extensions.views.visibleIf
import com.sipun.superiorwalls.library.ui.widgets.AboutButtonsLayout

class AboutViewHolder(view: View) : SectionedViewHolder(view) {

    private val photoImageView: AppCompatImageView? by view.findView(R.id.photo)
    private val nameTextView: TextView? by view.findView(R.id.name)
    private val descriptionTextView: TextView? by view.findView(R.id.description)
    private val buttonsView: AboutButtonsLayout? by view.findView(R.id.buttons)

    fun bind(aboutItem: AboutItem?) {
        aboutItem ?: return
        nameTextView?.text = aboutItem.name
        nameTextView?.visibleIf(aboutItem.name.hasContent())
        descriptionTextView?.text = aboutItem.description
        descriptionTextView?.visibleIf(aboutItem.description.orEmpty().hasContent())
        aboutItem.links.forEach { buttonsView?.addButton(it.first, it.second) }
        buttonsView?.visibleIf(aboutItem.links.isNotEmpty())
        photoImageView?.loadWallpaperPic(aboutItem.photoUrl.orEmpty(), cropAsCircle = true)
    }
}
