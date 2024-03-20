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

package com.saggitt.omega.smartspace.weather

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.smartspace.model.SmartspaceScores
import com.saggitt.omega.smartspace.model.WeatherData
import com.saggitt.omega.smartspace.provider.SmartspaceDataSource
import com.saggitt.omega.smartspace.weather.GoogleWeatherProvider.Companion.dummyTarget
import com.saggitt.omega.widget.Temperature
import com.saulhdev.smartspace.SmartspaceAction
import com.saulhdev.smartspace.SmartspaceTarget
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit

class PixelWeatherProvider(context: Context) : SmartspaceDataSource(
    context, R.string.weather_provider_pe
) {
    override val isAvailable: Boolean
    override val disabledTargets = listOf(dummyTarget)
    override lateinit var internalTargets: Flow<List<SmartspaceTarget>>
    private val contentResolver = context.contentResolver
    private var weatherData: WeatherData? = null

    init {
        isAvailable = isAvailable(context)
        internalTargets = if (isAvailable) {
            flow {
                while (true) {
                    updateData()
                    emit(updateWeatherData())
                    delay(TimeUnit.MINUTES.toMillis(30))
                }
            }
        } else {
            listOf(disabledTargets).asFlow()
        }
    }

    private fun updateWeatherData(): List<SmartspaceTarget> {
        if (weatherData != null) {
            Log.d("PixelWeatherProvider", "Updating weather data " + weatherData?.getTitle())
            val target = SmartspaceTarget(
                smartspaceTargetId = "PixelWeatherProvider",
                headerAction = SmartspaceAction(
                    id = "PixelWeatherProvider",
                    icon = Icon.createWithBitmap(weatherData!!.icon),
                    title = "",
                    subtitle = weatherData?.getTitle(Temperature.unitFromString(prefs.smartspaceWeatherUnit.getValue())),
                    pendingIntent = weatherData?.pendingIntent
                ),
                score = SmartspaceScores.SCORE_WEATHER,
                featureType = SmartspaceTarget.FEATURE_WEATHER,
            )
            return listOf(target)
        } else {
            return disabledTargets
        }

    }

    private fun updateData() {
        contentResolver.query(weatherUri, PROJECTION_DEFAULT_WEATHER, null, null, null)
            ?.use { cursor ->
                val count = cursor.count
                if (count > 0) {
                    cursor.moveToPosition(0)
                    val status = cursor.getInt(0)
                    if (status == 0) {
                        val conditions = cursor.getString(1)
                        val temperature = cursor.getInt(2)
                        weatherData = WeatherData(
                            getConditionIcon(conditions),
                            Temperature(temperature, Temperature.Unit.Celsius), ""
                        )
                    }
                }
            }
    }

    @SuppressLint("DiscouragedApi")
    private fun getConditionIcon(condition: String): Bitmap {
        val resName = when (condition) {
            "partly-cloudy" -> "weather_partly_cloudy"
            "partly-cloudy-night" -> "weather_partly_cloudy_night"
            "mostly-cloudy" -> "weather_mostly_cloudy"
            "mostly-cloudy-night" -> "weather_mostly_cloudy_night"
            "cloudy" -> "weather_cloudy"
            "clear-night" -> "weather_clear_night"
            "mostly-clear-night" -> "weather_mostly_clear_night"
            "sunny" -> "weather_sunny"
            "mostly-sunny" -> "weather_mostly_sunny"
            "scattered-showers" -> "weather_scattered_showers"
            "scattered-showers-night" -> "weather_scattered_showers_night"
            "rain" -> "weather_rain"
            "windy" -> "weather_windy"
            "snow" -> "weather_snow"
            "scattered-thunderstorms" -> "weather_isolated_scattered_thunderstorms"
            "scattered-thunderstorms-night" -> "weather_isolated_scattered_thunderstorms_night"
            "isolated-thunderstorms" -> "weather_isolated_scattered_thunderstorms"
            "isolated-thunderstorms-night" -> "weather_isolated_scattered_thunderstorms_night"
            "thunderstorms" -> "weather_thunderstorms"
            "foggy" -> "weather_foggy"
            else -> null
        }
        val res = context.resources
        val resId = res.getIdentifier(resName, "drawable", "android")
        return Utilities.drawableToBitmap(ResourcesCompat.getDrawable(res, resId, null))!!
    }

    companion object {
        private const val authority = "org.pixelexperience.weather.client.provider"
        private val weatherUri = Uri.parse("content://$authority/weather")!!

        private const val statusColumn = "status"
        private const val conditionsColumn = "conditions"
        private const val metricTemperatureColumn = "temperatureMetric"
        private const val imperialTemperatureColumn = "temperatureImperial"
        private val PROJECTION_DEFAULT_WEATHER = arrayOf(
            statusColumn,
            conditionsColumn,
            metricTemperatureColumn,
            imperialTemperatureColumn
        )

        fun isAvailable(context: Context): Boolean {
            val providerInfo = context.packageManager.resolveContentProvider(authority, 0)
                ?: return false
            return ContextCompat.checkSelfPermission(
                context,
                providerInfo.readPermission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}