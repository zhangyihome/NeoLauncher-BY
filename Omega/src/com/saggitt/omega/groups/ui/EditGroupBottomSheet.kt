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

package com.saggitt.omega.groups.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.compose.components.BaseDialog
import com.saggitt.omega.compose.components.preferences.BasePreference
import com.saggitt.omega.compose.pages.ColorSelectionDialog
import com.saggitt.omega.flowerpot.Flowerpot
import com.saggitt.omega.groups.AppGroups
import com.saggitt.omega.groups.AppGroupsManager
import com.saggitt.omega.groups.category.DrawerFolders
import com.saggitt.omega.groups.category.DrawerTabs
import com.saggitt.omega.groups.category.FlowerpotTabs.Companion.KEY_FLOWERPOT
import com.saggitt.omega.groups.category.FlowerpotTabs.Companion.TYPE_FLOWERPOT
import com.saggitt.omega.theme.AccentColorOption
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.prefs

@Composable
fun EditGroupBottomSheet(
    category: AppGroupsManager.Category,
    group: AppGroups.Group,
    onClose: (Int) -> Unit,
) {
    val context = LocalContext.current
    val prefs = Utilities.getNeoPrefs(context)
    val flowerpotManager = Flowerpot.Manager.getInstance(context)
    val config = group.customizations
    val keyboardController = LocalSoftwareKeyboardController.current
    val openDialog = remember { mutableStateOf(false) }

    var title by remember { mutableStateOf(group.title) }

    var isHidden by remember {
        mutableStateOf(
            (config[AppGroups.KEY_HIDE_FROM_ALL_APPS] as? AppGroups.BooleanCustomization)?.value
                ?: true
        )
    }

    val colorPicker = remember { mutableStateOf(false) }
    var selectedCategory by remember {
        mutableStateOf(
            (config[KEY_FLOWERPOT] as? AppGroups.StringCustomization)?.value
                ?: AppGroups.KEY_FLOWERPOT_DEFAULT
        )
    }
    val allAppsTab = "profile{\"matchesAll\":true}}"

    val apps: Array<ComponentKey> = if (group.type == allAppsTab) {
        prefs.drawerHiddenAppSet.getValue().map { ComponentKey.fromString(it)!! }.toTypedArray()
    } else {
        (config[AppGroups.KEY_ITEMS] as? AppGroups.ComponentsCustomization)?.value?.toTypedArray()
            ?: emptyArray()
    }

    val selectedApps = remember { mutableStateListOf(*apps) }
    var color by remember {
        mutableStateOf(
            (config[AppGroups.KEY_COLOR] as? AppGroups.StringCustomization)?.value
                ?: context.prefs.profileAccentColor.getValue()
        )
    }
    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Divider(
            modifier = Modifier
                .width(48.dp)
                .height(2.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier
                .fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12F),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
            }),
            shape = MaterialTheme.shapes.large,
            label = {
                Text(
                    text = stringResource(id = R.string.name),
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            isError = title.isEmpty()
        )
        Spacer(modifier = Modifier.height(8.dp))
        val summary = context.resources.getQuantityString(
            R.plurals.tab_apps_count,
            selectedApps.size,
            selectedApps.size
        )
        when (group.type) {
            allAppsTab -> {
                BasePreference(
                    titleId = R.string.title__drawer_hide_apps,
                    summary = summary,
                    startWidget = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_apps),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    endWidget = {
                        Icon(
                            painter = painterResource(id = R.drawable.chevron_right),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    index = 0,
                    groupSize = 2
                ) { openDialog.value = true }
                Spacer(modifier = Modifier.height(4.dp))

                if (openDialog.value) {
                    BaseDialog(openDialogCustom = openDialog) {
                        Card(
                            shape = MaterialTheme.shapes.extraLarge,
                            modifier = Modifier.padding(8.dp),
                            elevation = CardDefaults.elevatedCardElevation(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                        ) {
                            GroupAppSelection(
                                selectedApps = selectedApps.map { it.toString() }.toSet(),
                            ) {
                                val componentsSet =
                                    it.mapNotNull { ck -> ComponentKey.fromString(ck) }
                                        .toMutableSet()
                                selectedApps.clear()
                                selectedApps.addAll(componentsSet)
                                prefs.drawerHiddenAppSet.setValue(selectedApps.map { key -> key.toString() }
                                    .toSet())
                                openDialog.value = false
                            }
                        }
                    }
                }
            }

            DrawerTabs.TYPE_CUSTOM, DrawerFolders.TYPE_CUSTOM, TYPE_FLOWERPOT -> {
                if (category != AppGroupsManager.Category.FLOWERPOT) {
                    BasePreference(
                        titleId = R.string.tab_manage_apps,
                        summary = summary,
                        startWidget = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_apps),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                        endWidget = {
                            Icon(
                                painter = painterResource(id = R.drawable.chevron_right),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                        index = 0,
                        groupSize = 3
                    ) { openDialog.value = true }
                    Spacer(modifier = Modifier.height(4.dp))
                    BasePreference(
                        titleId = R.string.tab_hide_from_main,
                        startWidget = {
                            Icon(
                                painter = painterResource(id = R.drawable.tab_hide_from_main),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                        endWidget = {
                            Switch(
                                modifier = Modifier
                                    .height(24.dp),
                                checked = isHidden,
                                onCheckedChange = {
                                    isHidden = it
                                }
                            )
                        },
                        onClick = { isHidden = !isHidden },
                        index = 1,
                        groupSize = 3
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (openDialog.value) {
                        BaseDialog(openDialogCustom = openDialog) {
                            Card(
                                shape = MaterialTheme.shapes.extraLarge,
                                modifier = Modifier.padding(8.dp),
                                elevation = CardDefaults.elevatedCardElevation(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                            ) {
                                GroupAppSelection(
                                    selectedApps = selectedApps.map { it.toString() }.toSet(),
                                ) {
                                    val componentsSet =
                                        it.mapNotNull { ck -> ComponentKey.fromString(ck) }
                                            .toMutableSet()
                                    selectedApps.clear()
                                    selectedApps.addAll(componentsSet)
                                    (config[AppGroups.KEY_ITEMS] as? AppGroups.ComponentsCustomization)?.value =
                                        componentsSet
                                }
                            }
                        }
                    }
                } else {
                    BasePreference(
                        titleId = R.string.pref_appcategorization_flowerpot_title,
                        summary = flowerpotManager.getAllPots()
                            .find { it.name == selectedCategory }!!.displayName,
                        startWidget = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_category),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                        endWidget = {
                            Icon(
                                painter = painterResource(id = R.drawable.chevron_right),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                        index = 0
                    ) { openDialog.value = true }
                    Spacer(modifier = Modifier.height(4.dp))

                    if (openDialog.value) {
                        BaseDialog(openDialogCustom = openDialog) {
                            CategorySelectionDialogUI(selectedCategory = selectedCategory) {
                                selectedCategory = it
                                (config[KEY_FLOWERPOT] as? AppGroups.StringCustomization)?.value =
                                    it
                                openDialog.value = false
                            }
                        }
                    }
                }
            }
        }

        if (group.type != DrawerFolders.TYPE_CUSTOM) {
            BasePreference(
                titleId = R.string.tab_color,
                startWidget = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_color_donut),
                        contentDescription = "",
                        modifier = Modifier.size(30.dp),
                        tint = Color(AccentColorOption.fromString(color).accentColor)
                    )
                },
                index = 2,
                groupSize = 3
            ) {
                colorPicker.value = true
            }
            if (colorPicker.value) {
                BaseDialog(openDialogCustom = colorPicker) {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier.padding(8.dp),
                        elevation = CardDefaults.elevatedCardElevation(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                    ) {
                        ColorSelectionDialog(
                            defaultColor = color
                        ) {
                            color = it
                            colorPicker.value = false
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = {
                    onClose(Config.BS_SELECT_TAB_TYPE)
                },
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
            Spacer(modifier = Modifier.width(16.dp))

            OutlinedButton(
                onClick = {
                    (config[AppGroups.KEY_TITLE] as? AppGroups.StringCustomization)?.value =
                        title
                    (config[AppGroups.KEY_HIDE_FROM_ALL_APPS] as? AppGroups.BooleanCustomization)?.value =
                        isHidden
                    (config[AppGroups.KEY_ITEMS] as? AppGroups.ComponentsCustomization)?.value =
                        selectedApps.toMutableSet()
                    if (category != AppGroupsManager.Category.FOLDER) {
                        (config[AppGroups.KEY_COLOR] as? AppGroups.StringCustomization)?.value =
                            color
                    }
                    group.customizations.applyFrom(config)

                    when (category) {
                        AppGroupsManager.Category.FOLDER -> {
                            prefs.drawerAppGroupsManager.drawerFolders.saveToJson()
                        }

                        AppGroupsManager.Category.TAB,
                        AppGroupsManager.Category.FLOWERPOT,
                                                         -> {
                            prefs.drawerAppGroupsManager.drawerTabs.saveToJson()
                            prefs.reloadTabs()
                        }

                        else                             -> {}
                    }
                    onClose(Config.BS_SELECT_TAB_TYPE)
                },
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35F),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.65F)),
            ) {
                Text(text = stringResource(id = R.string.tab_bottom_sheet_save))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
