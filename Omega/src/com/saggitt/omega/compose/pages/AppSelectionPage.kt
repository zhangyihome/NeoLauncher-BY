/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.compose.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.saggitt.omega.compose.components.ListItemWithIcon
import com.saggitt.omega.compose.components.OverflowMenu
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.compose.components.preferences.PreferenceGroup
import com.saggitt.omega.util.appsState

@Composable
fun AppSelectionPage(
    pageTitle: String,
    selectedApps: Set<String>,
    pluralTitleId: Int,
    onSave: (Set<String>) -> Unit
) {
    val colors = CheckboxDefaults.colors(
        checkedColor = MaterialTheme.colorScheme.primary,
        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    var selected by remember { mutableStateOf(selectedApps) }
    var title by remember { mutableStateOf(pageTitle) }
    val allApps by appsState(comparator = hiddenAppsComparator(selectedApps))
    val pluralTitle = stringResource(id = pluralTitleId, selected.size)
    title = if (selected.isNotEmpty()) {
        pluralTitle
    } else {
        pageTitle
    }

    ViewWithActionBar(
        title = title,
        actions = {
            OverflowMenu {
                DropdownMenuItem(
                    onClick = {
                        selected = emptySet()
                        hideMenu()
                    },
                    text = { Text(text = stringResource(id = R.string.app_reset)) }
                )
            }
        },
        onBackAction = {
            onSave(selected)
        }
    ) { paddingValues ->
        //TODO: Show loading indicator while apps are being loaded
        val groupSize = allApps.size
        PreferenceGroup {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 8.dp),
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                itemsIndexed(allApps) { index, app ->
                    val isSelected = rememberSaveable(selected) {
                        mutableStateOf(selected.contains(app.key.toString()))
                    }
                    ListItemWithIcon(
                        title = app.label,
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = if (index == 0) 16.dp else 6.dp,
                                    topEnd = if (index == 0) 16.dp else 6.dp,
                                    bottomStart = if (index == groupSize - 1) 16.dp else 6.dp,
                                    bottomEnd = if (index == groupSize - 1) 16.dp else 6.dp
                                )
                            )
                            .background(
                                color = if (isSelected.value)
                                    MaterialTheme.colorScheme.primary.copy(0.4f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable {
                                selected = if (isSelected.value) selected.minus(app.key.toString())
                                else selected.plus(app.key.toString())
                            },
                        summary = "",
                        startIcon = {
                            Image(
                                painter = BitmapPainter(app.icon.asImageBitmap()),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                        },
                        endCheckbox = {
                            Checkbox(
                                checked = isSelected.value,
                                onCheckedChange = {
                                    selected = if (it) selected.plus(app.key.toString())
                                    else selected.minus(app.key.toString())
                                },
                                colors = colors
                            )
                        },
                        horizontalPadding = 0.dp,
                        verticalPadding = 6.dp
                    )
                }
            }
        }
    }

    DisposableEffect(key1 = null) {
        onDispose {
            onSave(selected)
        }
    }
}
