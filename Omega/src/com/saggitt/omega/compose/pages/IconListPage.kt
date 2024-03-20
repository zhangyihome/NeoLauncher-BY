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

import SearchTextField
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.Process
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.android.launcher3.R
import com.google.accompanist.insets.ui.LocalScaffoldPadding
import com.saggitt.omega.compose.components.LazyGridLayout
import com.saggitt.omega.compose.components.MutablePaddingValues
import com.saggitt.omega.compose.components.OverflowMenu
import com.saggitt.omega.compose.components.PreferenceLazyColumn
import com.saggitt.omega.compose.components.SearchBarUI
import com.saggitt.omega.compose.components.preferences.PreferenceGroupDescription
import com.saggitt.omega.compose.components.verticalGridItems
import com.saggitt.omega.compose.navigation.preferenceGraph
import com.saggitt.omega.compose.navigation.resultSender
import com.saggitt.omega.data.models.IconPickerItem
import com.saggitt.omega.iconpack.CustomIconPack
import com.saggitt.omega.iconpack.IconPack
import com.saggitt.omega.iconpack.IconPackProvider
import com.saggitt.omega.iconpack.filter
import com.saggitt.omega.util.getIcon
import com.saulhdev.neolauncher.icons.drawableToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/*
* List Icons from a given IconPack
 */
@Composable
fun IconListPage(
    iconPackName: String,
) {
    val context = LocalContext.current
    var iconPackage = ""
    if (iconPackName != "system_icons") {
        iconPackage = iconPackName
    }

    val iconPack = remember {
        IconPackProvider.INSTANCE.get(context).getIconPackOrSystem(iconPackage)
    }
    if (iconPack == null) {
        val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
        SideEffect {
            backDispatcher?.onBackPressed()
        }
        return
    }
    var searchQuery by remember { mutableStateOf("") }
    val onClickItem = resultSender<IconPickerItem>()
    val pickerComponent = remember {
        val launcherApps = context.getSystemService<LauncherApps>()!!
        launcherApps
            .getActivityList(iconPack.packPackageName, Process.myUserHandle())
            .firstOrNull()?.componentName
    }
    val pickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val icon = it.data?.getParcelableExtra<Intent.ShortcutIconResource>(
                Intent.EXTRA_SHORTCUT_ICON_RESOURCE
            ) ?: return@rememberLauncherForActivityResult
            val entry = (iconPack as CustomIconPack).createFromExternalPicker(icon)
                ?: return@rememberLauncherForActivityResult
            onClickItem(entry)
        }
    Column {
        SearchBarUI(
            searchInput = {
                SearchTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxSize(),
                    placeholder = {
                        Text(
                            text = iconPack.label,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.medium),
                        )
                    },
                    singleLine = true
                )
            },
            actions = {
                if (pickerComponent != null) {
                    OverflowMenu {
                        DropdownMenuItem(
                            onClick = {
                                val intent = Intent("com.novalauncher.THEME")
                                    .addCategory("com.novalauncher.category.CUSTOM_ICON_PICKER")
                                    .setComponent(pickerComponent)
                                pickerLauncher.launch(intent)
                                hideMenu()
                            },
                            text = { Text(text = stringResource(id = R.string.icon_pack_external_picker)) }
                        )
                    }
                }
            }
        ) {
            val scaffoldPadding = LocalScaffoldPadding.current
            val innerPadding = remember { MutablePaddingValues() }
            val layoutDirection = LocalLayoutDirection.current
            innerPadding.left = scaffoldPadding.calculateLeftPadding(layoutDirection)
            innerPadding.right = scaffoldPadding.calculateRightPadding(layoutDirection)
            innerPadding.bottom = scaffoldPadding.calculateBottomPadding()

            val topPadding = scaffoldPadding.calculateTopPadding()

            CompositionLocalProvider(LocalScaffoldPadding provides innerPadding) {
                IconPickerGrid(
                    iconPack = iconPack,
                    searchQuery = searchQuery,
                    onClickItem = onClickItem,
                    modifier = Modifier
                        .padding(top = topPadding)
                )
            }
            Spacer(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth()
                    .height(topPadding)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IconPickerGrid(
    iconPack: IconPack,
    searchQuery: String,
    onClickItem: (item: IconPickerItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    var loadFailed by remember { mutableStateOf(false) }
    val categoriesFlow = remember {
        iconPack.getAllIcons()
            .catch { loadFailed = true }
    }
    val categories by categoriesFlow.collectAsState(emptyList())
    val filteredCategories by remember(searchQuery) {
        derivedStateOf {
            categories.asSequence()
                .map { it.filter(searchQuery) }
                .filter { it.items.isNotEmpty() }
                .toList()
        }
    }

    val density = LocalDensity.current
    val gridLayout = remember {
        LazyGridLayout(
            minWidth = 56.dp,
            gapWidth = 16.dp,
            density = density
        )
    }
    val numColumns by gridLayout.numColumns
    PreferenceLazyColumn(modifier = modifier.then(gridLayout.onSizeChanged())) {
        if (numColumns != 0) {
            filteredCategories.forEach { category ->
                stickyHeader {
                    Text(
                        text = category.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                verticalGridItems(
                    modifier = Modifier
                        .padding(horizontal = 8.dp),
                    items = category.items,
                    numColumns = numColumns,
                ) { _, item ->
                    IconPreview(
                        iconPack = iconPack,
                        iconItem = item,
                        onClick = {
                            onClickItem(item)
                        }
                    )
                }
            }
        }
        if (loadFailed) {
            item {
                PreferenceGroupDescription(
                    description = stringResource(id = R.string.icon_picker_load_failed)
                )
            }
        }
    }
}

@Composable
fun IconPreview(
    iconPack: IconPack,
    iconItem: IconPickerItem,
    onClick: () -> Unit,
) {
    val drawable by produceState<Drawable?>(initialValue = null, iconPack, iconItem) {
        launch(Dispatchers.IO) {
            value = iconPack.getIcon(iconItem.toIconEntry(), 0)
        }
    }
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        Image(
            bitmap = drawableToBitmap(drawable ?: LocalContext.current.getIcon()).asImageBitmap(),
            contentDescription = iconItem.drawableName,
            modifier = Modifier.aspectRatio(1f),
        )
    }
}

fun NavGraphBuilder.iconPickerGraph(route: String) {
    preferenceGraph(route, {
        IconListPage(iconPackName = "")
    }) { subRoute ->
        composable(
            route = subRoute("{iconPackName}"),
            arguments = listOf(
                navArgument("iconPackName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments!!
            val packageName = args.getString("iconPackName")!!
            IconListPage(packageName)
        }
    }
}