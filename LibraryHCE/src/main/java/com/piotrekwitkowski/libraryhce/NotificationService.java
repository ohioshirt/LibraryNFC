package com.piotrekwitkowski.libraryhce;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

class NotificationService {
    private static final String NOTIFICATION_CHANNEL_NAME = "HCE Service";
    private static final String NOTIFICATION_CHANNEL_DESCRIPTION = "HCE Service channel";
    private final Context context;

    NotificationService(Context ctx) {
        this.context = ctx;
    }

    void show(String text) {
        int randomNotificationId = (int) (Math.random() * 1000);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_NAME).setSmallIcon(R.drawable.ic_stat_name).setContentTitle(context.getString(R.string.app_name)).setContentText(text).setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        NotificationManagerCompat.from(context).notify(randomNotificationId, builder.build());
    }

    void createNotificationChannel(Context ctx) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        String name = ctx.getString(R.string.app_name);
        NotificationChannel nc = new NotificationChannel(NOTIFICATION_CHANNEL_NAME, name, NotificationManager.IMPORTANCE_DEFAULT);
        nc.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);

        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.createNotificationChannel(nc);
        }
    }
}
