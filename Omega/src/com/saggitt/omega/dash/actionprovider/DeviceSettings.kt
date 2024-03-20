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

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.android.launcher3.R
import com.saggitt.omega.compose.icons.Phosphor
import com.saggitt.omega.compose.icons.phosphor.GearSix
import com.saggitt.omega.dash.DashActionProvider

class DeviceSettings(context: Context) : DashActionProvider(context) {
    override val itemId = 4
    override val name = context.getString(R.string.dash_device_settings_title)
    override val description = context.getString(R.string.dash_device_settings_summary)
    override val icon = Phosphor.GearSix

    override fun runAction(context: Context) {
        context.startActivity(Intent(Settings.ACTION_SETTINGS))
    }
}