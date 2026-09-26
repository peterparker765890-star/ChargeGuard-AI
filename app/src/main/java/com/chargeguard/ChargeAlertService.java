package com.chargeguard.ai;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class ChargeAlertService extends Service {

    public static final String ACTION_UPDATE_SETTINGS =
            "com.chargeguard.ai.UPDATE_SETTINGS";

    private static final String CHANNEL_ID =
            "chargeguard_monitor";

    private boolean alerted90 = false;
    private boolean alerted100 = false;

    private MediaPlayer mediaPlayer;

    private final BroadcastReceiver batteryReceiver =
            new BroadcastReceiver() {

                @Override
                public void onReceive(
                        Context context,
                        Intent intent) {

                    int level =
                            intent.getIntExtra(
                                    BatteryManager.EXTRA_LEVEL,
                                    -1
                            );

                    int status =
                            intent.getIntExtra(
                                    BatteryManager.EXTRA_STATUS,
                                    -1
                            );

                    boolean charging =
                            status ==
                                    BatteryManager
                                            .BATTERY_STATUS_CHARGING
                            ||
                            status ==
                                    BatteryManager
                                            .BATTERY_STATUS_FULL;

                    if (!charging) {

                        alerted90 = false;
                        alerted100 = false;

                        return;
                    }

                    boolean alertsEnabled =
                            getSharedPreferences(
                                    "ChargeGuardPrefs",
                                    MODE_PRIVATE
                            ).getBoolean(
                                    "alerts_enabled",
                                    true
                            );

                    if (!alertsEnabled) {
                        return;
                    }

                    if (level >= 90
                            && level < 100
                            && !alerted90) {

                        alerted90 = true;

                        sendAlert(
                                "⚡ 90% ALERT",
                                "You're at 90%. ChargeGuard is watching."
                        );

                        playAlertSound();
                    }

                    if (level >= 100
                            && !alerted100) {

                        alerted100 = true;

                        sendAlert(
                                "🔥 100% FULL POWER",
                                "Battery is full! Unplug your charger."
                        );

                        playAlertSound();
                    }
                }
            };

    @Override
    public void onCreate() {

        super.onCreate();

        createNotificationChannel();

        Notification notification =
                new NotificationCompat.Builder(
                        this,
                        CHANNEL_ID
                )
                        .setContentTitle(
                                "⚡ ChargeGuard AI"
                        )
                        .setContentText(
                                "🛡 Battery protection active"
                        )
                        .setSmallIcon(
                                R.drawable.ic_chargeguard
                        )
                        .setOngoing(true)
                        .setPriority(
                                NotificationCompat.PRIORITY_LOW
                        )
                        .build();

        startForeground(
                1001,
                notification
        );

        IntentFilter filter =
                new IntentFilter(
                        Intent.ACTION_BATTERY_CHANGED
                );

        registerReceiver(
                batteryReceiver,
                filter
        );
    }

    private void sendAlert(
            String title,
            String message) {

        android.content.SharedPreferences preferences =
                getSharedPreferences(
                        "ChargeGuardPrefs",
                        MODE_PRIVATE
                );

        String savedSound =
                preferences.getString(
                        "alert_sound",
                        null
                );

        Uri sound = null;

        if (savedSound != null) {

            sound = Uri.parse(savedSound);

        } else {

            sound =
                    android.media.RingtoneManager
                            .getDefaultUri(
                                    android.media.RingtoneManager
                                            .TYPE_NOTIFICATION
                            );
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        this,
                        CHANNEL_ID
                )
                        .setSmallIcon(
                                R.drawable.ic_chargeguard
                        )
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(
                                NotificationCompat
                                        .PRIORITY_MAX
                        )
                        .setCategory(
                                NotificationCompat
                                        .CATEGORY_ALARM
                        )
                        .setAutoCancel(true)
                        .setSound(sound);

        NotificationManager manager =
                (NotificationManager)
                        getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        manager.notify(
                (int) System.currentTimeMillis(),
                builder.build()
        );
    }

    private void playAlertSound() {

        try {

            if (mediaPlayer != null) {

                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }

            android.content.SharedPreferences preferences =
                    getSharedPreferences(
                            "ChargeGuardPrefs",
                            MODE_PRIVATE
                    );

            String savedSound =
                    preferences.getString(
                            "alert_sound",
                            null
                    );

            Uri sound;

            if (savedSound != null) {

                sound = Uri.parse(savedSound);

            } else {

                sound =
                        android.media.RingtoneManager
                                .getDefaultUri(
                                        android.media.RingtoneManager
                                                .TYPE_ALARM
                                );
            }

            mediaPlayer =
                    MediaPlayer.create(
                            this,
                            sound
                    );

            if (mediaPlayer != null) {

                mediaPlayer.setLooping(false);

                mediaPlayer.start();
            }

        } catch (Exception ignored) {
        }
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "ChargeGuard Battery Alerts",
                            NotificationManager
                                    .IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "Battery charging alerts"
            );

            channel.enableVibration(true);

            channel.setSound(
                    android.media.RingtoneManager
                            .getDefaultUri(
                                    android.media.RingtoneManager
                                            .TYPE_NOTIFICATION
                            ),
                    new AudioAttributes.Builder()
                            .setUsage(
                                    AudioAttributes
                                            .USAGE_NOTIFICATION
                            )
                            .build()
            );

            NotificationManager manager =
                    getSystemService(
                            NotificationManager.class
                    );

            manager.createNotificationChannel(
                    channel
            );
        }
    }

    @Override
    public void onDestroy() {

        try {

            unregisterReceiver(
                    batteryReceiver
            );

        } catch (Exception ignored) {
        }

        if (mediaPlayer != null) {

            try {
                mediaPlayer.stop();
            } catch (Exception ignored) {
            }

            mediaPlayer.release();
            mediaPlayer = null;
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}