package com.chargeguard.ai;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class ChargeAlertService extends Service {

    private static final String CHANNEL_ID = "chargeguard_monitor";

    private boolean alerted90 = false;
    private boolean alerted100 = false;

    private final BroadcastReceiver batteryReceiver =
            new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {

                    int level = intent.getIntExtra(
                            BatteryManager.EXTRA_LEVEL, -1);

                    int status = intent.getIntExtra(
                            BatteryManager.EXTRA_STATUS, -1);

                    boolean charging =
                            status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL;

                    if (!charging) {
                        alerted90 = false;
                        alerted100 = false;
                        return;
                    }

                    if (level >= 90 && !alerted90 && level < 100) {
                        alerted90 = true;

                        sendAlert(
                                "⚡ 90% Alert",
                                "Yo! You're at 90% 👀🔋"
                        );
                    }

                    if (level >= 100 && !alerted100) {
                        alerted100 = true;

                        sendAlert(
                                "🔋 100% — FULL POWER!",
                                "Brooo, we're full! Unplug me 😭⚡"
                        );
                    }
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        Notification notification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle("ChargeGuard AI")
                        .setContentText("🧠 Watching your battery")
                        .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
                        .setOngoing(true)
                        .build();

        startForeground(1001, notification);

        IntentFilter filter =
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        registerReceiver(batteryReceiver, filter);
    }

    private void sendAlert(String title, String message) {

        Uri sound =
                RingtoneManager.getDefaultUri(
                        RingtoneManager.TYPE_NOTIFICATION);

        Notification notification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(
                                android.R.drawable.ic_lock_idle_charging)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setSound(sound)
                        .build();

        NotificationManager manager =
                (NotificationManager)
                        getSystemService(
                                Context.NOTIFICATION_SERVICE);

        manager.notify(
                (int) System.currentTimeMillis(),
                notification);
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Uri sound =
                    RingtoneManager.getDefaultUri(
                            RingtoneManager.TYPE_NOTIFICATION);

            android.app.NotificationChannel channel =
                    new android.app.NotificationChannel(
                            CHANNEL_ID,
                            "ChargeGuard Battery Alerts",
                            NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription(
                    "90% and 100% charging alerts");

            android.media.AudioAttributes attributes =
                    new android.media.AudioAttributes.Builder()
                            .setUsage(
                                    android.media.AudioAttributes
                                            .USAGE_NOTIFICATION)
                            .build();

            channel.setSound(sound, attributes);

            NotificationManager manager =
                    getSystemService(
                            NotificationManager.class);

            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {

        try {
            unregisterReceiver(batteryReceiver);
        } catch (Exception ignored) {
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}