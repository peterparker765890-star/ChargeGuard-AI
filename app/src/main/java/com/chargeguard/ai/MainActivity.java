package com.chargeguard.ai;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
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

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            startForegroundService(serviceIntent);

        } else {

            startService(serviceIntent);
        }
    }

    private void updateBatteryDisplay() {

        Intent batteryIntent =
                registerReceiver(
                        null,
                        new android.content.IntentFilter(
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

        int status =
                batteryIntent.getIntExtra(
                        BatteryManager.EXTRA_STATUS,
                        -1
                );

        boolean charging =
                status == BatteryManager.BATTERY_STATUS_CHARGING
                        || status == BatteryManager.BATTERY_STATUS_FULL;

        batteryText.setText(level + "%");

        if (charging) {

            chargingText.setText("⚡ CHARGING");
            chargingText.setTextColor(
                    Color.rgb(34, 197, 94)
            );

            if (level >= 100) {

                insightText.setText(
                        "🔥 FULL POWER\n\n" +
                        "Battery reached 100%.\n" +
                        "ChargeGuard recommends unplugging now."
                );

            } else if (level >= 90) {

                insightText.setText(
                        "🚨 FINAL CHARGE ZONE\n\n" +
                        "You're above 90%.\n" +
                        "ChargeGuard is watching the battery."
                );

            } else if (level <= 20) {

                insightText.setText(
                        "🆘 LOW BATTERY\n\n" +
                        "Battery is getting low.\n" +
                        "Consider charging soon."
                );

            } else {

                insightText.setText(
                        "🧠 AI BATTERY WATCH\n\n" +
                        "Charging normally.\n" +
                        "ChargeGuard is monitoring your battery."
                );
            }

        } else {

            chargingText.setText("● NOT CHARGING");

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
                Color.rgb(7, 7, 10)
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
                Color.rgb(168, 85, 247)
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

        batteryText.setText("--%");

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
                "Checking..."
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
                Color.rgb(24, 24, 28)
        );

        Button alertsButton =
                new Button(this);

        if (alertsEnabled) {

            alertsButton.setText(
                    "🔔  ALERTS: ON"
            );

        } else {

            alertsButton.setText(
                    "🔕  ALERTS: OFF"
            );
        }

        alertsButton.setTextColor(
                Color.WHITE
        );

        alertsButton.setOnClickListener(v -> {

            alertsEnabled = !alertsEnabled;

            preferences.edit()
                    .putBoolean(
                            "alerts_enabled",
                            alertsEnabled
                    )
                    .apply();

            Intent serviceIntent =
                    new Intent(
                            this,
                            ChargeAlertService.class
                    );

            serviceIntent.setAction(
                    ChargeAlertService.ACTION_UPDATE_SETTINGS
            );

            startService(serviceIntent);

            if (alertsEnabled) {

                alertsButton.setText(
                        "🔔  ALERTS: ON"
                );

                Toast.makeText(
                        this,
                        "Charge alerts enabled ⚡",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                alertsButton.setText(
                        "🔕  ALERTS: OFF"
                );

                Toast.makeText(
                        this,
                        "Charge alerts disabled",
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

        historyButton.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Charging history module is next 📊",
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

        aiButton.setOnClickListener(v ->
                showSmartInsight()
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

        addSpace(root, 18);

        root.addView(alertsButton);
        root.addView(soundButton);
        root.addView(historyButton);
        root.addView(aiButton);

        addSpace(root, 12);

        root.addView(alertStatusText);

        setContentView(root);
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
                        | android.media.RingtoneManager
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
                && resultCode == RESULT_OK
                && data != null) {

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
                        new android.content.IntentFilter(
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

        String message;

        if (level >= 95) {

            message =
                    "🔥 Almost full.\n\n" +
                    "ChargeGuard recommends unplugging soon.";

        } else if (level >= 80) {

            message =
                    "🧠 Smart Tip\n\n" +
                    "You're in the high-charge zone.\n" +
                    "Avoid unnecessary charging for long periods.";

        } else if (level <= 20) {

            message =
                    "🆘 Smart Warning\n\n" +
                    "Battery is critically low.\n" +
                    "Connect your charger soon.";

        } else {

            message =
                    "🧠 Battery Health Tip\n\n" +
                    "Keep your phone away from excessive heat " +
                    "while charging.\n\n" +
                    "ChargeGuard is watching ⚡";
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle("🧠 ChargeGuard AI")
                .setMessage(message)
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