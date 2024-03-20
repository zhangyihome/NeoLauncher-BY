/*
 * This file is part of Neo Launcher
 * Copyright (c) 2023   Neo Launcher Team
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

import android.util.Log
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.shortcuts.ShortcutKey
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.saggitt.omega.compose.components.ExpandableListItem
import com.saggitt.omega.compose.components.NavBarItem
import com.saggitt.omega.compose.components.TabItem
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.compose.components.preferences.PreferenceGroup
import com.saggitt.omega.compose.navigation.preferenceGraph
import com.saggitt.omega.data.AppItemWithShortcuts
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.gestures.handlers.StartAppGestureHandler
import com.saggitt.omega.preferences.NavigationPref
import com.saggitt.omega.theme.GroupItemShape
import com.saggitt.omega.util.App
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.appsState
import com.saggitt.omega.util.blockBorder
import kotlinx.coroutines.launch
import org.json.JSONObject

fun NavGraphBuilder.gesturesPageGraph(route: String) {
    preferenceGraph(route, { }) { subRoute ->
        composable(
            route = subRoute("{key}"),
            arguments = listOf(
                navArgument("key") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments!!
            val key = stringPreferencesKey(args.getString("key")!!)
            val prefs = Config.gesturePrefs(LocalContext.current)
            Log.d("GestureSelector", "key: $key")
            val gesture = prefs.first { it.key == key }
            GestureSelectorPage(prefs = gesture)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GestureSelectorPage(prefs: NavigationPref) {

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val currentOption = remember { mutableStateOf(prefs.getValue()) }
    val apps = appsState().value
    val tabs = listOf(
        TabItem(R.drawable.ic_assistant, R.string.tab_launcher) {
            LauncherScreen(selectedOption = currentOption, onSelect = {
                prefs.setValue(it)
                backDispatcher?.onBackPressed()
            })
        },
        TabItem(R.drawable.ic_apps, R.string.apps_label) {
            AppsScreen(
                apps = apps,
                selectedOption = currentOption,
                onSelect = {
                    prefs.setValue(it)
                    backDispatcher?.onBackPressed()
                }
            )
        },
        TabItem(R.drawable.ic_edit_dash, R.string.tab_shortcuts) {
            ShortcutsScreen(
                apps = apps,
                selectedOption = currentOption,
                onSelect = {
                    prefs.setValue(it)
                    backDispatcher?.onBackPressed()
                }
            )
        }
    )
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    ViewWithActionBar(
        title = stringResource(prefs.titleId),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = 8.dp,
                    end = 8.dp
                )
        ) {
            HorizontalPager(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .blockBorder(),
                state = pagerState,
            ) { page ->
                tabs[page].screen()
            }
            NavigationBar(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavBarItem(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 16.dp),
                        icon = painterResource(id = tab.icon),
                        labelId = tab.title,
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun LauncherScreen(
    selectedOption: MutableState<String>,
    onSelect: (String) -> Unit,
) {
    val context = LocalContext.current
    val launcherItems = GestureController.getGestureHandlers(
        context = context,
        isSwipeUp = true,
        hasBlank = true
    )

    val groupSize = launcherItems.size
    val colors = RadioButtonDefaults.colors(
        selectedColor = MaterialTheme.colorScheme.primary,
        unselectedColor = MaterialTheme.colorScheme.onPrimary
    )

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        PreferenceGroup {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                itemsIndexed(launcherItems) { index, item ->
                    ListItem(
                        modifier = Modifier
                            .clip(
                                GroupItemShape(index, groupSize - 1)
                            )
                            .clickable {
                                onSelect(item.toString())
                            },
                        colors = ListItemDefaults.colors(
                            containerColor = if (item.toString() == selectedOption.value)
                                MaterialTheme.colorScheme.primary.copy(0.4f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        leadingContent = {
                            Icon(
                                painter = rememberDrawablePainter(drawable = item.icon),
                                contentDescription = item.displayName,
                                modifier = Modifier
                                    .size(32.dp)
                                    .zIndex(1f),
                            )
                        },
                        headlineContent = {
                            Text(
                                text = item.displayName,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        },
                        trailingContent = {
                            RadioButton(
                                selected = (item.toString() == selectedOption.value),
                                onClick = {
                                    onSelect(item.toString())
                                },
                                colors = colors
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun AppsScreen(
    apps: List<App>,
    selectedOption: MutableState<String>,
    onSelect: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        val context = LocalContext.current
        val colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.onPrimary,
            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val groupSize = apps.size

        PreferenceGroup {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                itemsIndexed(apps) { index, item ->
                    val config = JSONObject("{}")
                    config.apply {
                        put("appName", item.label)
                        put("packageName", item.packageName)
                        put("target", item.key)
                        put("type", "app")
                    }

                    val appGestureHandler = StartAppGestureHandler(context, config)
                    appGestureHandler.apply {
                        appName = item.label
                    }
                    ListItem(
                        modifier = Modifier
                            .clip(
                                GroupItemShape(index, groupSize - 1)
                            )
                            .clickable {
                                onSelect(appGestureHandler.toString())
                            },
                        colors = ListItemDefaults.colors(
                            containerColor = if (appGestureHandler.toString() == selectedOption.value)
                                MaterialTheme.colorScheme.primary.copy(0.4f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        leadingContent = {
                            Image(
                                painter = BitmapPainter(item.icon.asImageBitmap()),
                                contentDescription = item.label,
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.size(32.dp),
                            )
                        },
                        headlineContent = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        },
                        trailingContent = {
                            RadioButton(
                                selected = (appGestureHandler.toString() == selectedOption.value),
                                onClick = {
                                    onSelect(appGestureHandler.toString())
                                },
                                colors = colors
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun ShortcutsScreen(
    apps: List<App>,
    selectedOption: MutableState<String>,
    onSelect: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        val context = LocalContext.current
        var appsWithShortcuts by remember { mutableStateOf(emptyList<AppItemWithShortcuts>()) }

        if (apps.isNotEmpty()) {
            appsWithShortcuts = apps
                .map { AppItemWithShortcuts(context, it) }
                .filter { it.hasShortcuts }
        }

        val colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.onPrimary,
            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val appsSize = appsWithShortcuts.size
        PreferenceGroup {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                itemsIndexed(appsWithShortcuts) { appIndex, app ->
                    var expanded by remember { mutableStateOf(false) }

                    ExpandableListItem(
                        modifier = Modifier
                            .clip(
                                GroupItemShape(appIndex, appsSize - 1)
                            )
                            .background(
                                if (expanded) MaterialTheme.colorScheme.background
                                else MaterialTheme.colorScheme.surface
                            ),
                        title = app.info.label,
                        icon = app.info.icon,
                        onClick = { expanded = !expanded }
                    ) {
                        val groupSize = app.shortcuts.size

                        app.shortcuts.forEachIndexed { index, it ->

                            val config = JSONObject("{}")
                            config.apply {
                                put("appName", it.label.toString())
                                put("packageName", it.info.`package`)
                                put("intent", ShortcutKey.makeIntent(it.info).toUri(0))
                                put("user", 0)
                                put("id", it.info.id)
                                put("type", "shortcut")
                            }
                            val appGestureHandler = StartAppGestureHandler(context, config)
                            appGestureHandler.apply {
                                appName = it.label.toString()
                            }
                            ListItem(
                                modifier = Modifier
                                    .clip(
                                        GroupItemShape(index, groupSize - 1)
                                    )
                                    .clickable {
                                        onSelect(appGestureHandler.toString())
                                    },
                                colors = ListItemDefaults.colors(
                                    containerColor = if (appGestureHandler.toString() == selectedOption.value)
                                        MaterialTheme.colorScheme.primary.copy(0.4f)
                                    else MaterialTheme.colorScheme.surface
                                ),
                                leadingContent = {
                                    Image(
                                        painter = if (it.iconDrawable != null) BitmapPainter(
                                            it.iconDrawable.toBitmap(
                                                32,
                                                32,
                                                null
                                            ).asImageBitmap()
                                        ) else painterResource(id = R.drawable.ic_widget),
                                        contentScale = ContentScale.FillBounds,
                                        contentDescription = it.label.toString(),
                                        modifier = Modifier.size(32.dp),
                                    )
                                },
                                headlineContent = {
                                    Text(
                                        text = it.label.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                },
                                trailingContent = {
                                    RadioButton(
                                        selected = (appGestureHandler.toString() == selectedOption.value),
                                        onClick = {
                                            onSelect(appGestureHandler.toString())
                                        },
                                        colors = colors
                                    )
                                }
                            )
                            if (index < groupSize - 1) Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun GestureSelectorPreview() {
    val context = LocalContext.current
    val prefs = Utilities.getNeoPrefs(context)
    GestureSelectorPage(prefs.gestureDoubleTap)
}