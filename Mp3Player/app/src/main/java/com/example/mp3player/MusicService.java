package com.example.mp3player;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static android.app.NotificationManager.IMPORTANCE_DEFAULT;

//import static com.mp3player.MP3PlayerApp.CHANNEL_ID;

public class MusicService extends Service {

    //Declare Variables
    private final IBinder myBinder = new MyLocalBinder();
    public MP3Player mp3;
    public String CHANNEL_ID = "Mp3Channel";
    private int notificationId=10;
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);

    //Init Notification manager
    NotificationManager notificationManager;

    //Constructor to initialize an object of MP3Player class
    public MusicService() {
        mp3 = new MP3Player();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        notificationManager = getSystemService(NotificationManager.class);

        //Customize the Notification UI
        builder
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Mp3 Player")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //Onclick event for Notification, redirecting it to Main Activity
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        //Create Notification Channel
        NotificationChannel mp3channel = new NotificationChannel(CHANNEL_ID, "Mp3 Player", IMPORTANCE_DEFAULT);
        mp3channel.setDescription("Mp3 notification");
        notificationManager.createNotificationChannel(mp3channel);

        //Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, builder.build());
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    //Local binder for Music Service
    public class MyLocalBinder extends Binder{
        MusicService getService(){
            return MusicService.this;
        }
    }

    //Function for removing the notification
    public void removeNotification() {
        String nService = Context.NOTIFICATION_SERVICE;
        NotificationManager Manager = (NotificationManager) getApplicationContext().getSystemService(nService);
        Manager.cancel(0);
    }

    //onDestroy for the service
    @Override
    public void onDestroy() {
        super.onDestroy();
        removeNotification();
        mp3.stop();
    }

}
