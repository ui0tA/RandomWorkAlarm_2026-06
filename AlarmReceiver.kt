package com.ui.randomalarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.annotation.SuppressLint
import android.app.PendingIntent

class AlarmReceiver : BroadcastReceiver()
{
    @SuppressLint("MissingPermission")
    override fun onReceive(

        context: Context,
        intent: Intent
    )
    {
        val channelId = "summon_channel"

        val activityIntent  = Intent(
            context,
            MainActivity::class.java
        ).apply {
            putExtra("START_TIMER", true)
        }

        val pendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val channel = NotificationChannel(
                channelId,
                "召集通知",
                NotificationManager.IMPORTANCE_HIGH
            )

            val manager =
                context.getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(channel)
        }

        val notification =
            NotificationCompat.Builder(
                context,
                channelId
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_alert
                )
                .setContentTitle("召集命令")
                .setContentText("作業を開始してください")
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat
            .from(context)
            .notify(1, notification)
    }
}