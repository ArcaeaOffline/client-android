package xyz.sevive.arcaeaoffline.data.notification

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationManagerCompat
import xyz.sevive.arcaeaoffline.R

object Notifications {
    const val CHANNEL_DEFAULT = "default_channel"

    const val GROUP_TASKS = "tasks_group"

    const val CHANNEL_OCR_QUEUE_JOB = "ocr_queue_job_channel"
    const val ID_OCR_QUEUE_JOB = 101

    const val CHANNEL_OCR_QUEUE_ENQUEUE_CHECKER_JOB = "ocr_queue_enqueue_checker_job_channel"
    const val ID_OCR_QUEUE_ENQUEUE_CHECKER_JOB = 102

    private val legacyChannels = listOf<String>()
    private val legacyChannelGroups = listOf<String>()

    private fun buildNotificationChannelGroup(
        groupId: String,
        block: (NotificationChannelGroupCompat.Builder.() -> Unit),
    ): NotificationChannelGroupCompat {
        val builder = NotificationChannelGroupCompat.Builder(groupId)
        builder.block()
        return builder.build()
    }

    private fun buildNotificationChannel(
        channelId: String,
        importance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT,
        block: (NotificationChannelCompat.Builder.() -> Unit),
    ): NotificationChannelCompat {
        val builder = NotificationChannelCompat.Builder(channelId, importance)
        builder.block()
        return builder.build()
    }

    fun createChannels(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)

        legacyChannels.forEach { notificationManager.deleteNotificationChannel(it) }
        legacyChannelGroups.forEach { notificationManager.deleteNotificationChannelGroup(it) }

        val groups = listOf(
            buildNotificationChannelGroup(GROUP_TASKS) {
                setName(context.getString(R.string.notif_group_tasks))
            },
        )
        notificationManager.createNotificationChannelGroupsCompat(groups)

        val channels = listOf(
            buildNotificationChannel(CHANNEL_OCR_QUEUE_JOB) {
                setGroup(GROUP_TASKS)
                setName(context.getString(R.string.notif_channel_ocr_queue_job))
                setDescription(context.getString(R.string.notif_description_ocr_queue_job))
                setShowBadge(false)
            },

            buildNotificationChannel(CHANNEL_OCR_QUEUE_ENQUEUE_CHECKER_JOB) {
                setGroup(GROUP_TASKS)
                setName(context.getString(R.string.notif_channel_ocr_queue_enqueue_checker_job))
                setDescription(context.getString(R.string.notif_description_ocr_queue_enqueue_checker_job))
                setShowBadge(false)
            },
        )
        notificationManager.createNotificationChannelsCompat(channels)
    }
}
