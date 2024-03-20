/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Omega Launcher Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.popup

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.net.Uri
import android.view.View
import android.widget.Toast
import com.android.launcher3.AbstractFloatingView
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
import com.android.launcher3.LauncherState
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.icons.BitmapInfo
import com.android.launcher3.model.data.FolderInfo
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.model.data.ItemInfoWithIcon
import com.android.launcher3.model.data.ItemInfoWithIcon.FLAG_SYSTEM_YES
import com.android.launcher3.model.data.LauncherAppWidgetInfo
import com.android.launcher3.model.data.WorkspaceItemInfo
import com.android.launcher3.popup.SystemShortcut
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.NeoLauncher
import com.saggitt.omega.compose.components.ComposeBottomSheet
import com.saggitt.omega.icons.CustomizeIconPage
import com.saggitt.omega.preferences.NeoPrefs
import com.saggitt.omega.util.hasFlag
import com.saggitt.omega.util.hasFlags
import java.net.URISyntaxException
import com.android.launcher3.model.data.AppInfo as ModelAppInfo

class OmegaShortcuts {
    class Customize(
        private val launcher: NeoLauncher,
        private val appInfo: ModelAppInfo,
        itemInfo: ItemInfo,
        originalView: View,
    ) : SystemShortcut<NeoLauncher>(
        R.drawable.ic_edit_no_shadow,
        R.string.action_preferences, launcher, itemInfo,
        originalView
    ) {

        val prefs: NeoPrefs = NeoPrefs.getInstance(launcher)
        override fun onClick(v: View?) {
            val outObj = Array<Any?>(1) { null }
            var icon = Utilities.loadFullDrawableWithoutTheme(launcher, appInfo, 0, 0, outObj)
            if (mItemInfo.screenId != NO_ID && icon is BitmapInfo.Extender) {
                icon = icon.getThemedDrawable(launcher)
            }

            val launcherActivityInfo = outObj[0] as LauncherActivityInfo
            val defaultTitle = launcherActivityInfo.label.toString()

            if (launcher.isInState(LauncherState.ALL_APPS)) {
                if (prefs.drawerPopupEdit) {
                    AbstractFloatingView.closeAllOpenViews(mTarget)
                    ComposeBottomSheet.show(launcher) {
                        CustomizeIconPage(
                            defaultTitle = defaultTitle,
                            componentKey = appInfo.toComponentKey(),
                            appInfo = appInfo,
                            onClose = { close(true) }
                        )
                    }
                }
            } else {
                if (prefs.desktopPopupEdit && !prefs.desktopLock.getValue()) {
                    AbstractFloatingView.closeAllOpenViews(mTarget)
                    ComposeBottomSheet.show(launcher) {
                        CustomizeIconPage(
                            defaultTitle = defaultTitle,
                            componentKey = appInfo.toComponentKey(),
                            appInfo = appInfo,
                            onClose = { close(true) }
                        )
                    }
                }
            }
        }
    }

    class AppRemove(
        private val launcher: NeoLauncher,
        itemInfo: ItemInfo,
        originalView: View,
    ) : SystemShortcut<NeoLauncher>(
        R.drawable.ic_remove_no_shadow,
        R.string.remove_drop_target_label, launcher, itemInfo,
        originalView
    ) {

        override fun onClick(v: View?) {
            dismissTaskMenuView(mTarget)
            launcher.removeItem(v, mItemInfo, true)
            launcher.workspace.stripEmptyScreens()
            launcher.model.forceReload()
        }
    }

    class AppUninstall(
        private val launcher: NeoLauncher,
        itemInfo: ItemInfo,
        originalView: View,
    ) : SystemShortcut<NeoLauncher>(
        R.drawable.ic_uninstall_no_shadow,
        R.string.uninstall_drop_target_label, launcher, itemInfo, originalView
    ) {

        override fun onClick(v: View?) {
            AbstractFloatingView.closeAllOpenViews(mTarget)
            try {
                val componentName: ComponentName? = getUninstallTarget(launcher, mItemInfo)
                val i: Intent =
                    Intent.parseUri(mTarget.getString(R.string.delete_package_intent), 0)
                        .setData(
                            Uri.fromParts(
                                "package",
                                componentName?.packageName,
                                componentName?.className
                            )
                        )
                        .putExtra(Intent.EXTRA_USER, mItemInfo.user)
                mTarget.startActivity(i)
            } catch (e: URISyntaxException) {
                Toast.makeText(launcher, R.string.uninstall_failed, Toast.LENGTH_SHORT).show()
            }
        }

        private fun getUninstallTarget(launcher: Launcher, item: ItemInfo): ComponentName? {
            if (item.itemType == ITEM_TYPE_APPLICATION && item.id == NO_ID) {
                val intent = item.intent
                val user = item.user
                if (intent != null) {
                    val info = launcher
                        .getSystemService(LauncherApps::class.java)
                        .resolveActivity(intent, user)
                    if (info != null && !info.applicationInfo.flags.hasFlag(ApplicationInfo.FLAG_SYSTEM))
                        return info.componentName
                }
            } else {
                return item.targetComponent
            }
            return null
        }
    }

    companion object {
        val CUSTOMIZE = SystemShortcut.Factory<NeoLauncher> { activity, itemInfo, originalView ->
            val prefs = NeoPrefs.getInstance(activity)
            var customize: Customize? = null
            if (Launcher.getLauncher(activity).isInState(LauncherState.NORMAL)) {
                if (prefs.desktopPopupEdit && !prefs.desktopLock.getValue()) {
                    getAppInfo(activity, itemInfo)?.let {
                        customize = Customize(activity, it, itemInfo, originalView)
                    }
                }
            } else {
                if (prefs.drawerPopupEdit) {
                    getAppInfo(activity, itemInfo)?.let {
                        customize = Customize(activity, it, itemInfo, originalView)
                    }
                }
            }
            customize
        }

        private fun getAppInfo(launcher: NeoLauncher, itemInfo: ItemInfo): ModelAppInfo? {
            if (itemInfo is ModelAppInfo) return itemInfo
            if (itemInfo.itemType != ITEM_TYPE_APPLICATION) return null
            val key = ComponentKey(itemInfo.targetComponent, itemInfo.user)
            return launcher.appsView.appsStore.getApp(key)
        }

        val APP_REMOVE = SystemShortcut.Factory<NeoLauncher> { launcher, itemInfo, originalView ->
            val prefs = NeoPrefs.getInstance(launcher)
            var appRemove: AppRemove? = null
            if (Launcher.getLauncher(launcher).isInState(LauncherState.NORMAL)) {
                if (itemInfo is WorkspaceItemInfo
                    || itemInfo is LauncherAppWidgetInfo
                    || itemInfo is FolderInfo
                ) {
                    if (prefs.desktopPopupRemove && !prefs.desktopLock.getValue()
                    ) {
                        appRemove = AppRemove(launcher, itemInfo, originalView)
                    }
                }
            }
            appRemove
        }

        val APP_UNINSTALL =
            SystemShortcut.Factory<NeoLauncher> { launcher, itemInfo, originalView ->
                val prefs = NeoPrefs.getInstance(launcher)
                var appUninstall: AppUninstall? = null

                if (prefs.drawerPopupUninstall && launcher.isInState(LauncherState.ALL_APPS)) {

                    if (itemInfo is ItemInfoWithIcon) {
                        if (!itemInfo.runtimeStatusFlags.hasFlags(FLAG_SYSTEM_YES)) {
                            appUninstall = AppUninstall(launcher, itemInfo, originalView)
                        }
                    }
                }
                appUninstall
            }
    }
}