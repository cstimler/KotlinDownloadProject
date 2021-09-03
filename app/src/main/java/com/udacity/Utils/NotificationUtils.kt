package com.udacity.Utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.udacity.DetailActivity
import com.udacity.MainActivity
import com.udacity.R
// nice to have this as a reference, but I added the below to MainActivity.ket:
class NotificationUtils {
    // Notification ID.
    private val NOTIFICATION_ID = 0
    private val REQUEST_CODE = 0
    private val FLAGS = 0

    fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context) {
        val contentIntent = Intent(applicationContext, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val detailIntent = Intent(applicationContext, DetailActivity::class.java)
        val detailPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            REQUEST_CODE,
            detailIntent,
            FLAGS)

        val builder = NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.download_notification_channel_id)
        )
            .setContentTitle(applicationContext
                .getString(R.string.notification_title))
            .setContentText(messageBody)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_launcher_foreground,
                applicationContext.getString(R.string.notification_button),
                detailPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        notify(NOTIFICATION_ID, builder.build())
    }
}

fun NotificationManager.cancelNotifications() {
    cancelAll()
}