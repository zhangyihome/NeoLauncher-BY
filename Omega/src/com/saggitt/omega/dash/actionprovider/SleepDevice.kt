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
import com.android.launcher3.R
import com.saggitt.omega.compose.icons.Phosphor
import com.saggitt.omega.compose.icons.phosphor.Power
import com.saggitt.omega.dash.DashActionProvider
import com.saggitt.omega.gestures.handlers.SleepGestureHandler
import com.saggitt.omega.gestures.handlers.SleepMethodDeviceAdmin
import com.saggitt.omega.gestures.handlers.SleepMethodPieAccessibility

class SleepDevice(context: Context) : DashActionProvider(context) {
    override val itemId = 10
    override val name = context.getString(R.string.action_sleep)
    override val description = context.getString(R.string.action_sleep)
    override val icon = Phosphor.Power

    private val method: SleepGestureHandler.SleepMethod? by lazy {
        listOf(
            SleepMethodPieAccessibility(context),
            SleepMethodDeviceAdmin(context)
        ).firstOrNull { it.supported }
    }

    override fun runAction(context: Context) {
        method!!.sleep(null)
    }
}