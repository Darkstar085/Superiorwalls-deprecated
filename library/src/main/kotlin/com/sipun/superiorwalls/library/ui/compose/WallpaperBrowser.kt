package com.sipun.superiorwalls.library.ui.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sipun.superiorwalls.library.data.models.Wallpaper

@Composable
fun WallpaperBrowser(
    wallpapers: List<Wallpaper>,
    canModifyFavorites: Boolean,
    onWallpaperClick: (Wallpaper) -> Unit,
    onFavoriteClick: (Wallpaper, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    emptyTitle: String = "No wallpapers found",
    emptyMessage: String = "Try another search or refresh your collection.",
) {
    if (wallpapers.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isLoading) CircularProgressIndicator()
            else EmptyState(emptyTitle, emptyMessage)
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 156.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 28.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(wallpapers, key = { it.url }) { wallpaper ->
            WallpaperCard(wallpaper, canModifyFavorites, { onWallpaperClick(wallpaper) }, { onFavoriteClick(wallpaper, it) }, Modifier.animateContentSize())
        }
    }
}

@Composable
private fun WallpaperCard(
    wallpaper: Wallpaper,
    canModifyFavorites: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(24.dp))) {
            AsyncImage(
                model = wallpaper.thumbnail?.takeIf { it.isNotBlank() } ?: wallpaper.url,
                contentDescription = wallpaper.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color.Transparent, .55f to Color.Transparent, 1f to Color.Black.copy(alpha = .78f))))
            if (canModifyFavorites) {
                Surface(Modifier.align(Alignment.TopEnd).padding(10.dp), CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = .88f)) {
                    IconButton(onClick = { onFavoriteClick(!wallpaper.isInFavorites) }, Modifier.size(44.dp)) {
                        Icon(if (wallpaper.isInFavorites) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, if (wallpaper.isInFavorites) "Remove from favorites" else "Add to favorites", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(15.dp)) {
                Text(wallpaper.name, color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                wallpaper.author?.takeIf { it.isNotBlank() }?.let { Spacer(Modifier.height(3.dp)); Text(it, color = Color.White.copy(alpha = .78f), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            }
        }
    }
}
