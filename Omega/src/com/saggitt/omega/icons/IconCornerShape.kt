/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Omega Launcher Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.icons

import android.graphics.Path
import android.graphics.PointF
import com.android.launcher3.Utilities
import com.android.app.animation.Interpolators.LINEAR

abstract class IconCornerShape {

    abstract fun addCorner(
        path: Path,
        position: Position,
        size: PointF,
        progress: Float,
        offsetX: Float,
        offsetY: Float
    )

    abstract class BaseBezierPath : IconCornerShape() {

        protected val roundControlDistance = .447771526f
        protected open val controlDistance = roundControlDistance
        protected open val controlDistanceX get() = controlDistance
        protected open val controlDistanceY get() = controlDistance

        private fun getControl1X(position: Position, controlDistance: Float): Float {
            return Utilities.mapRange(controlDistance, position.controlX, position.startX)
        }

        private fun getControl1Y(position: Position, controlDistance: Float): Float {
            return Utilities.mapRange(controlDistance, position.controlY, position.startY)
        }

        private fun getControl2X(position: Position, controlDistance: Float): Float {
            return Utilities.mapRange(controlDistance, position.controlX, position.endX)
        }

        private fun getControl2Y(position: Position, controlDistance: Float): Float {
            return Utilities.mapRange(controlDistance, position.controlY, position.endY)
        }

        override fun addCorner(
            path: Path, position: Position, size: PointF, progress: Float,
            offsetX: Float, offsetY: Float
        ) {
            val controlDistanceX =
                Utilities.mapRange(progress, controlDistanceX, roundControlDistance)
            val controlDistanceY =
                Utilities.mapRange(progress, controlDistanceY, roundControlDistance)
            path.cubicTo(
                getControl1X(position, controlDistanceX) * size.x + offsetX,
                getControl1Y(position, controlDistanceY) * size.y + offsetY,
                getControl2X(position, controlDistanceX) * size.x + offsetX,
                getControl2Y(position, controlDistanceY) * size.y + offsetY,
                position.endX * size.x + offsetX,
                position.endY * size.y + offsetY
            )
        }
    }

    class Cut : BaseBezierPath() {

        override val controlDistance = 1f

        override fun addCorner(
            path: Path, position: Position, size: PointF, progress: Float,
            offsetX: Float, offsetY: Float
        ) {
            if (progress == 0f) {
                path.lineTo(
                    position.endX * size.x + offsetX,
                    position.endY * size.y + offsetY
                )
            } else {
                super.addCorner(path, position, size, progress, offsetX, offsetY)
            }
        }

        override fun toString(): String {
            return "cut"
        }
    }

    class CutHex : BaseBezierPath() {

        override fun addCorner(
            path: Path, position: Position, size: PointF, progress: Float,
            offsetX: Float, offsetY: Float
        ) {
            var paddingX = size.x - (size.x * Math.sqrt(3.0) / 2).toFloat()
            var newOffsetX = offsetX

            if (position is Position.BottomRight) {
                path.rMoveTo(-paddingX, 0f)
            }

            if (position is Position.BottomLeft) {
                newOffsetX += paddingX
            }

            if (position is Position.TopLeft) {
                path.setLastPoint(paddingX, size.y)
            }

            if (position is Position.TopRight) {
                newOffsetX -= paddingX
            }

            path.lineTo(
                position.endX * size.x + newOffsetX,
                position.endY * size.y + offsetY
            )
        }

        override fun toString(): String {
            return "cuthex"
        }
    }

    class LightSquircle : BaseBezierPath() {

        override val controlDistance = .1f

        override fun toString(): String {
            return "lightsquircle"
        }
    }


    class Squircle : BaseBezierPath() {

        override val controlDistance = .2f

        override fun toString(): String {
            return "squircle"
        }
    }

    class StrongSquircle : BaseBezierPath() {

        override val controlDistance = .3f

        override fun toString(): String {
            return "strongsquircle"
        }
    }

    class UltraSquircle : BaseBezierPath() {

        override val controlDistance = .37f

        override fun toString(): String {
            return "ultrasquircle"
        }
    }

    class Sammy : BaseBezierPath() {

        override val controlDistanceX = 0.4431717172f
        override val controlDistanceY = 0.1401010101f
    }

    open class Arc : BaseBezierPath() {

        override val controlDistance = roundControlDistance

        override fun toString(): String {
            return "arc"
        }
    }

    class Cupertino : Arc() {

