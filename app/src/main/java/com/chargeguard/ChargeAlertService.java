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
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;

public class ChargeAlertService extends Service {

    private static final String CHANNEL_ID = "chargeguard_monitor";

    private boolean alerted90 = false;
    private boolean alerted100 = false;

    private boolean chargingSessionActive = false;

    private int sessionStartLevel = -1;
    private long sessionStartTime = 0;

    private SharedPreferences history;

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

                    /*
                     * CHARGING STARTED
                     */
                    if (charging && !chargingSessionActive) {

                        chargingSessionActive = true;

                        sessionStartLevel = level;

                        sessionStartTime =
                                System.currentTimeMillis();
                    }

                    /*
                     * CHARGING STOPPED
                     */
                    if (!charging && chargingSessionActive) {

                        saveChargingSession(level);

                        chargingSessionActive = false;

                        sessionStartLevel = -1;

                        sessionStartTime = 0;

                        alerted90 = false;
                        alerted100 = false;

                        return;
                    }

                    if (!charging) {
                        alerted90 = false;
                        alerted100 = false;
                        return;
                    }

                    /*
                     * 90% ALERT
                     */
                    if (level >= 90 &&
                            !alerted90 &&
                            level < 100) {

                        alerted90 = true;

                        sendAlert(
                                "⚡ 90% Alert",
                                "Yo! You're at 90% 👀🔋"
                        );
                    }

                    /*
                     * 100% ALERT
                     */
                    if (level >= 100 &&
                            !alerted100) {

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

        history = getSharedPreferences(
                "chargeguard_history",
                MODE_PRIVATE);

        createNotificationChannel();

        Notification notification =
                new NotificationCompat.Builder(
                        this,
                        CHANNEL_ID)

                        .setContentTitle(
                                "ChargeGuard AI")

                        .setContentText(
                                "🧠 Watching your battery")

                        .setSmallIcon(
                                android.R.drawable
                                        .ic_lock_idle_charging)

                        .setOngoing(true)

                        .build();

        startForeground(
                1001,
                notification);

        IntentFilter filter =
                new IntentFilter(
                        Intent.ACTION_BATTERY_CHANGED);

        registerReceiver(
                batteryReceiver,
                filter);
    }

    private void saveChargingSession(int endLevel) {

        if (sessionStartLevel < 0 ||
                sessionStartTime == 0) {
            return;
        }

        long endTime =
                System.currentTimeMillis();

        long duration =
                endTime - sessionStartTime;

        long minutes =
                duration / (1000 * 60);

        /*
         * Ignore extremely short sessions.
         */
        if (minutes < 1 &&
                endLevel <= sessionStartLevel) {
            return;
        }

        String oldHistory =
                history.getString(
                        "sessions",
                        "");

        String session =
                "⚡ " +
                sessionStartLevel +
                "% → " +
                endLevel +
                "% | " +
                formatDuration(minutes);

        String newHistory;

        if (oldHistory.isEmpty()) {

            newHistory = session;

        } else {

            newHistory =
                    session +
                    "\n" +
                    oldHistory;
        }

        /*
         * Keep the latest 10 sessions.
         */
        String[] sessions =
                newHistory.split("\n");

        StringBuilder limitedHistory =
                new StringBuilder();

        int count = 0;

        for (String item : sessions) {

            if (item.trim().isEmpty()) {
                continue;
            }

            if (count >= 10) {
                break;
            }

            if (limitedHistory.length() > 0) {
                limitedHistory.append("\n");
            }

            limitedHistory.append(item);

            count++;
        }

        history.edit()
                .putString(
                        "sessions",
                        limitedHistory.toString())
                .apply();
    }

    private String formatDuration(long minutes) {

        long hours = minutes / 60;

        long remainingMinutes =
                minutes % 60;

        if (hours > 0) {

            return hours +
                    "h " +
                    remainingMinutes +
                    "m";

        } else {

            return minutes +
                    "m";
        }
    }

    private void sendAlert(
            String title,
            String message) {

        Uri sound =
                RingtoneManager.getDefaultUri(
                        RingtoneManager.TYPE_NOTIFICATION);

        Notification notification =
                new NotificationCompat.Builder(
                        this,
                        CHANNEL_ID)

                        .setSmallIcon(
                                android.R.drawable
                                        .ic_lock_idle_charging)

                        .setContentTitle(title)

                        .setContentText(message)

                        .setPriority(
                                NotificationCompat
                                        .PRIORITY_HIGH)

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

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            Uri sound =
                    RingtoneManager.getDefaultUri(
                            RingtoneManager.TYPE_NOTIFICATION);

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "ChargeGuard Battery Alerts",
                            NotificationManager
                                    .IMPORTANCE_HIGH);

            channel.setDescription(
                    "90% and 100% charging alerts");

            android.media.AudioAttributes attributes =
                    new android.media.AudioAttributes
                            .Builder()
                            .setUsage(
                                    android.media.AudioAttributes
                                            .USAGE_NOTIFICATION)
                            .build();

            channel.setSound(
                    sound,
                    attributes);

            NotificationManager manager =
                    getSystemService(
                            NotificationManager.class);

            manager.createNotificationChannel(
                    channel);
        }
    }

    @Override
    public void onDestroy() {

        try {

            unregisterReceiver(
                    batteryReceiver);

        } catch (Exception ignored) {
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}