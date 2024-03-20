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

package com.saggitt.omega.folder

import android.content.Context
import com.android.launcher3.R
import com.android.launcher3.model.ModelWriter
import com.android.launcher3.model.data.FolderInfo
import com.android.launcher3.model.data.ItemInfo
import com.saggitt.omega.util.SingletonHolder
import com.saggitt.omega.util.ensureOnMainThread
import com.saggitt.omega.util.useApplicationContext

class FolderInfoProvider(context: Context) : CustomInfoProvider<FolderInfo>(context) {

    override fun getTitle(info: FolderInfo): String {
        return if (info.title == null || info.title == "")
            getDefaultTitle(info)
        else
            info.title.toString()
    }

    override fun getDefaultTitle(info: FolderInfo): String =
        context.getString(R.string.folder_hint_text)

    override fun getCustomTitle(info: FolderInfo): String = if (info.title == null) ""
    else info.title.toString()

    override fun setTitle(info: FolderInfo, title: String?, modelWriter: ModelWriter) {
        info.setTitle(title ?: "", modelWriter)
    }

    override fun supportsSwipeUp(info: FolderInfo) = info.container != ItemInfo.NO_ID

    override fun supportsIcon() = true

    override fun setSwipeUpAction(info: FolderInfo, action: String?) {
        info.setSwipeUpAction(context, action)
    }

    fun isDisabled(): Boolean {
        return false
    }

    override fun getSwipeUpAction(info: FolderInfo): String? = info.swipeUpAction

    companion object : SingletonHolder<FolderInfoProvider, Context>(
        ensureOnMainThread(
            useApplicationContext(::FolderInfoProvider)
        )
    )
}
