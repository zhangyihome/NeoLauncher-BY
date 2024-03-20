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
package com.saggitt.omega.dash.actionprovider

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.android.launcher3.R
import com.saggitt.omega.compose.icons.Phosphor
import com.saggitt.omega.compose.icons.phosphor.ImageSquare
import com.saggitt.omega.dash.DashActionProvider

class ChangeWallpaper(context: Context) : DashActionProvider(context) {
    override val itemId = 3
    override val name = context.getString(R.string.wallpaper_pick)
    override val description = context.getString(R.string.wallpaper_pick_summary)
    override val icon = Phosphor.ImageSquare

    override fun runAction(context: Context) {
        try {
            context.startActivity(
                Intent.createChooser(
                    Intent(Intent.ACTION_SET_WALLPAPER),
                    context.getString(R.string.wallpaper_pick)
                )
            )
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.activity_not_found, Toast.LENGTH_SHORT).show()
        }
    }
}