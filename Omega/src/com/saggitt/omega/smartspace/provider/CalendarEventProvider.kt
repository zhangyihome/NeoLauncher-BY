package com.saggitt.omega.smartspace.provider

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.provider.CalendarContract
import android.text.format.DateFormat
import android.util.Log
import com.android.launcher3.R
import com.saggitt.omega.compose.navigation.Routes
import com.saggitt.omega.preferences.PreferenceActivity
import com.saggitt.omega.smartspace.model.SmartspaceScores
import com.saulhdev.smartspace.SmartspaceAction
import com.saulhdev.smartspace.SmartspaceTarget
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

class CalendarEventProvider(context: Context) : SmartspaceDataSource(
    context, R.string.smartspace_provider_calendar
) {
    override var internalTargets: Flow<List<SmartspaceTarget>> = flowOf(disabledTargets)

    private val requiredPermissions = listOf(android.Manifest.permission.READ_CALENDAR)
    private val calendarProjection = arrayOf(
        CalendarContract.Instances._ID,
        CalendarContract.Instances.TITLE,
        CalendarContract.Instances.DTSTART,
        CalendarContract.Instances.DTEND,
        CalendarContract.Instances.EVENT_LOCATION,
        CalendarContract.Instances.CUSTOM_APP_PACKAGE
    )
    private val oneMinute = TimeUnit.MINUTES.toMillis(1)
    private val includeBehind = oneMinute * 15
    private val includeAhead = oneMinute * 60

    init {
        internalTargets = flow {
            while (true) {
                requiresSetup()
                emit(calendarTarget())
                delay(TimeUnit.MINUTES.toMillis(30))
            }
        }
    }

    private fun calendarTarget(): List<SmartspaceTarget> {
        val event = getNextEvent()
        Log.d("CalendarEventProvider", "calendarTarget " + event?.title)
        if (event != null) {
            val timeText = "${formatTime(event.start)} – ${formatTime(event.end)}"
            val subtitle = if (event.location != null) {
                "${event.location} $timeText"
            } else {
                timeText
            }

            val target = SmartspaceTarget(
                smartspaceTargetId = "CalendarEvent",
                headerAction = SmartspaceAction(
                    id = "CalendarEvent",
                    icon = Icon.createWithResource(context, R.drawable.ic_calendar),
                    title = "${event.title} ${formatTimeRelative(event.start)}",
                    subtitle = subtitle,
                    pendingIntent = getPendingIntent(event)
                ),
                score = SmartspaceScores.SCORE_CALENDAR,
                featureType = SmartspaceTarget.FEATURE_CALENDAR,
            )
            return listOf(target)
        } else {
            return disabledTargets
        }
    }

    private fun formatTime(time: Long) = DateFormat.getTimeFormat(context).format(Date(time))

    private fun formatTimeRelative(time: Long): String {
        val res = context.resources
        val currentTime = System.currentTimeMillis()
        if (time <= currentTime) {
            return res.getString(R.string.smartspace_now)
        }
        val minutesToEvent = ceil((time - currentTime).toDouble() / oneMinute).toInt()
        val timeString = if (minutesToEvent >= 60) {
            val hours = minutesToEvent / 60
            val minutes = minutesToEvent % 60
            val hoursString = res.getQuantityString(R.plurals.smartspace_hours, hours, hours)
            if (minutes <= 0) {
                hoursString
            } else {
                val minutesString =
                    res.getQuantityString(R.plurals.smartspace_minutes, minutes, minutes)
                res.getString(R.string.smartspace_hours_mins, hoursString, minutesString)
            }
        } else {
            res.getQuantityString(R.plurals.smartspace_minutes, minutesToEvent, minutesToEvent)
        }
        return res.getString(R.string.smartspace_in_time, timeString)
    }

    private fun getPendingIntent(event: CalendarEvent): PendingIntent? {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("content://com.android.calendar/events/${event.id}")
            `package` = event.appPackage
        }
        return PendingIntent.getActivity(context, 0, intent, 0)
    }

    @SuppressLint("Range")
    private fun getNextEvent(): CalendarEvent? {
        val currentTime = System.currentTimeMillis()
        context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            calendarProjection,
            "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?",
            arrayOf("${currentTime - includeBehind}", "${currentTime + includeAhead}"),
            "${CalendarContract.Events.DTSTART} ASC LIMIT 1"
        )
            ?.use { c ->
                while (c.moveToNext()) {
                    return CalendarEvent(
                        c.getLong(c.getColumnIndex(CalendarContract.Events._ID)),
                        c.getString(c.getColumnIndex(CalendarContract.Events.TITLE)),
                        c.getLong(c.getColumnIndex(CalendarContract.Events.DTSTART)),
                        c.getLong(c.getColumnIndex(CalendarContract.Events.DTEND)),
                        c.getString(c.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)),
                        c.getString(c.getColumnIndex(CalendarContract.Events.CUSTOM_APP_PACKAGE))
                    )
                }
            }
        return null
    }

    private fun checkPermissionGranted(): Boolean {
        return requiredPermissions.all { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
    }

    override suspend fun requiresSetup(): Boolean =
        checkPermissionGranted().not()

    override suspend fun startSetup(activity: Activity) {
        val intent = PreferenceActivity.createIntent(activity, "/${Routes.PREFS_MAIN}/")
        val message = activity.getString(
            R.string.event_provider_missing_notification_dots,
            activity.getString(providerName)
        )

        PreferenceActivity.startBlankActivityDialog(
            activity,
            intent,
            activity.getString(R.string.title_missing_notification_access),
            message,
            context.getString(R.string.title_change_settings),
        )
    }

    data class CalendarEvent(
        val id: Long,
        val title: String,
        val start: Long,
        val end: Long,
        val location: String?,
        val appPackage: String?
    )
}