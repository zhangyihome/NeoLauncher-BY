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

package com.saggitt.omega.gestures.handlers

import android.accessibilityservice.AccessibilityService
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.view.View
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.android.launcher3.R
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.gestures.GestureHandler
import com.saggitt.omega.neoApp
import org.json.JSONObject
@Keep
@TargetApi(Build.VERSION_CODES.P)
open class PressBackGestureHandler(context: Context, config: JSONObject?) :
    GestureHandler(context, config) {

    override val displayName: String = context.getString(R.string.gesture_press_back)
    override val displayNameRes = R.string.gesture_press_back
    override val icon = ContextCompat.getDrawable(context, R.drawable.ic_arrow_back)

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        context.neoApp.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    }

    override fun isAvailableForSwipeUp(isSwipeUp: Boolean) = isSwipeUp
}
