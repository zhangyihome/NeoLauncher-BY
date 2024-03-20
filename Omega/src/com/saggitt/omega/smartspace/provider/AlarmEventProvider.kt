package com.saggitt.omega.smartspace.provider

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.provider.AlarmClock
import com.android.launcher3.R
import com.saggitt.omega.smartspace.model.SmartspaceScores
import com.saggitt.omega.util.formatTime
import com.saulhdev.smartspace.SmartspaceAction
import com.saulhdev.smartspace.SmartspaceTarget
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AlarmEventProvider(context: Context) : SmartspaceDataSource(
    context, R.string.name_provider_alarm_events
) {
    override var internalTargets: Flow<List<SmartspaceTarget>> = flowOf(disabledTargets)

    init {
        internalTargets = flow {
            while (true) {
                emit(alarmTarget())
                delay(TimeUnit.MINUTES.toMillis(10))
            }
        }
    }

    private fun alarmTarget(): List<SmartspaceTarget> {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager;
        if (alarmManager.nextAlarmClock != null
            && alarmManager.nextAlarmClock!!.triggerTime - System.currentTimeMillis() <= TimeUnit.MINUTES.toMillis(
                30
            )
        ) {
            val alarmClock = alarmManager.nextAlarmClock!!
            val title = context.getString(R.string.resuable_text_alarm)
            val calendarTrigerTime = Calendar.getInstance()
            calendarTrigerTime.timeInMillis = alarmClock.triggerTime
            val subTitle = formatTime(calendarTrigerTime, context)

            val target = SmartspaceTarget(
                smartspaceTargetId = "AlarmEvent",
                headerAction = SmartspaceAction(
                    id = "AlarmEvent",
                    icon = Icon.createWithResource(context, R.drawable.ic_alarm_on_black_24dp),
                    title = title,
                    subtitle = subTitle,
                    pendingIntent = getPendingIntent()
                ),
                score = SmartspaceScores.SCORE_ALARM,
                featureType = SmartspaceTarget.FEATURE_ALARM
            )

            return listOf(target)
        }

        return emptyList()

    }

    private fun getPendingIntent(): PendingIntent {
        return PendingIntent.getActivity(
            context, 0,
            Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }, 0
        )
    }
}