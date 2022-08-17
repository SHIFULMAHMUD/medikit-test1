package com.app.test1.notification;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import androidx.core.app.NotificationCompat;

import com.app.test1.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class MyNotificationManager {
    public static final int ID_BIG_NOTIFICATION = new Random().nextInt(10);
    public static final int ID_SMALL_NOTIFICATION = new Random().nextInt(10);
    String description = "channel Description";
    String channelId = "CHANNELID";
    String channelName = "channel Name";

    private Context mCtx;
    Uri urinotification;

    public MyNotificationManager(Context mCtx) {
        this.mCtx = mCtx;
    }


    public void showBigNotification(String title, String message, String url, Intent intent) {
        try {
            urinotification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(mCtx, urinotification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mCtx,
                        new Random().nextInt(10),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(title);
        bigPictureStyle.setSummaryText(Html.fromHtml(message).toString());
        bigPictureStyle.bigPicture(getBitmapFromURL(url));
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mCtx, channelId);
        Notification notification;
        notification = mBuilder.setSmallIcon(R.drawable.logo).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setContentTitle(Html.fromHtml(title).toString())
                .setStyle(bigPictureStyle)
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(BitmapFactory.decodeResource(mCtx.getResources(), R.drawable.logo))
                .setContentText(Html.fromHtml(message).toString())
                .setSound(urinotification)
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        android.app.NotificationManager notificationManager = (android.app.NotificationManager) mCtx.getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,channelName, android.app.NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        if (notificationManager != null) {
            notificationManager.notify(new Random().nextInt(10), notification);
        }
    }

    //the method will show a small notification
    //parameters are title for message title, message for message text and an intent that will open
    //when you will tap on the notification
    public void showSmallNotification(String title, String message, Intent intent) {
        try {
            urinotification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(mCtx, urinotification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }



        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mCtx,
                        ID_SMALL_NOTIFICATION,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mCtx,channelId);
        Notification notification;
        notification = mBuilder.setSmallIcon(R.drawable.logo).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(BitmapFactory.decodeResource(mCtx.getResources(), R.drawable.logo))
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setSound(urinotification)
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        android.app.NotificationManager notificationManager = (android.app.NotificationManager) mCtx.getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,channelName, android.app.NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }


        if (notificationManager != null) {
            notificationManager.notify(ID_SMALL_NOTIFICATION, notification);
        }
    }

    //The method will return Bitmap from an image URL
    private Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}