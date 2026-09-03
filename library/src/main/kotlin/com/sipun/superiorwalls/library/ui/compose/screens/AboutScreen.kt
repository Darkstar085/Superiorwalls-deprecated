package com.sipun.superiorwalls.library.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sipun.superiorwalls.library.data.models.AboutItem

@Composable
fun AboutScreen(
    title: String,
    designerItems: List<AboutItem>,
    internalItems: List<AboutItem>,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(padding),
        ) {
            item { Text("Credits", style = MaterialTheme.typography.headlineSmall) }
            items(designerItems, key = { it.name }) { AboutCard(it) }
            if (internalItems.isNotEmpty()) {
                item { Text("Superiorwalls", style = MaterialTheme.typography.headlineSmall) }
                items(internalItems, key = { it.name }) { AboutCard(it) }
            }
        }
    }
}

@Composable
private fun AboutCard(item: AboutItem) {
    val uriHandler = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = item.photo,
                contentDescription = item.name,
                modifier = Modifier.padding(top = 2.dp),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleLarge)
                Text(item.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item.buttons.forEach { (label, link) ->
                androidx.compose.material3.TextButton(onClick = { uriHandler.openUri(link) }) { Text(label) }
            }
        }
    }
}
