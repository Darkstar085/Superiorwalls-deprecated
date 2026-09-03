package com.sipun.superiorwalls.library.ui.compose

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import com.sipun.superiorwalls.library.data.models.Wallpaper
import com.sipun.superiorwalls.library.extensions.resources.toHexString
import com.sipun.superiorwalls.library.extensions.utils.bestTextColor
import com.sipun.superiorwalls.library.extensions.utils.bestSwatches
import com.sipun.superiorwalls.library.R
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperDetailsSheet(
    wallpaper: Wallpaper,
    palette: Palette?,
    showPalette: Boolean,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboard = LocalClipboardManager.current
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            item {
                Text(
                    text = stringResource(R.string.details),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
                )
            }
            items(wallpaper.details) { (titleRes, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Text(stringResource(titleRes), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                    Text(value, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (showPalette) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
                    Text(stringResource(R.string.palette), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp))
                    Text(stringResource(R.string.tap_to_copy), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp))
                }
                items(palette?.bestSwatches.orEmpty()) { swatch ->
                    val hex = swatch.rgb.toHexString()
                    Button(
                        onClick = { clipboard.setText(AnnotatedString(hex)) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(hex, color = Color(swatch.bestTextColor))
                    }
                }
            }
            item { androidx.compose.foundation.layout.Spacer(Modifier.size(24.dp)) }
        }
    }
}

@Composable
fun WallpaperApplyDialog(
    selectedOption: Int?,
    onOptionSelected: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val options = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        listOf(R.string.home_screen, R.string.lock_screen, R.string.home_and_lock_screens, R.string.apply_w_external_app)
    } else {
        listOf(R.string.home_and_lock_screens, R.string.apply_w_external_app)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.apply_to)) },
        text = {
            Column {
                options.forEachIndexed { index, titleRes ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = selectedOption == index, onClick = { onOptionSelected(index) })
                        TextButton(onClick = { onOptionSelected(index) }, modifier = Modifier.weight(1f)) {
                            Text(stringResource(titleRes), modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = selectedOption != null) { Text(stringResource(android.R.string.ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) } },
    )
}
