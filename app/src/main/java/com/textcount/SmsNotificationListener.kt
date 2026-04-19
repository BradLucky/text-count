package com.textcount

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat

class SmsNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this) ?: return
        if (sbn.packageName != defaultSmsPackage) return

        if (sbn.isGroup) return

        val prefs = TextCountPreferences(this)
        val newCount = prefs.incrementCount()
        val target = prefs.targetCount

        if (newCount >= target) {
            Handler(Looper.getMainLooper()).postDelayed({
                showNotification(target)
                prefs.resetCount()
            }, NOTIFICATION_DELAY_MS)
        }

        val updateIntent = Intent(ACTION_SMS_COUNT_UPDATED)
        updateIntent.setPackage(packageName)
        sendBroadcast(updateIntent)
    }

    private fun showNotification(target: Int) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Text Count Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications when your text count reaches the target"
        }
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Text Count Reached!")
            .setContentText("You received $target texts! Count has been reset.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val ACTION_SMS_COUNT_UPDATED = "com.textcount.SMS_COUNT_UPDATED"
        private const val CHANNEL_ID = "text_count_channel"
        private const val NOTIFICATION_ID = 1001
        private const val NOTIFICATION_DELAY_MS = 1_500L
    }
}
