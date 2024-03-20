/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
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
package com.saggitt.omega.allapps

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import com.android.launcher3.AbstractFloatingView
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.workprofile.PersonalWorkSlidingTabStrip
import com.saggitt.omega.compose.components.ComposeBottomSheet
import com.saggitt.omega.groups.category.DrawerTabs
import com.saggitt.omega.groups.ui.EditGroupBottomSheet
import com.saggitt.omega.preferences.NeoPrefs
import com.saggitt.omega.theme.AccentColorOption
import com.saggitt.omega.views.ColoredButton
import kotlin.math.floor

class AllAppsTabItem(context: Context, attrs: AttributeSet) :
    PersonalWorkSlidingTabStrip(context, attrs) {

    private var mSelectedIndicatorPaint: Paint = Paint()

    private var mIndicatorLeft = -1
    private var mIndicatorRight = -1
    private var mScrollOffset = 0f
    private var mSelectedPosition = 0

    private var mIsRtl = false

    private val mArgbEvaluator: ArgbEvaluator = ArgbEvaluator()
    val prefs: NeoPrefs = Utilities.getNeoPrefs(context)
    val launcher: Launcher = Launcher.getLauncher(context)

    init {
        mSelectedIndicatorPaint.color = prefs.profileAccentColor.getColor()
        mIsRtl = Utilities.isRtl(resources)
    }

    override fun updateTabTextColor(pos: Int) {
        mSelectedPosition = pos
        for (i in 0 until childCount) {
            val tab = getChildAt(i) as ColoredButton
            tab.isSelected = i == pos
        }
    }

    private fun getChildWidth(): Int {
        var width = 0
        for (i in 0 until childCount) {
            width += getChildAt(i).measuredWidth
        }
        return width
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val childWidth = getChildWidth()
        if (childWidth < measuredWidth) {
            val isLayoutRtl = layoutDirection == LAYOUT_DIRECTION_RTL
            val count = childCount
            var start = 0
            var dir = 1
            //In case of RTL, start drawing from the last child.
            if (isLayoutRtl) {
                start = count - 1
                dir = -1
            }
            val horizontalPadding = paddingLeft + paddingRight
            val padding = (measuredWidth - childWidth - horizontalPadding) / (count + 1)
            var left = paddingLeft
            for (i in 0 until count) {
                val childIndex = start + dir * i
                val child = getChildAt(childIndex)
                left += padding
                setChildFrame(child, left, paddingTop, child.measuredWidth, child.measuredHeight)
                left += child.measuredWidth
            }
        } else {
            super.onLayout(changed, l, t, r, b)
        }
        updateTabTextColor(mSelectedPosition)
        updateIndicatorPosition(mScrollOffset)
    }

    private fun setChildFrame(child: View, left: Int, top: Int, width: Int, height: Int) {
        child.layout(left, top, left + width, top + height)
    }

    private fun updateIndicatorPosition(scrollOffset: Float) {
        mScrollOffset = scrollOffset
        updateIndicatorPosition()
    }

    private fun updateIndicatorPosition() {
        val scaled: Float = mScrollOffset * (childCount - 1)
        var left = -1
        var right = -1
        val position = floor(scaled).toInt()
        val leftFraction = scaled - position
        val rightFraction = 1 - leftFraction
        val leftIndex = if (mIsRtl) childCount - position - 1 else position
        val rightIndex = if (mIsRtl) (leftIndex - 1) else (leftIndex + 1)
        var leftTab: ColoredButton? = null
        var rightTab: ColoredButton? = null

        if (getChildAt(leftIndex) != null) {
            leftTab = getChildAt(leftIndex) as ColoredButton
        }

        if (getChildAt(rightIndex) != null) {
            rightTab = getChildAt(rightIndex) as ColoredButton
        }

        if (leftTab != null && rightTab != null) {
            val leftWidth = leftTab.width
            val rightWidth = rightTab.width
            val width = leftWidth + (rightWidth - leftWidth) * leftFraction
            val halfWidth = width / 2
            val leftCenter = leftTab.left + leftWidth / 2f
            val rightCenter = rightTab.left + rightWidth / 2f
            val dis = rightCenter - leftCenter
            val center = leftCenter + (dis * leftFraction).toInt()
            left = (center - halfWidth).toInt()
            right = (center + halfWidth).toInt()
            val leftColor = leftTab.color
            val rightColor = rightTab.color
            if (leftColor == rightColor) {
                mSelectedIndicatorPaint.color = leftColor
            } else {
                mSelectedIndicatorPaint.color =
                    mArgbEvaluator.evaluate(leftFraction, leftColor, rightColor) as Int
            }
        } else if (leftTab != null) {
            left = (leftTab.left + leftTab.width * leftFraction).toInt()
            right = left + leftTab.width
            mSelectedIndicatorPaint.color = leftTab.color
        } else if (rightTab != null) {
            right = (rightTab.right - rightTab.width * rightFraction).toInt()
            left = right - rightTab.width
            mSelectedIndicatorPaint.color = rightTab.color
        }
        setIndicatorPosition(left, right)
    }

    private fun setIndicatorPosition(left: Int, right: Int) {
        if (left != mIndicatorLeft || right != mIndicatorRight) {
            mIndicatorLeft = left
            mIndicatorRight = right
            invalidate()
            centerInScrollView()
        }
    }

    private fun centerInScrollView() {
        val scrollView = parent as HorizontalScrollView
        val padding = left
        val center: Int = (mIndicatorLeft + mIndicatorRight) / 2 + padding
        val scroll = center - scrollView.width / 2
        val maxAmount = width - scrollView.width + padding + padding
        val boundedScroll: Int = Utilities.boundToRange(scroll, 0, maxAmount)
        scrollView.scrollTo(boundedScroll, 0)
    }

    override fun setScroll(currentScroll: Int, totalScroll: Int) {
        val scrollOffset = currentScroll.toFloat() / totalScroll
        updateIndicatorPosition(scrollOffset)
    }

    fun inflateButtons(tabs: AllAppsTabs) {
        val childCount = childCount
        val count = tabs.count
        val inflater = LayoutInflater.from(context)
        for (i in childCount until count) {
            inflater.inflate(R.layout.all_apps_tab, this)
        }
        while (getChildCount() > count) {
            removeViewAt(0)
        }
        for (i in 0 until tabs.count) {
            val tab = tabs[i]
            val button = getChildAt(i) as ColoredButton
            button.color = getTabColor(tab.drawerTab)
            button.refreshColor()
            button.text = tab.name
            button.setOnLongClickListener { v: View ->
                ComposeBottomSheet.show(v.context) {
                    EditGroupBottomSheet(
                        category = prefs.drawerAppGroupsManager.getEnabledType()!!,
                        group = tab.drawerTab,
                        onClose = { AbstractFloatingView.closeAllOpenViews(launcher) }
                    )
                }
                true
            }
        }
        updateIndicatorPosition()
        invalidate()
    }

    private fun getTabColor(tab: DrawerTabs.Tab): Int {
        return AccentColorOption.fromString(tab.color.value()).accentColor
    }
}