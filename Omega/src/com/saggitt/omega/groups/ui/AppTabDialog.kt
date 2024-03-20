/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
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

package com.saggitt.omega.groups.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.compose.components.DialogNegativeButton
import com.saggitt.omega.compose.components.DialogPositiveButton
import com.saggitt.omega.compose.navigation.Routes
import com.saggitt.omega.groups.category.DrawerTabs
import com.saggitt.omega.preferences.PreferenceActivity
import com.saggitt.omega.util.addOrRemove

@Composable
fun AppTabDialog(
    componentKey: ComponentKey,
    openDialogCustom: MutableState<Boolean>,
) {
    Dialog(
        onDismissRequest = { openDialogCustom.value = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AppTabDialogUI(
            componentKey = componentKey,
            openDialogCustom = openDialogCustom
        )
    }
}

@Composable
fun AppTabDialogUI(
    componentKey: ComponentKey,
    openDialogCustom: MutableState<Boolean>,
) {
    val context = LocalContext.current
    val prefs = Utilities.getNeoPrefs(context)

    var radius = 16.dp
    if (prefs.profileWindowCornerRadius.getValue() > -1) {
        radius = prefs.profileWindowCornerRadius.getValue().dp
    }
    val cornerRadius by remember { mutableStateOf(radius) }

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column {
            val tabs: List<DrawerTabs.CustomTab> =
                prefs.drawerTabs.getGroups().mapNotNull { it as? DrawerTabs.CustomTab }
            val entries = tabs.map { it.title }.toList()
            val checkedEntries = tabs.map {
                it.contents.value().contains(componentKey)
            }.toBooleanArray()

            val selectedItems = checkedEntries.toMutableList()
            LazyColumn(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 16.dp)
                    .weight(1f, false)
            ) {
                itemsIndexed(entries) { index, tabName ->
                    var isSelected by rememberSaveable { mutableStateOf(selectedItems[index]) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isSelected = !isSelected
                                selectedItems[index] = isSelected
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = {
                                isSelected = !isSelected
                                selectedItems[index] = isSelected
                            },
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                            )
                        )
                        Text(text = tabName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            //Button Rows
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                DialogPositiveButton(
                    cornerRadius = cornerRadius,
                    textId = R.string.tabs_manage,
                    onClick = {
                        openDialogCustom.value = false
                        context.startActivity(
                            PreferenceActivity.createIntent(
                                context,
                                "/${Routes.CATEGORIZE_APPS}/"
                            )
                        )
                    },
                )

                Spacer(Modifier.weight(1f))

                DialogNegativeButton(
                    modifier = Modifier.padding(start = 16.dp),
                    cornerRadius = cornerRadius,
                    onClick = {
                        openDialogCustom.value = false
                    }
                )

                DialogPositiveButton(
                    modifier = Modifier.padding(start = 16.dp),
                    cornerRadius = cornerRadius,
                    onClick = {
                        tabs.forEachIndexed { index, tab ->
                            tab.contents.value().addOrRemove(componentKey, selectedItems[index])
                        }
                        tabs.hashCode()
                        prefs.drawerAppGroupsManager.drawerTabs.saveToJson()
                        openDialogCustom.value = false
                    }
                )
            }
        }
    }
}