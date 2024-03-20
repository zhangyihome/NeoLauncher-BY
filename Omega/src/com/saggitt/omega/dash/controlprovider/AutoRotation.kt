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
package com.saggitt.omega.dash.controlprovider

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.android.launcher3.R
import com.saggitt.omega.compose.icons.PhosphorCustom
import com.saggitt.omega.compose.icons.phosphor.Rotate
import com.saggitt.omega.dash.DashControlProvider

class AutoRotation(context: Context) : DashControlProvider(context) {
    override val itemId = 12
    override val name = context.getString(R.string.dash_auto_rotation)
    override val description = context.getString(R.string.dash_auto_rotation_summary)
    override val extendable = false
    override val icon = PhosphorCustom.Rotate

    override var state: Boolean
        get() =
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION
            ) == 1
        set(value) {
            if (Settings.System.canWrite(context)) {
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.ACCELEROMETER_ROTATION,
                    if (value) 1 else 0
                )
            } else {
                AlertDialog.Builder(context)
                    .setTitle(R.string.modify_system_settings)
                    .setMessage(R.string.modify_system_settings_message)
                    .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                        context.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS))
                    }
                    .setNegativeButton(android.R.string.cancel) { di: DialogInterface, _: Int ->
                        di.dismiss()
                    }
                    .show()
            }
        }
}