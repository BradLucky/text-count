package com.textcount
 
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.core.app.NotificationCompat
 
class SmsReceiver : BroadcastReceiver() {
 
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
 
        val prefs = TextCountPreferences(context)
        val newCount = prefs.incrementCount()
        val target = prefs.targetCount
 
        if (newCount >= target) {
            showNotification(context, target)
            prefs.resetCount()
        }
 
        // Broadcast to update UI if the app is open
        val updateIntent = Intent(ACTION_SMS_COUNT_UPDATED)
        updateIntent.setPackage(context.packageName)
        context.sendBroadcast(updateIntent)
    }
 
    private fun showNotification(context: Context, target: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
 
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Text Count Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications when your text count reaches the target"
        }
        notificationManager.createNotificationChannel(channel)
 
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
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
    }
}
