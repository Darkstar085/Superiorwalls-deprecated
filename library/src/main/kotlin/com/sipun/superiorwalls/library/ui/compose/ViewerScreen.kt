package com.sipun.superiorwalls.library.ui.compose

import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.sipun.superiorwalls.library.data.models.Wallpaper

@Composable
fun ViewerScreen(
    wallpaper: Wallpaper,
    canModifyFavorites: Boolean,
    showDownload: Boolean,
    onBack: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDetails: () -> Unit,
    onDownload: () -> Unit,
    onApply: () -> Unit,
    onFavorite: () -> Unit,
    onToggleSystemUi: () -> Unit,
    onImageLoaded: (Drawable?) -> Unit = {},
) {
    var scale by remember(wallpaper.url) { mutableFloatStateOf(1f) }
    var offsetX by remember(wallpaper.url) { mutableFloatStateOf(0f) }
    var offsetY by remember(wallpaper.url) { mutableFloatStateOf(0f) }
    var controlsVisible by remember { mutableStateOf(true) }
    val painter = rememberAsyncImagePainter(wallpaper.url)

    androidx.compose.runtime.LaunchedEffect(painter.state) {
        val state = painter.state
        if (state is AsyncImagePainter.State.Success) onImageLoaded(state.result.drawable)
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AsyncImage(
            painter = painter,
            contentDescription = wallpaper.name,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(wallpaper.url) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
                .pointerInput(wallpaper.url) {
                    detectTapGestures(onDoubleTap = { controlsVisible = !controlsVisible; onToggleSystemUi() })
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                },
            contentScale = ContentScale.Fit,
        )

        if (controlsVisible) {
            Surface(
                modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(),
                color = Color.Black.copy(alpha = .52f),
            ) {
                Row(Modifier.padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Color.White) }
                    Column(Modifier.weight(1f)) {
                        Text(wallpaper.name, color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        wallpaper.author?.takeIf { it.isNotBlank() }?.let {
                            Text(it, color = Color.White.copy(alpha = .72f), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            Row(Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                ViewerCircleButton(Icons.Rounded.KeyboardArrowLeft, "Previous", onPrevious)
                ViewerCircleButton(Icons.Rounded.KeyboardArrowRight, "Next", onNext)
            }

            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding(),
                color = Color.Black.copy(alpha = .60f),
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    ViewerAction(Icons.Rounded.Info, "Details", onDetails)
                    if (showDownload) ViewerAction(Icons.Rounded.Download, "Download", onDownload)
                    ViewerAction(Icons.Rounded.Wallpaper, "Apply", onApply)
                    if (canModifyFavorites) ViewerAction(
                        if (wallpaper.isInFavorites) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        if (wallpaper.isInFavorites) "Remove favorite" else "Add favorite",
                        onFavorite,
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewerCircleButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    Surface(shape = androidx.compose.foundation.shape.CircleShape, color = Color.Black.copy(alpha = .55f)) {
        IconButton(onClick = onClick, modifier = Modifier.size(56.dp)) { Icon(icon, description, tint = Color.White) }
    }
}

@Composable
private fun ViewerAction(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(52.dp)) { Icon(icon, description, tint = Color.White) }
}
