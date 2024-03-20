package com.saggitt.omega.allapps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.saggitt.omega.theme.OmegaAppTheme
import com.saggitt.omega.util.prefs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabsBar(
    modifier: Modifier = Modifier,
    list: List<AllAppsTabs.Tab>,
    selectedPage: MutableState<Int>,
    onClick: (Int) -> Unit,
    onLongClick: (AllAppsTabs.Tab) -> Unit,
) {
    OmegaAppTheme {
        val context = LocalContext.current

        AnimatedVisibility(list.size > 1) {
            LazyRow(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                itemsIndexed(list) { i, item ->
                    val selected by remember(selectedPage.value) {
                        mutableStateOf(selectedPage.value == i)
                    }

                    Surface(
                        modifier = Modifier
                            .combinedClickable(
                                onClick = { onClick(i) },
                                onLongClick = { onLongClick(item) },
                            ),
                        shape = MaterialTheme.shapes.extraLarge,
                        contentColor = when {
                            selected -> MaterialTheme.colorScheme.background
                            else     -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        color = when {
                            selected -> Color(
                                item.drawerTab.color.value?.substringAfter("|")?.toColorInt()
                                    ?: context.prefs.profileAccentColor.getColor()
                            )

                            else     -> MaterialTheme.colorScheme.surfaceVariant
                        },
                    ) {
                        Row(
                            modifier = Modifier
                                .widthIn(min = 72.dp)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = item.name)
                        }
                    }
                }
            }
        }
    }
}