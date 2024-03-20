package com.saggitt.omega.smartspace.provider

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Icon
import com.android.launcher3.R
import com.saggitt.omega.compose.components.preferences.isNotificationServiceEnabled
import com.saggitt.omega.compose.components.preferences.notificationDotsEnabled
import com.saggitt.omega.compose.navigation.Routes
import com.saggitt.omega.preferences.PreferenceActivity
import com.saggitt.omega.smartspace.model.SmartspaceScores
import com.saulhdev.smartspace.SmartspaceAction
import com.saulhdev.smartspace.SmartspaceTarget
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first

class NowPlayingProvider(context: Context) : SmartspaceDataSource(
    context, R.string.event_provider_now_playing
) {

    private val defaultIcon = Icon.createWithResource(context, R.drawable.ic_music_note)

    override val internalTargets = callbackFlow {
        val mediaListener = MediaListener(context) {
            trySend(listOfNotNull(getSmartspaceTarget(it)))
        }
        mediaListener.onResume()
        awaitClose { mediaListener.onPause() }
    }

    private fun getSmartspaceTarget(media: MediaListener): SmartspaceTarget? {
        val tracking = media.tracking ?: return null
        val title = tracking.info?.title ?: return null

        val sbn = tracking.sbn
        val icon = sbn?.notification?.smallIcon ?: defaultIcon

        val mediaInfo = tracking.info
        val subtitle = mediaInfo?.artist?.takeIf { it.isNotEmpty() }
            ?: sbn?.getAppName(context)
            ?: context.getAppName(tracking.packageName)
        val intent = sbn?.notification?.contentIntent
        return SmartspaceTarget(
            smartspaceTargetId = "nowPlaying-${mediaInfo.hashCode()}",
            headerAction = SmartspaceAction(
                id = "nowPlayingAction-${mediaInfo.hashCode()}",
                icon = icon,
                title = title,
                subtitle = subtitle,
                pendingIntent = intent,
                onClick = if (intent == null) Runnable { media.toggle(true) } else null,
            ),
            score = SmartspaceScores.SCORE_MEDIA,
            featureType = SmartspaceTarget.FEATURE_MEDIA
        )
    }

    override suspend fun requiresSetup(): Boolean =
        isNotificationServiceEnabled(context = context).not() ||
                notificationDotsEnabled(context = context).first().not()

    override suspend fun startSetup(activity: Activity) {
        val intent = PreferenceActivity.createIntent(activity, "/${Routes.PREFS_WIDGETS}/")
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
}
