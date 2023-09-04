package com.george.fitnessapp.receivers;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.george.fitnessapp.Add_Fragment;
import com.george.fitnessapp.Community_Stats_Fragment;
import com.george.fitnessapp.MainActivity;

public class NotificationReceiver extends BroadcastReceiver {

    NotificationChannel notificationChannel;

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        Intent addIntent = new Intent(context, MainActivity.class);
        addIntent.putExtra("notification","true");

        PendingIntent pendingIntent = PendingIntent.getActivity(context,1,addIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        //Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel("channel_id","channel_name", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("This is a notification Channel");

            manager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"channel_id")
                .setContentIntent(pendingIntent)
                .setSmallIcon(android.R.drawable.arrow_up_float)
                .setContentTitle("Water Notification")
                .setContentText("This is a water reminder. Pls don't forget to drink your daily dose of water. Click the notification to add water!")
                .setAutoCancel(true);

        Notification notification = builder.build();

        manager.notify(1, notification);

    }
}
