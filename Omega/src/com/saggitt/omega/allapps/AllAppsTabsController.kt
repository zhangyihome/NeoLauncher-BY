package com.saggitt.omega.allapps

import android.view.View
import android.view.ViewGroup
import com.android.launcher3.BaseDraggingActivity
import com.android.launcher3.allapps.ActivityAllAppsContainerView
import com.android.launcher3.allapps.ActivityAllAppsContainerView.AdapterHolder.MAIN
import com.android.launcher3.allapps.ActivityAllAppsContainerView.AdapterHolder.WORK
import com.android.launcher3.allapps.AllAppsPagedView
import com.android.launcher3.allapps.AllAppsStore
import com.saggitt.omega.util.forEachChildIndexed

typealias AdapterHolders = List<ActivityAllAppsContainerView<*>.AdapterHolder>

class AllAppsTabsController<T : BaseDraggingActivity>(
    val tabs: AllAppsTabs,
    private val container: ActivityAllAppsContainerView<*>,
) {
    val tabsCount get() = tabs.count
    val shouldShowTabs get() = tabsCount > 1

    private var holders = mutableListOf<ActivityAllAppsContainerView<*>.AdapterHolder>()

    private var horizontalPadding = 0
    private var bottomPadding = 0

    fun createHolders(): AdapterHolders {
        for (tab in tabs) {
            if (tab.isWork) {
                holders.add(container.createHolder(WORK).apply {
                    mPadding.bottom = bottomPadding
                    mPadding.left = horizontalPadding
                    mPadding.right = horizontalPadding
                })
            } else {
                holders.add(container.createHolder(MAIN).apply {
                    mPadding.bottom = bottomPadding
                    mPadding.left = horizontalPadding
                    mPadding.right = horizontalPadding
                })
            }
        }
        return holders
    }

    fun reloadTabs() {
        tabs.reloadTabs()
    }

    fun registerIconContainers(allAppsStore: AllAppsStore<T>) {
        holders.forEach { allAppsStore.registerIconContainer(it.mRecyclerView) }
    }

    fun unregisterIconContainers(allAppsStore: AllAppsStore<T>) {
        holders.forEach { allAppsStore.unregisterIconContainer(it.mRecyclerView) }
    }

    fun setup(pagedView: AllAppsPagedView) {
        tabs.forEachIndexed { index, tab ->
            holders[index].setIsWork(tab.isWork)
            holders[index].setup(pagedView.getChildAt(index), tab.matcher)
        }
    }

    fun setup(view: View) {
        holders.forEach { it.mRecyclerView = null }
        holders[0].setup(view, null)
    }

    fun bindButtons(buttonsContainer: ViewGroup, pagedView: AllAppsPagedView) {
        buttonsContainer.forEachChildIndexed { view, i ->
            view.setOnClickListener { pagedView.snapToPage(i) }
        }
    }

    fun setPadding(horizontal: Int, bottom: Int) {
        horizontalPadding = horizontal
        bottomPadding = bottom

        holders.forEach {
            it.mPadding.bottom = bottomPadding
            it.mPadding.left = horizontalPadding
            it.mPadding.right = horizontalPadding
            it.applyPadding()
        }
    }
}
