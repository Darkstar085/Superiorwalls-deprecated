package com.sipun.superiorwalls.library.extensions.views

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.SearchView
import com.sipun.superiorwalls.library.R
import com.sipun.superiorwalls.library.extensions.context.color
import com.sipun.superiorwalls.library.extensions.context.resolveColor
import com.sipun.superiorwalls.library.extensions.resources.tint
import com.sipun.superiorwalls.library.extensions.resources.withAlpha

fun SearchView.tint(
    @ColorInt color: Int =
        context.resolveColor(
            com.google.android.material.R.attr.colorOnPrimary,
            context.color(R.color.onPrimary)
        ),
    @ColorInt hintColor: Int = color
) {
    val field: EditText? by findView(androidx.appcompat.R.id.search_src_text)
    field?.setTextColor(color)
    field?.setHintTextColor(if (hintColor == color) hintColor.withAlpha(0.6F) else hintColor)
    field?.tint(color)

    val plate: View? by findView(androidx.appcompat.R.id.search_plate)
    plate?.background = null

    val iconsIds = arrayOf(
        androidx.appcompat.R.id.search_button,
        androidx.appcompat.R.id.search_close_btn,
        androidx.appcompat.R.id.search_go_btn,
        androidx.appcompat.R.id.search_voice_btn,
        androidx.appcompat.R.id.search_mag_icon
    )
    iconsIds.forEach {
        try {
            findViewById<ImageView?>(it)?.tint(color)
        } catch (_: Exception) {
        }
    }
}
