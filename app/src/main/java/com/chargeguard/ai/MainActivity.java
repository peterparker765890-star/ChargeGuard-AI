package com.chargeguard.ai;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private TextView batteryText;
    private TextView chargingText;
    private TextView insightText;
    private TextView alertStatusText;

    private boolean alertsEnabled = true;

    private SharedPreferences preferences;

    private static final int SOUND_PICKER_REQUEST = 500;

    /*
     * Continuously watches the phone battery.
     * This fixes the old battery percentage staying on screen.
     */
    private final BroadcastReceiver batteryReceiver =
            new BroadcastReceiver() {

                @Override
                public void onReceive(
                        Context context,
                        Intent intent) {

                    if (Intent.ACTION_BATTERY_CHANGED.equals(
                            intent.getAction())) {

                        updateBatteryDisplay(intent);
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(
                "ChargeGuardPrefs",
                MODE_PRIVATE
        );

        alertsEnabled = preferences.getBoolean(
                "alerts_enabled",
                true
        );

        requestNotificationPermission();

        buildInterface();

        startChargeGuardService();

        updateBatteryDisplay();
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter =
                new IntentFilter(
                        Intent.ACTION_BATTERY_CHANGED
                );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            registerReceiver(
                    batteryReceiver,
                    filter,
                    Context.RECEIVER_NOT_EXPORTED
            );

        } else {

            registerReceiver(
                    batteryReceiver,
                    filter
            );
        }

        updateBatteryDisplay();
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            unregisterReceiver(batteryReceiver);
        } catch (Exception ignored) {
        }
    }

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.TIRAMISU) {

            if (checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        100
                );
            }
        }
    }

    private void startChargeGuardService() {

        Intent serviceIntent =
                new Intent(
                        this,
                        ChargeAlertService.class
                );

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            startForegroundService(
                    serviceIntent
            );

        } else {

            startService(
                    serviceIntent
            );
        }
    }

    /*
     * Gets the current battery information.
     */
    private void updateBatteryDisplay() {

        Intent batteryIntent =
                registerReceiver(
                        null,
                        new IntentFilter(
                                Intent.ACTION_BATTERY_CHANGED
                        )
                );

        if (batteryIntent != null) {

            updateBatteryDisplay(
                    batteryIntent
            );
        }
    }

    /*
     * Updates the UI from the current battery Intent.
     */
    private void updateBatteryDisplay(
            Intent batteryIntent) {

        int level =
                batteryIntent.getIntExtra(
                        BatteryManager.EXTRA_LEVEL,
                        -1
                );

        int status =
                batteryIntent.getIntExtra(
                        BatteryManager.EXTRA_STATUS,
                        -1
                );

        int plugged =
                batteryIntent.getIntExtra(
                        BatteryManager.EXTRA_PLUGGED,
                        -1
                );

        boolean charging =
                status ==
                        BatteryManager.BATTERY_STATUS_CHARGING
                        ||
                status ==
                        BatteryManager.BATTERY_STATUS_FULL;

        /*
         * Always update the percentage.
         */
        if (level >= 0) {

            batteryText.setText(
                    level + "%"
            );
        }

        if (charging) {

            String chargingType =
                    getChargingType(
                            plugged
                    );

            if (level >= 100) {

                chargingText.setText(
                        "🔥 FULL • " +
                        chargingType
                );

                chargingText.setTextColor(
                        Color.rgb(
                                255,
                                90,
                                70
                        )
                );

                insightText.setText(
                        "🔥 FULL POWER\n\n" +
                        "Battery reached 100%.\n" +
                        "ChargeGuard recommends unplugging now."
                );

            } else if (level >= 90) {

                chargingText.setText(
                        "⚡ CHARGING • " +
                        chargingType
                );

                chargingText.setTextColor(
                        Color.rgb(
                                250,
                                204,
                                21
                        )
                );

                insightText.setText(
                        "🚨 FINAL CHARGE ZONE\n\n" +
                        "You're above 90%.\n" +
                        "ChargeGuard is watching your battery."
                );

            } else if (level <= 20) {

                chargingText.setText(
                        "⚡ CHARGING • " +
                        chargingType
                );

                chargingText.setTextColor(
                        Color.rgb(
                                34,
                                197,
                                94
                        )
                );

                insightText.setText(
                        "🔋 POWER RECOVERY\n\n" +
                        "Battery is low and charging.\n" +
                        "ChargeGuard is monitoring the recovery."
                );

            } else {

                chargingText.setText(
                        "⚡ CHARGING • " +
                        chargingType
                );

                chargingText.setTextColor(
                        Color.rgb(
                                34,
                                197,
                                94
                        )
                );

                insightText.setText(
                        "🧠 AI BATTERY WATCH\n\n" +
                        "Charging normally.\n" +
                        "ChargeGuard is monitoring your battery."
                );
            }

        } else {

            chargingText.setText(
                    "● NOT CHARGING"
            );

            chargingText.setTextColor(
                    Color.LTGRAY
            );

            if (level <= 15) {

                insightText.setText(
                        "🆘 CRITICAL BATTERY\n\n" +
                        "Battery is very low.\n" +
                        "Plug in your charger."
                );

            } else if (level <= 30) {

                insightText.setText(
                        "⚠️ BATTERY LOW\n\n" +
                        "You may want to charge soon."
                );

            } else {

                insightText.setText(
                        "🧠 AI STATUS\n\n" +
                        "Battery monitoring is active."
                );
            }
        }

        /*
         * Small live status at bottom.
         */
        if (alertStatusText != null) {

            if (alertsEnabled) {

                alertStatusText.setText(
                        "🛡 ChargeGuard is protecting your battery"
                );

            } else {

                alertStatusText.setText(
                        "🔕 Charge alerts are currently disabled"
                );
            }
        }
    }

    /*
     * Detects whether charging is through USB, AC,
     * wireless or another charging source.
     */
    private String getChargingType(int plugged) {

        if (plugged ==
                BatteryManager.BATTERY_PLUGGED_USB) {

            return "USB";

        } else if (plugged ==
                BatteryManager.BATTERY_PLUGGED_AC) {

            return "AC";

        } else if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.JELLY_BEAN_MR1
                &&
                plugged ==
                        BatteryManager.BATTERY_PLUGGED_WIRELESS) {

            return "WIRELESS";

        } else if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.TIRAMISU
                &&
                plugged ==
                        BatteryManager.BATTERY_PLUGGED_DOCK) {

            return "DOCK";

        } else {

            return "CHARGER";
        }
    }

    private void buildInterface() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                28,
                45,
                28,
                30
        );

        root.setBackgroundColor(
                Color.rgb(
                        7,
                        7,
                        10
                )
        );

        TextView logo =
                new TextView(this);

        logo.setText(
                "⚡ CHARGEGUARD"
        );

        logo.setTextColor(
                Color.WHITE
        );

        logo.setTextSize(30);

        logo.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        logo.setGravity(
                Gravity.CENTER
        );

        TextView aiLabel =
                new TextView(this);

        aiLabel.setText(
                "AI BATTERY DEFENSE SYSTEM"
        );

        aiLabel.setTextColor(
                Color.rgb(
                        168,
                        85,
                        247
                )
        );

        aiLabel.setTextSize(13);

        aiLabel.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        aiLabel.setGravity(
                Gravity.CENTER
        );

        aiLabel.setPadding(
                0,
                8,
                0,
                30
        );

        batteryText =
                new TextView(this);

        batteryText.setText(
                "--%"
        );

        batteryText.setTextColor(
                Color.WHITE
        );

        batteryText.setTextSize(64);

        batteryText.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        batteryText.setGravity(
                Gravity.CENTER
        );

        chargingText =
                new TextView(this);

        chargingText.setText(
                "CHECKING..."
        );

        chargingText.setTextColor(
                Color.LTGRAY
        );

        chargingText.setTextSize(17);

        chargingText.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        chargingText.setGravity(
                Gravity.CENTER
        );

        chargingText.setPadding(
                0,
                5,
                0,
                25
        );

        insightText =
                new TextView(this);

        insightText.setText(
                "🧠 AI BATTERY WATCH"
        );

        insightText.setTextColor(
                Color.WHITE
        );

        insightText.setTextSize(16);

        insightText.setGravity(
                Gravity.CENTER
        );

        insightText.setPadding(
                25,
                25,
                25,
                25
        );

        insightText.setBackgroundColor(
                Color.rgb(
                        24,
                        24,
                        28
                )
        );

        Button alertsButton =
                new Button(this);

        updateAlertButton(
                alertsButton
        );

        alertsButton.setTextColor(
                Color.WHITE
        );

        alertsButton.setOnClickListener(v -> {

            alertsEnabled =
                    !alertsEnabled;

            preferences.edit()
                    .putBoolean(
                            "alerts_enabled",
                            alertsEnabled
                    )
                    .apply();

            /*
             * No ACTION_UPDATE_SETTINGS here.
             * This avoids the previous compile error.
             */
            updateAlertButton(
                    alertsButton
            );

            updateBatteryDisplay();

            if (alertsEnabled) {

                Toast.makeText(
                        this,
                        "⚡ Charge alerts enabled",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                Toast.makeText(
                        this,
                        "🔕 Charge alerts disabled",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        Button soundButton =
                new Button(this);

        soundButton.setText(
                "🎵  CHOOSE ALERT SOUND"
        );

        soundButton.setTextColor(
                Color.WHITE
        );

        soundButton.setOnClickListener(
                v -> chooseAlertSound()
        );

        Button historyButton =
                new Button(this);

        historyButton.setText(
                "📊  CHARGING HISTORY"
        );

        historyButton.setTextColor(
                Color.WHITE
        );

        historyButton.setOnClickListener(
                v ->
                        Toast.makeText(
                                this,
                                "📊 Charging history module is next",
                                Toast.LENGTH_SHORT
                        ).show()
        );

        Button aiButton =
                new Button(this);

        aiButton.setText(
                "🧠  AI INSIGHTS"
        );

        aiButton.setTextColor(
                Color.WHITE
        );

        aiButton.setOnClickListener(
                v -> showSmartInsight()
        );

        alertStatusText =
                new TextView(this);

        alertStatusText.setText(
                "🛡 ChargeGuard is protecting your battery"
        );

        alertStatusText.setTextColor(
                Color.GRAY
        );

        alertStatusText.setTextSize(12);

        alertStatusText.setGravity(
                Gravity.CENTER
        );

        root.addView(logo);
        root.addView(aiLabel);
        root.addView(batteryText);
        root.addView(chargingText);
        root.addView(insightText);

        addSpace(
                root,
                18
        );

        root.addView(
                alertsButton
        );

        root.addView(
                soundButton
        );

        root.addView(
                historyButton
        );

        root.addView(
                aiButton
        );

        addSpace(
                root,
                12
        );

        root.addView(
                alertStatusText
        );

        setContentView(root);
    }

    private void updateAlertButton(
            Button button) {

        if (alertsEnabled) {

            button.setText(
                    "🔔  ALERTS: ON"
            );

        } else {

            button.setText(
                    "🔕  ALERTS: OFF"
            );
        }
    }

    private void chooseAlertSound() {

        Intent intent =
                new Intent(
                        android.media.RingtoneManager
                                .ACTION_RINGTONE_PICKER
                );

        intent.putExtra(
                android.media.RingtoneManager
                        .EXTRA_RINGTONE_TYPE,
                android.media.RingtoneManager
                        .TYPE_NOTIFICATION
                        |
                android.media.RingtoneManager
                        .TYPE_ALARM
        );

        intent.putExtra(
                android.media.RingtoneManager
                        .EXTRA_RINGTONE_TITLE,
                "Choose ChargeGuard Alert Sound"
        );

        String saved =
                preferences.getString(
                        "alert_sound",
                        null
                );

        if (saved != null) {

            intent.putExtra(
                    android.media.RingtoneManager
                            .EXTRA_RINGTONE_EXISTING_URI,
                    Uri.parse(saved)
            );
        }

        startActivityForResult(
                intent,
                SOUND_PICKER_REQUEST
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode ==
                SOUND_PICKER_REQUEST
                &&
                resultCode ==
                        RESULT_OK
                &&
                data != null) {

            Uri soundUri =
                    data.getParcelableExtra(
                            android.media.RingtoneManager
                                    .EXTRA_RINGTONE_PICKED_URI
                    );

            if (soundUri != null) {

                preferences.edit()
                        .putString(
                                "alert_sound",
                                soundUri.toString()
                        )
                        .apply();

                Toast.makeText(
                        this,
                        "🔥 Your alert sound is saved!",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private void showSmartInsight() {

        Intent batteryIntent =
                registerReceiver(
                        null,
                        new IntentFilter(
                                Intent.ACTION_BATTERY_CHANGED
                        )
                );

        if (batteryIntent == null) {
            return;
        }

        int level =
                batteryIntent.getIntExtra(
                        BatteryManager.EXTRA_LEVEL,
                        -1
                );

        int plugged =
                batteryIntent.getIntExtra(
                        BatteryManager.EXTRA_PLUGGED,
                        -1
                );

        String chargingType =
                getChargingType(
                        plugged
                );

        String message;

        if (level >= 95) {

            message =
                    "🔥 ALMOST FULL\n\n" +
                    "You're at " + level + "%.\n" +
                    "ChargeGuard recommends unplugging soon.";

        } else if (level >= 80) {

            message =
                    "🧠 SMART TIP\n\n" +
                    "You're in the high-charge zone.\n" +
                    "Avoid unnecessary long charging sessions.";

        } else if (level <= 20) {

            message =
                    "🆘 SMART WARNING\n\n" +
                    "Battery is critically low.\n" +
                    "Connect your charger soon.";

        } else {

            message =
                    "🧠 BATTERY HEALTH TIP\n\n" +
                    "Keep your phone away from excessive heat " +
                    "while charging.\n\n" +
                    "ChargeGuard is watching ⚡";
        }

        if (level > 0) {

            message +=
                    "\n\n🔌 Current source: " +
                    chargingType;
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle(
                        "🧠 ChargeGuard AI"
                )
                .setMessage(
                        message
                )
                .setPositiveButton(
                        "OK",
                        null
                )
                .show();
    }

    private void addSpace(
            LinearLayout layout,
            int height) {

        View space =
                new View(this);

        layout.addView(
                space,
                new LinearLayout.LayoutParams(
                        1,
                        height
                )
        );
    }
}