package com.example.egatepass;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import androidx.core.app.NotificationCompat;
import android.os.Build;

public class LocalNotificationHelper {
    public static void show(Context ctx, String title, String message){
        NotificationManager mgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        String ch = "gp_updates";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(ch, "Gatepass updates", NotificationManager.IMPORTANCE_HIGH);
            mgr.createNotificationChannel(channel);
        }
        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, ch)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true);

        mgr.notify((int)System.currentTimeMillis(), b.build());
    }
}
