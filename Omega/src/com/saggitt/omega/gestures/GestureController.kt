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

package com.saggitt.omega.gestures

import android.content.Context
import android.graphics.PointF
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import com.android.launcher3.util.TouchController
import com.saggitt.omega.NeoLauncher
import com.saggitt.omega.gestures.gestures.DoubleTapGesture
import com.saggitt.omega.gestures.gestures.LongPressGesture
import com.saggitt.omega.gestures.gestures.PressBackGesture
import com.saggitt.omega.gestures.gestures.PressHomeGesture
import com.saggitt.omega.gestures.gestures.VerticalSwipeGesture
import com.saggitt.omega.gestures.handlers.NotificationsOpenGestureHandler
import com.saggitt.omega.gestures.handlers.OpenDashGestureHandler
import com.saggitt.omega.gestures.handlers.OpenDrawerGestureHandler
import com.saggitt.omega.gestures.handlers.OpenOverlayGestureHandler
import com.saggitt.omega.gestures.handlers.OpenOverviewGestureHandler
import com.saggitt.omega.gestures.handlers.OpenSettingsGestureHandler
import com.saggitt.omega.gestures.handlers.OpenWidgetsGestureHandler
import com.saggitt.omega.gestures.handlers.PressBackGestureHandler
import com.saggitt.omega.gestures.handlers.SleepGestureHandler
import com.saggitt.omega.gestures.handlers.StartGlobalSearchGestureHandler
import org.json.JSONException
import org.json.JSONObject

class GestureController(val launcher: NeoLauncher) : TouchController {

    val blankGestureHandler = BlankGestureHandler(launcher, null)
    private val doubleTapGesture by lazy { DoubleTapGesture(this) }
    private val pressHomeGesture by lazy { PressHomeGesture(this) }
    private val pressBackGesture by lazy { PressBackGesture(this) }
    private val longPressGesture by lazy { LongPressGesture(this) }

    //private val assistantGesture by lazy { LaunchAssistantGesture(this) }
    val verticalSwipeGesture by lazy { VerticalSwipeGesture(this) }

    var touchDownPoint = PointF()

    private var swipeUpOverride: Pair<GestureHandler, Long>? = null

    override fun onControllerInterceptTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

    override fun onControllerTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

    fun attachDoubleTapListener(gestureDetector: GestureDetector) {
        gestureDetector.setOnDoubleTapListener(doubleTapGesture.createDoubleTapListener())
    }

    fun onLongPress() {
        longPressGesture.isEnabled && longPressGesture.onEvent()
    }

    fun onPressHome() {
        pressHomeGesture.isEnabled && pressHomeGesture.onEvent()
    }

    fun onPressBack() {
        pressBackGesture.isEnabled && pressBackGesture.onEvent()
    }

    /*fun onLaunchAssistant() {
        assistantGesture.isEnabled && assistantGesture.onEvent()
    }*/

    fun setSwipeUpOverride(handler: GestureHandler, downTime: Long) {
        if (swipeUpOverride?.second != downTime) {
            swipeUpOverride = Pair(handler, downTime)
        }
    }

    fun getSwipeUpOverride(downTime: Long): GestureHandler? {
        swipeUpOverride?.let {
            if (it.second == downTime) {
                return it.first
            } else {
                swipeUpOverride = null
            }
        }
        return null
    }


    fun createGestureHandler(jsonString: String) =
        createGestureHandler(launcher, jsonString, blankGestureHandler)

    companion object {
        private const val TAG = "GestureController"
        private val LEGACY_SLEEP_HANDLERS = listOf(
            "com.saggitt.omega.gestures.handlers.SleepGestureHandlerDeviceAdmin",
            "com.saggitt.omega.gestures.handlers.SleepGestureHandlerAccessibility"
        )

        fun createGestureHandler(
            context: Context,
            jsonString: String?,
            fallback: GestureHandler,
        ): GestureHandler {
            if (!jsonString.isNullOrEmpty()) {
                val config: JSONObject? = try {
                    JSONObject(jsonString ?: "{ }")
                } catch (e: JSONException) {
                    null
                }
                var className = config?.getString("class") ?: jsonString
                if (className in LEGACY_SLEEP_HANDLERS) {
                    className = SleepGestureHandler::class.java.name
                }
                val configValue =
                    if (config?.has("config") == true) config.getJSONObject("config") else null
                try {
                    val handler = Class.forName(className)
                        .getConstructor(Context::class.java, JSONObject::class.java)
                        .newInstance(context, configValue) as GestureHandler
                    if (handler.isAvailable) return handler
                } catch (t: Throwable) {
                    Log.e(TAG, "can't create gesture handler", t)
                }
            }
            return fallback
        }

        fun getClassName(jsonString: String): String {
            val config: JSONObject? = try {
                JSONObject(jsonString)
            } catch (e: JSONException) {
                null
            }
            val className = config?.getString("class") ?: jsonString
            return if (className in LEGACY_SLEEP_HANDLERS) {
                SleepGestureHandler::class.java.name
            } else {
                className
            }
        }

        fun getGestureHandlers(context: Context, isSwipeUp: Boolean, hasBlank: Boolean) =
            mutableListOf(
                PressBackGestureHandler(context, null),
                SleepGestureHandler(context, null),
                OpenDashGestureHandler(context, null),
                OpenDrawerGestureHandler(context, null),
                OpenWidgetsGestureHandler(context, null),
                NotificationsOpenGestureHandler(context, null),
                OpenOverlayGestureHandler(context, null),
                OpenOverviewGestureHandler(context, null),
                StartGlobalSearchGestureHandler(context, null),
                OpenSettingsGestureHandler(context, null),
            ).apply {
                if (hasBlank) {
                    add(0, BlankGestureHandler(context, null))
                }
            }.filter { it.isAvailableForSwipeUp(isSwipeUp) }
    }
}
