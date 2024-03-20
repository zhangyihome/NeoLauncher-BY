/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.saggitt.omega

import android.content.Context
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherAnimUtils
import com.android.launcher3.LauncherState
import com.android.launcher3.logging.StatsLogManager.LAUNCHER_STATE_OPTIONS
import com.android.launcher3.views.ActivityContext
import kotlin.math.max


class OptionsState(id: Int) :
    LauncherState(id, LAUNCHER_STATE_OPTIONS, STATE_FLAGS) {
    override fun <DEVICE_PROFILE_CONTEXT> getTransitionDuration(
        context: DEVICE_PROFILE_CONTEXT,
        isToState: Boolean
    ): Int where DEVICE_PROFILE_CONTEXT : Context?, DEVICE_PROFILE_CONTEXT : ActivityContext? {
        return LauncherAnimUtils.SPRING_LOADED_EXIT_DELAY
    }

    override fun getWorkspaceScaleAndTranslation(launcher: Launcher): ScaleAndTranslation {
        val grid = launcher.deviceProfile
        val ws = launcher.workspace

        val scale = 85f / 100f
        if (grid.isVerticalBarLayout) {
            val optionsView = launcher.nLauncher.optionsView

            val wsHeightWithoutInsets = ws.height - grid.insets.top - grid.insets.bottom
            val desiredCenter = wsHeightWithoutInsets * 0.5f * scale
            val actualCenter = wsHeightWithoutInsets * 0.5f

            val desiredBottom = grid.heightPx - optionsView.height
            val actualBottom = ws.height * 0.5f + (ws.height * 0.5f * scale)

            return ScaleAndTranslation(
                scale, 0f, max(
                    desiredCenter - actualCenter,
                    desiredBottom - actualBottom
                )
            )
        }

        val insets = launcher.dragLayer.insets

        val scaledHeight = scale * ws.normalChildHeight
        val shrunkTop = (insets.top + grid.dropTargetBarSizePx).toFloat()
        val shrunkBottom = (ws.measuredHeight - insets.bottom
                - grid.workspacePadding.bottom).toFloat()
        val totalShrunkSpace = shrunkBottom - shrunkTop

        val desiredCellTop = shrunkTop + (totalShrunkSpace - scaledHeight) / 2

        val halfHeight = (ws.height / 2).toFloat()
        val myCenter = ws.top + halfHeight
        val cellTopFromCenter = halfHeight - ws.getChildAt(0).top
        val actualCellTop = myCenter - cellTopFromCenter * scale
        return ScaleAndTranslation(scale, 0f, (desiredCellTop - actualCellTop) / scale)
    }

    override fun getVisibleElements(launcher: Launcher): Int = OPTIONS_VIEW

    companion object {
        private val STATE_FLAGS: Int by lazy { FLAG_MULTI_PAGE or FLAG_DISABLE_RESTORE }
    }
}