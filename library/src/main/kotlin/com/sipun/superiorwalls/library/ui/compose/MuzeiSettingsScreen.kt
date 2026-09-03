package com.sipun.superiorwalls.library.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.sipun.superiorwalls.library.R
import com.sipun.superiorwalls.library.data.models.Collection

@Composable
fun MuzeiSettingsScreen(
    selectedCollections: String,
    refreshOnWifiOnly: Boolean,
    collections: List<Collection>,
    showCollections: Boolean,
    onSelectedCollectionsChange: (String) -> Unit,
    onRefreshOnWifiOnlyChange: (Boolean) -> Unit,
    onChooseCollections: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(stringResource(R.string.muzei_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.refresh_only_on_wifi)) },
                leadingContent = { Icon(Icons.Rounded.Wifi, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingContent = {
                    Switch(checked = refreshOnWifiOnly, onCheckedChange = onRefreshOnWifiOnlyChange)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRefreshOnWifiOnlyChange(!refreshOnWifiOnly) },
            )
            if (showCollections) {
                HorizontalDivider(Modifier.padding(horizontal = 20.dp))
                ListItem(
                    headlineContent = { Text(stringResource(R.string.choose_collections_title)) },
                    supportingContent = {
                        Text(selectedCollections.ifBlank { stringResource(R.string.no_collections_selected) }, maxLines = 2)
                    },
                    leadingContent = { Icon(Icons.Rounded.Collections, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = collections.isNotEmpty(), onClick = onChooseCollections),
                )
            }
        }
    }
}

@Composable
fun CollectionSelectionDialog(
    collections: List<Collection>,
    selectedCollections: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val available = remember(collections) {
        (listOf(Collection("Favorites")) + collections).distinctBy { it.displayName.lowercase() }
    }
    val initial = remember(selectedCollections) {
        selectedCollections.split(",").map { it.trim() }.filter(String::isNotBlank).toSet()
    }
    var selected by remember(initial) { mutableStateOf(initial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.choose_collections_title)) },
        text = {
            Column(Modifier.selectableGroup()) {
                available.forEach { collection ->
                    val checked = selected.any { it.equals(collection.displayName, ignoreCase = true) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = checked,
                                role = Role.Checkbox,
                                onClick = {
                                    selected = if (checked) {
                                        selected.filterNot { it.equals(collection.displayName, true) }.toSet()
                                    } else {
                                        selected + collection.displayName
                                    }
                                },
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Checkbox(checked = checked, onCheckedChange = null)
                        Text(collection.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected.joinToString(", ")) }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) } },
    )
}
