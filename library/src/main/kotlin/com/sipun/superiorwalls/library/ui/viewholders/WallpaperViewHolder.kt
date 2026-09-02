package com.sipun.superiorwalls.library.ui.viewholders

import android.view.View
import android.widget.TextView
import androidx.core.view.postDelayed
import androidx.core.widget.CompoundButtonCompat
import com.google.android.material.card.MaterialCardView
import com.sipun.superiorwalls.library.R
import com.sipun.superiorwalls.library.data.models.Wallpaper
import com.sipun.superiorwalls.library.extensions.context.string
import com.sipun.superiorwalls.library.extensions.resources.dpToPx
import com.sipun.superiorwalls.library.extensions.resources.hasContent
import com.sipun.superiorwalls.library.extensions.resources.tint
import com.sipun.superiorwalls.library.extensions.resources.withAlpha
import com.sipun.superiorwalls.library.extensions.views.context
import com.sipun.superiorwalls.library.extensions.views.findView
import com.sipun.superiorwalls.library.extensions.views.gone
import com.sipun.superiorwalls.library.extensions.views.loadWallpaperPicResPlaceholder
import com.sipun.superiorwalls.library.extensions.views.setPaddingTop
import com.sipun.superiorwalls.library.extensions.views.visible
import com.sipun.superiorwalls.library.extensions.views.visibleIf
import com.sipun.superiorwalls.library.ui.activities.ViewerActivity
import com.sipun.superiorwalls.library.ui.widgets.FavoriteCheckbox
import com.sipun.superiorwalls.library.ui.widgets.PortraitImageView

class WallpaperViewHolder(view: View) : PaletteGeneratorViewHolder(view) {
    internal val card: MaterialCardView? by view.findView(R.id.card)
    internal val image: PortraitImageView? by view.findView(R.id.wallpaper_image)
    internal val title: TextView? by view.findView(R.id.wallpaper_name)
    internal val author: TextView? by view.findView(R.id.wallpaper_author)
    internal val favorite: FavoriteCheckbox? by view.findView(R.id.fav_button)
    private val detailsBackground: View? by view.findView(R.id.wallpaper_details_background)

    fun bind(
        wallpaper: Wallpaper,
        canShowFavoritesButton: Boolean,
        canModifyFavorites: Boolean,
        onClick: (Wallpaper, WallpaperViewHolder) -> Unit,
        onFavClick: (Boolean, Wallpaper) -> Unit
    ) {
        if (canShowFavoritesButton) {
            favorite?.setOnCheckedChangeListener(null)
            favorite?.isChecked = wallpaper.isInFavorites
            favorite?.invalidate()
            favorite?.canCheck = canModifyFavorites
            favorite?.setOnClickListener { view ->
                view.postDelayed(FAV_DELAY) {
                    onFavClick(
                        (view as? FavoriteCheckbox)?.isChecked ?: wallpaper.isInFavorites,
                        wallpaper
                    )
                }
            }
            favorite?.onDisabledClickListener = { onFavClick(wallpaper.isInFavorites, wallpaper) }
            favorite?.visible()
        } else favorite?.gone()

        card?.transitionName = ViewerActivity.TRANSITION_NAME
        title?.text = wallpaper.name
        author?.text = wallpaper.author
        author?.visibleIf(wallpaper.author.hasContent())
        itemView.setOnClickListener { onClick(wallpaper, this) }
        image?.loadWallpaperPicResPlaceholder(
            wallpaper.url,
            wallpaper.thumbnail,
            context.string(R.string.wallpapers_placeholder),
            onImageLoaded = generatePalette
        )
    }

    @Suppress("ConstantConditionIf")
    override fun doWithColors(bgColor: Int, textColor: Int) {
        if (GRADIENT_CENTER_ALPHA <= .5F) {
            detailsBackground?.post {
                detailsBackground?.setPaddingTop(96.dpToPx)
                image?.postDelayed(2) { updateImageColors(bgColor) }
            }
        } else updateImageColors(bgColor)
        title?.setTextColor(textColor)
        author?.setTextColor(textColor)
        favorite?.let { favBtn ->
            favBtn.buttonDrawable =
                CompoundButtonCompat.getButtonDrawable(favBtn)?.tint(textColor)
        }
    }

    private fun updateImageColors(bgColor: Int) {
        image?.setOverlayColor(bgColor.withAlpha(OVERLAY_ALPHA))
        image?.setGradientColors(
            intArrayOf(
                bgColor.withAlpha(GRADIENT_END_ALPHA),
                bgColor.withAlpha(GRADIENT_CENTER_ALPHA),
                bgColor.withAlpha(GRADIENT_START_ALPHA)
            )
        )
    }

    companion object {
        private const val FAV_DELAY = 100L
        internal const val COLORED_TILES_ALPHA = .9F
        private const val GRADIENT_START_ALPHA = .9F
        private const val GRADIENT_CENTER_ALPHA = .9F
        private const val GRADIENT_END_ALPHA = .9F
        private const val OVERLAY_ALPHA = .15F
    }
}
