package com.sipun.superiorwalls.library.ui.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.sipun.superiorwalls.library.data.models.Wallpaper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    title: String,
    wallpapers: List<Wallpaper>,
    canModifyFavorites: Boolean,
    searchQuery: String,
    searchOpen: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSearchOpen: () -> Unit,
    onSearchClose: () -> Unit,
    onBack: () -> Unit,
    onWallpaperClick: (Wallpaper) -> Unit,
    onFavoriteClick: (Wallpaper, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = searchOpen) { onSearchClose() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    if (searchOpen) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search wallpapers") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { }),
                        )
                    } else {
                        Text(title)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = if (searchOpen) onSearchClose else onBack) {
                        Icon(
                            imageVector = if (searchOpen) Icons.AutoMirrored.Rounded.ArrowBack else Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    if (searchOpen) {
                        IconButton(onClick = onSearchClose) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close search")
                        }
                    } else {
                        IconButton(onClick = onSearchOpen) {
                            Icon(Icons.Rounded.Search, contentDescription = "Search wallpapers")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Top,
        ) {
            WallpaperBrowser(
                wallpapers = wallpapers,
                canModifyFavorites = canModifyFavorites,
                onWallpaperClick = onWallpaperClick,
                onFavoriteClick = onFavoriteClick,
                emptyTitle = "No wallpapers found",
                emptyMessage = if (searchQuery.isBlank()) {
                    "This collection is empty."
                } else {
                    "Try another search."
                },
            )
        }
    }
}