        override fun addCorner(
            path: Path, position: Position, size: PointF, progress: Float,
            offsetX: Float, offsetY: Float
        ) {
            if (progress >= 0.55f) {
                val sizeScale = Utilities.mapToRange(progress, 0.55f, 1f, 0.45f, 1f, LINEAR)
                val adjustment = 1f - sizeScale
                val xAdjustment = size.x * position.controlX * adjustment
                val yAdjustment = size.y * position.controlY * adjustment
                val newSize = PointF(size.x * sizeScale, size.y * sizeScale)
                path.lineTo(
                    position.startX * newSize.x + offsetX + xAdjustment,
                    position.startY * newSize.y + offsetY + yAdjustment
                )
                super.addCorner(
                    path,
                    position,
                    newSize,
                    progress,
                    offsetX + xAdjustment,
                    offsetY + yAdjustment
                )
                return
            }
            val points = points[position] ?: error("")
            path.lineTo(
                points[0].x * size.x + offsetX,
                points[0].y * size.y + offsetY
            )
            for (i in 1..9 step 3) {
                path.cubicTo(
                    points[i].x * size.x + offsetX,
                    points[i].y * size.y + offsetY,
                    points[i + 1].x * size.x + offsetX,
                    points[i + 1].y * size.y + offsetY,
                    points[i + 2].x * size.x + offsetX,
                    points[i + 2].y * size.y + offsetY
                )
            }
            path.lineTo(
                position.endX * size.x + offsetX,
                position.endY * size.y + offsetY
            )
        }

        override fun toString(): String {
            return "cupertino"
        }

        companion object {

            private val points: Map<Position, List<PointF>>

            init {
                val tmp = listOf(
                    PointF(0.302716f, 0f),
                    PointF(0.5035f, 0f),
                    PointF(0.603866f, 0f),
                    PointF(0.71195f, 0.0341666f),
                    PointF(0.82995f, 0.0771166f)
                )
                val positions = listOf(
                    Position.TopLeft,
                    Position.TopRight,
                    Position.BottomRight,
                    Position.BottomLeft
                )
                val allScales = tmp + tmp.asReversed().map { PointF(it.y, it.x) }
                val reversedScales = allScales.asReversed()
                points = positions.associateWith {
                    val normal = Pair(Pair(it.startX, it.endX), Pair(it.startY, it.endY))
                    val reversed = Pair(Pair(it.endX, it.startX), Pair(it.endY, it.startY))
                    when (it) {
                        Position.TopRight, Position.BottomLeft -> allScales
                        Position.TopLeft, Position.BottomRight -> reversedScales
                    }.mapIndexed { index, scale ->
                        val point = if (index < 5) normal else reversed
                        val x = Utilities.mapRange(scale.x, point.first.first, point.first.second)
                        val y = Utilities.mapRange(scale.y, point.second.first, point.second.second)
                        PointF(x, y)
                    }
                }
            }
        }
    }

    sealed class Position {

        abstract val startX: Float
        abstract val startY: Float

        abstract val controlX: Float
        abstract val controlY: Float

        abstract val endX: Float
        abstract val endY: Float

        object TopLeft : Position() {

            override val startX = 0f
            override val startY = 1f
            override val controlX = 0f
            override val controlY = 0f
            override val endX = 1f
            override val endY = 0f
        }

        object TopRight : Position() {

            override val startX = 0f
            override val startY = 0f
            override val controlX = 1f
            override val controlY = 0f
            override val endX = 1f
            override val endY = 1f
        }

        object BottomRight : Position() {

            override val startX = 1f
            override val startY = 0f
            override val controlX = 1f
            override val controlY = 1f
            override val endX = 0f
            override val endY = 1f
        }

        object BottomLeft : Position() {

            override val startX = 1f
            override val startY = 1f
            override val controlX = 0f
            override val controlY = 1f
            override val endX = 0f
            override val endY = 0f
        }
    }

    companion object {

        val cut = Cut()
        val cuthex = CutHex()
        val lightsquircle = LightSquircle()
        val squircle = Squircle()
        val strongsquircle = StrongSquircle()
        val ultrasquircle = UltraSquircle()
        val sammy = Sammy()
        val arc = Arc()
        val cupertino = Cupertino()

        fun fromString(value: String): IconCornerShape {
            return when (value) {
                "cut" -> cut
                "cuthex" -> cuthex
                "lightsquircle" -> lightsquircle
                "cubic", "squircle" -> squircle
                "strongsquircle" -> strongsquircle
                "ultrasquircle" -> ultrasquircle
                "sammy" -> sammy
                "arc" -> arc
                "cupertino" -> cupertino
                else -> error("invalid corner shape $value")
            }
        }
    }
}
