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

package com.saggitt.omega.blur

import android.content.Context
import android.graphics.Bitmap
import com.hoko.blur.HokoBlur
import com.hoko.blur.task.AsyncBlurTask
import com.saggitt.omega.preferences.NeoPrefs

class BlurWallpaperFilter(private val context: Context) : WallpaperFilter {

    private var blurRadius = 25

    override fun applyPrefs(prefs: NeoPrefs) {
        blurRadius = (prefs.profileBlurRadius.getValue() / BlurWallpaperProvider.DOWN_SAMPLE_FACTOR)
            .toInt()
        blurRadius = blurRadius.coerceAtLeast(1).coerceAtMost(25)
    }

    override fun apply(wallpaper: Bitmap): WallpaperFilter.ApplyTask {
        return WallpaperFilter.ApplyTask.create { emitter ->
            HokoBlur.with(context)
                .scheme(HokoBlur.SCHEME_OPENGL)
                .mode(HokoBlur.MODE_STACK)
                .radius(blurRadius)
                .sampleFactor(BlurWallpaperProvider.DOWN_SAMPLE_FACTOR.toFloat())
                .forceCopy(false)
                .needUpscale(true)
                .processor()
                .asyncBlur(wallpaper, object : AsyncBlurTask.Callback {
                    override fun onBlurSuccess(bitmap: Bitmap) {
                        emitter.onSuccess(bitmap)
                    }

                    override fun onBlurFailed(error: Throwable?) {
                        emitter.onError(error!!)
                    }
                })
        }
    }
}