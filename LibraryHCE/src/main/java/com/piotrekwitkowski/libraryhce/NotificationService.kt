package com.piotrekwitkowski.libraryhce

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

internal class NotificationService(private val context: Context) {
    fun show(text: String?) {
        val randomNotificationId = (Math.random() * 1000).toInt()
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_NAME)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(context.getString(R.string.app_name)).setContentText(text).setPriority(
            NotificationCompat.PRIORITY_DEFAULT
        )
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        NotificationManagerCompat.from(context).notify(randomNotificationId, builder.build())
    }

    fun createNotificationChannel(ctx: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = ctx.getString(R.string.app_name)
        val nc = NotificationChannel(
            NOTIFICATION_CHANNEL_NAME,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        nc.description = NOTIFICATION_CHANNEL_DESCRIPTION

        val nm = ctx.getSystemService(
            NotificationManager::class.java
        )
        nm?.createNotificationChannel(nc)
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_NAME = "HCE Service"
        private const val NOTIFICATION_CHANNEL_DESCRIPTION = "HCE Service channel"
    }
}
