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

package com.saggitt.omega.groups.category

import com.android.launcher3.AbstractFloatingView
import com.android.launcher3.BaseDraggingActivity
import com.android.launcher3.Launcher
import com.android.launcher3.allapps.AllAppsStore
import com.android.launcher3.model.ModelWriter
import com.android.launcher3.model.data.FolderInfo
import com.saggitt.omega.compose.components.ComposeBottomSheet
import com.saggitt.omega.groups.ui.EditGroupBottomSheet
import com.saggitt.omega.preferences.NeoPrefs
import com.saggitt.omega.util.prefs

class DrawerFolderInfo(private val drawerFolder: DrawerFolders.Folder) : FolderInfo() {

    private var changed = false
    lateinit var appsStore: AllAppsStore<BaseDraggingActivity>

    override fun setTitle(title: CharSequence?, modelWriter: ModelWriter?) {
        super.setTitle(title, modelWriter)
        changed = true
        drawerFolder.title = title.toString()
    }

    override fun onIconChanged() {
        super.onIconChanged()
        drawerFolder.context.prefs.withChangeCallback {
            it.reloadGrid()
        }
    }

    fun onCloseComplete() {
        if (changed) {
            changed = false
            drawerFolder.context.prefs.drawerAppGroupsManager.drawerFolders.saveToJson()
        }
    }

    fun showEdit(launcher: Launcher) {
        val prefs = NeoPrefs.getInstance(launcher)
        ComposeBottomSheet.show(launcher) {
            EditGroupBottomSheet(
                category = prefs.drawerAppGroupsManager.getEnabledType()!!,
                group = drawerFolder,
                onClose = { AbstractFloatingView.closeAllOpenViews(launcher) }
            )
        }
    }
}
