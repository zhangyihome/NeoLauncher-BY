/*
 * This file is part of Omega Launcher
 * Copyright (c) 2023   Omega Launcher Team
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
package com.saggitt.omega.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import com.android.launcher3.R
import com.google.android.material.button.MaterialButton
import com.saggitt.omega.util.getColorAttr

class ColoredButton(context: Context, attrs: AttributeSet) : MaterialButton(context, attrs) {
    var color: Int = 0

    private val defaultColor = context.getColorAttr(R.attr.colorOnSurface)
    private val selectedColor = context.getColorAttr(R.attr.colorSurface)
    private val defaultBackgroundColor = context.getColorAttr(R.attr.popupColorPrimary)

    fun refreshColor() {
        setTextColor()
        setBackgroundColor()
    }

    private fun setTextColor() {
        val stateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_selected),
                intArrayOf()
            ),
            intArrayOf(
                selectedColor,
                defaultColor
            )
        )
        setTextColor(stateList)
    }

    private fun setBackgroundColor() {
        val stateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_selected),
                intArrayOf()
            ),
            intArrayOf(
                color,
                defaultBackgroundColor
            )
        )
        backgroundTintList = stateList
    }
}