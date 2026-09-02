package com.sipun.superiorwalls.library.extensions.wallpapers

import com.sipun.superiorwalls.library.data.models.Wallpaper
import com.sipun.superiorwalls.library.ui.adapters.WallpapersAdapter
import com.sipun.superiorwalls.library.ui.viewholders.WallpaperViewHolder

internal fun wallpapersAdapter(
    canShowFavoritesButton: Boolean = true,
    canModifyFavorites: Boolean = true,
    block: WallpapersAdapter.() -> Unit
): WallpapersAdapter =
    WallpapersAdapter(canShowFavoritesButton, canModifyFavorites).apply(block)

internal fun WallpapersAdapter.onClick(what: (Wallpaper, WallpaperViewHolder) -> Unit) {
    this.onClick = what
}

internal fun WallpapersAdapter.onFavClick(what: (Boolean, Wallpaper) -> Unit) {
    this.onFavClick = what
}