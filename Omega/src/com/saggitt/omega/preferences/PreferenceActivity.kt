/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
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

package com.saggitt.omega.preferences

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.saggitt.omega.compose.navigation.PrefsComposeView
import com.saggitt.omega.theme.OmegaAppTheme
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.theme.ThemeOverride
import com.saggitt.omega.util.prefs
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PreferenceActivity : AppCompatActivity(), ThemeManager.ThemeableActivity {
    private lateinit var navController: NavHostController
    override var currentTheme = 0
    override var currentAccent = 0
    private lateinit var themeOverride: ThemeOverride
    private val themeSet: ThemeOverride.ThemeSet get() = ThemeOverride.Settings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeOverride = ThemeOverride(themeSet, this)
        themeOverride.applyTheme(this)
        currentTheme = themeOverride.getTheme(this)
        currentAccent = prefs.profileAccentColor.getColor()
        setContent {
            OmegaAppTheme {
                navController = rememberNavController()
                PrefsComposeView(navController)
            }
        }
    }

    override fun onThemeChanged(forceUpdate: Boolean) = recreate()

    companion object {
        fun createIntent(context: Context, destination: String): Intent {
            val uri = "android-app://androidx.navigation//$destination".toUri()
            return Intent(Intent.ACTION_VIEW, uri, context, PreferenceActivity::class.java)
        }

        suspend fun startBlankActivityDialog(
            activity: Activity, targetIntent: Intent,
            dialogTitle: String, dialogMessage: String,
            positiveButton: String,
        ) {
            start(activity, targetIntent, Bundle().apply {
                putParcelable("intent", targetIntent)
                putString("dialogTitle", dialogTitle)
                putString("dialogMessage", dialogMessage)
                putString("positiveButton", positiveButton)
            })
        }

        suspend fun startBlankActivityForResult(
            activity: Activity,
            targetIntent: Intent,
        ): ActivityResult {
            return start(activity, targetIntent, Bundle.EMPTY)
        }

        private suspend fun start(
            activity: Activity,
            targetIntent: Intent,
            extras: Bundle,
        ): ActivityResult {
            return suspendCoroutine { continuation ->
                val intent = Intent(activity, PreferenceActivity::class.java)
                intent.putExtras(extras)
                intent.putExtra("intent", targetIntent)
                val resultReceiver = createResultReceiver {
                    continuation.resume(it)
                }
                activity.startActivity(intent.putExtra("callback", resultReceiver))
            }
        }

        private fun createResultReceiver(callback: (ActivityResult) -> Unit): ResultReceiver {
            return object : ResultReceiver(Handler(Looper.myLooper()!!)) {

                override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                    val data = Intent()
                    if (resultData != null) {
                        data.putExtras(resultData)
                    }
                    callback(ActivityResult(resultCode, data))
                }
            }
        }
    }
}