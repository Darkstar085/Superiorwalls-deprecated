package com.sipun.superiorwalls.library.ui.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sipun.superiorwalls.library.data.models.Collection
import com.sipun.superiorwalls.library.data.models.Wallpaper

private enum class HomeDestination { WALLPAPERS, COLLECTIONS, FAVORITES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperiorwallsHome(
    wallpapers: List<Wallpaper>,
    collections: List<Collection>,
    favorites: List<Wallpaper>,
    canModifyFavorites: Boolean,
    useDarkTheme: Boolean,
    useDynamicColor: Boolean,
    useAmoled: Boolean,
    onWallpaperClick: (Wallpaper) -> Unit,
    onFavoriteClick: (Wallpaper, Boolean) -> Unit,
    onCollectionClick: (Collection) -> Unit,
    onSettingsClick: () -> Unit,
) {
    var destination by remember { mutableIntStateOf(HomeDestination.WALLPAPERS.ordinal) }
    var query by remember { mutableStateOf("") }
    var searchOpen by remember { mutableStateOf(false) }
    val current = HomeDestination.entries[destination]
    val source = when (current) {
        HomeDestination.WALLPAPERS -> wallpapers
        HomeDestination.FAVORITES -> favorites
        HomeDestination.COLLECTIONS -> emptyList()
    }
    val filtered = if (query.isBlank()) source else source.filter {
        it.name.contains(query, true) || it.author.orEmpty().contains(query, true) ||
            it.collections.orEmpty().contains(query, true)
    }

    SuperiorwallsTheme(
        darkTheme = useDarkTheme,
        dynamicColor = useDynamicColor,
        amoled = useAmoled,
    ) {
        Scaffold(
            topBar = {
                if (searchOpen) {
                    SearchBar(
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = query,
                                onQueryChange = { query = it },
                                onSearch = { searchOpen = false },
                                expanded = false,
                                onExpandedChange = { searchOpen = it },
                                placeholder = { Text("Search wallpapers") },
                                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                                trailingIcon = {
                                    IconButton(onClick = { query = ""; searchOpen = false }) {
                                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Close search")
                                    }
                                },
                            )
                        },
                        expanded = false,
                        onExpandedChange = { searchOpen = it },
                    ) {}
                } else {
                    LargeTopAppBar(
                        title = {
                            Column {
                                Text("Superiorwalls")
                                Text(
                                    when (current) {
                                        HomeDestination.WALLPAPERS -> "Discover something beautiful"
                                        HomeDestination.COLLECTIONS -> "Curated collections"
                                        HomeDestination.FAVORITES -> "Your saved wallpapers"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { searchOpen = true }) {
                                Icon(Icons.Rounded.Search, "Search")
                            }
                            IconButton(onClick = onSettingsClick) {
                                Icon(Icons.Rounded.Settings, "Settings")
                            }
                        },
                        scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
                    )
                }
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = current == HomeDestination.WALLPAPERS,
                        onClick = { destination = HomeDestination.WALLPAPERS.ordinal },
                        icon = { Icon(Icons.Rounded.GridView, null) },
                        label = { Text("Wallpapers") },
                    )
                    NavigationBarItem(
                        selected = current == HomeDestination.COLLECTIONS,
                        onClick = { destination = HomeDestination.COLLECTIONS.ordinal },
                        icon = { Icon(Icons.Rounded.CollectionsBookmark, null) },
                        label = { Text("Collections") },
                    )
                    NavigationBarItem(
                        selected = current == HomeDestination.FAVORITES,
                        onClick = { destination = HomeDestination.FAVORITES.ordinal },
                        icon = { Icon(Icons.Rounded.Favorite, null) },
                        label = { Text("Favorites") },
                    )
                }
            },
        ) { padding ->
            AnimatedContent(
                targetState = current,
                modifier = Modifier.fillMaxSize().padding(padding),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "home_destination",
            ) { screen ->
                when (screen) {
                    HomeDestination.COLLECTIONS -> CollectionGrid(collections, onCollectionClick)
                    HomeDestination.WALLPAPERS, HomeDestination.FAVORITES -> {
                        WallpaperBrowser(
                            wallpapers = filtered,
                            canModifyFavorites = canModifyFavorites,
                            onWallpaperClick = onWallpaperClick,
                            onFavoriteClick = onFavoriteClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionGrid(
    collections: List<Collection>,
    onClick: (Collection) -> Unit,
) {
    if (collections.isEmpty()) {
        EmptyState("No collections yet", "Collections appear here after wallpapers load.")
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(240.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(collections, key = { it.name }) { collection ->
            Surface(
                onClick = { onClick(collection) },
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Box(Modifier.height(210.dp)) {
                    AsyncImage(
                        model = collection.cover?.thumbnail ?: collection.cover?.url,
                        contentDescription = collection.displayName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .82f)))
                        )
                    )
                    Column(Modifier.align(Alignment.BottomStart).padding(18.dp)) {
                        Text(collection.displayName, color = Color.White, style = MaterialTheme.typography.titleLarge)
                        Text("${collection.count} wallpapers", color = Color.White.copy(alpha = .75f))
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(title: String, message: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                Icon(Icons.Rounded.FavoriteBorder, null, Modifier.padding(18.dp).size(34.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
