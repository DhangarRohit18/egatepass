package com.example.egatepass;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "egatepass_notifications";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // Save token to firestore if logged in
        com.google.firebase.auth.FirebaseUser u = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (u != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(u.getUid())
                    .update("fcmToken", token);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String title = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getTitle() : "Gatepass";
        String body = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getBody() : "Status updated";

        createChannel();

        Intent intent = new Intent(this, MyRequestsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_notification) // add an icon in drawable
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify((int)System.currentTimeMillis(), nb.build());
    }

    private void createChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Gatepass Notifications", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Push notifications for gatepass status");
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);
        }
    }
}
