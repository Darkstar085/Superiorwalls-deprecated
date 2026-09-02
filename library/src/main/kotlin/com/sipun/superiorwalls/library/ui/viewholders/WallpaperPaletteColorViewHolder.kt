package com.sipun.superiorwalls.library.ui.viewholders

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.palette.graphics.Palette
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import com.sipun.superiorwalls.library.R
import com.sipun.superiorwalls.library.extensions.context.toast
import com.sipun.superiorwalls.library.extensions.resources.toHexString
import com.sipun.superiorwalls.library.extensions.utils.bestTextColor
import com.sipun.superiorwalls.library.extensions.views.context
import com.sipun.superiorwalls.library.extensions.views.findView

class WallpaperPaletteColorViewHolder(view: View) : SectionedViewHolder(view) {

    private val colorBtn: AppCompatButton? by view.findView(R.id.palette_color_btn)

    fun bind(swatch: Palette.Swatch? = null) {
        swatch ?: return
        colorBtn?.setBackgroundColor(swatch.rgb)
        colorBtn?.setTextColor(swatch.bestTextColor)
        colorBtn?.text = swatch.rgb.toHexString()
        colorBtn?.setOnClickListener {
            val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager
            clipboard?.setPrimaryClip(
                ClipData.newPlainText("label", swatch.rgb.toHexString())
            )
            context.toast(R.string.copied_to_clipboard)
        }
    }
}