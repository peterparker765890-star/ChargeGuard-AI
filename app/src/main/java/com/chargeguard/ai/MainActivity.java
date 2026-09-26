package com.chargeguard.ai;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
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

    private boolean alertsEnabled = true;

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int level = intent.getIntExtra(
                    BatteryManager.EXTRA_LEVEL, -1
            );

            int status = intent.getIntExtra(
                    BatteryManager.EXTRA_STATUS, -1
            );

            boolean charging =
                    status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            batteryText.setText(level + "%");

            if (charging) {
                chargingText.setText("⚡ CHARGING");
                chargingText.setTextColor(Color.rgb(34, 197, 94));

                if (level >= 100) {
                    insightText.setText(
                            "💯 FULL POWER\n\n" +
                            "Your battery is completely charged.\n" +
                            "Time to unplug, legend ⚡"
                    );
                } else if (level >= 90) {
                    insightText.setText(
                            "👀 90% ZONE\n\n" +
                            "You're almost full.\n" +
                            "ChargeGuard is keeping an eye on it."
                    );
                } else {
                    insightText.setText(
                            "🧠 AI STATUS\n\n" +
                            "Charging normally.\n" +
                            "I'll keep watching your battery."
                    );
                }

            } else {
                chargingText.setText("● NOT CHARGING");
                chargingText.setTextColor(Color.LTGRAY);

                insightText.setText(
                        "🧠 AI STATUS\n\n" +
                        "Battery monitoring is active."
                );
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createNotificationChannel();
        buildInterface();

        IntentFilter filter = new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED
        );

        registerReceiver(batteryReceiver, filter);
    }

    private void buildInterface() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 45, 28, 30);
        root.setBackgroundColor(Color.rgb(9, 9, 11));

        TextView logo = new TextView(this);
        logo.setText("⚡ CHARGEGUARD AI");
        logo.setTextColor(Color.WHITE);
        logo.setTextSize(26);
        logo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        logo.setGravity(Gravity.CENTER);

        TextView tagline = new TextView(this);
        tagline.setText("Power Up. Stay Smart.");
        tagline.setTextColor(Color.rgb(168, 85, 247));
        tagline.setTextSize(15);
        tagline.setGravity(Gravity.CENTER);
        tagline.setPadding(0, 8, 0, 35);

        batteryText = new TextView(this);
        batteryText.setText("--%");
        batteryText.setTextColor(Color.WHITE);
        batteryText.setTextSize(62);
        batteryText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        batteryText.setGravity(Gravity.CENTER);

        chargingText = new TextView(this);
        chargingText.setText("Checking...");
        chargingText.setTextColor(Color.LTGRAY);
        chargingText.setTextSize(17);
        chargingText.setGravity(Gravity.CENTER);
        chargingText.setPadding(0, 5, 0, 25);

        insightText = new TextView(this);
        insightText.setText("🧠 AI STATUS");
        insightText.setTextColor(Color.WHITE);
        insightText.setTextSize(16);
        insightText.setGravity(Gravity.CENTER);
        insightText.setPadding(25, 25, 25, 25);
        insightText.setBackgroundColor(Color.rgb(24, 24, 27));

        Button alertsButton = new Button(this);
        alertsButton.setText("🔔  ALERT SETTINGS");
        alertsButton.setTextColor(Color.WHITE);

        alertsButton.setOnClickListener(v -> {

            alertsEnabled = !alertsEnabled;

            if (alertsEnabled) {
                alertsButton.setText("🔔  ALERTS: ON");
                Toast.makeText(
                        this,
                        "Charge alerts enabled ⚡",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                alertsButton.setText("🔕  ALERTS: OFF");
                Toast.makeText(
                        this,
                        "Charge alerts disabled",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        Button historyButton = new Button(this);
        historyButton.setText("📊  CHARGING HISTORY");
        historyButton.setTextColor(Color.WHITE);

        historyButton.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Charging history is coming next 📊",
                        Toast.LENGTH_SHORT
                ).show()
        );

        Button aiButton = new Button(this);
        aiButton.setText("🧠  AI INSIGHTS");
        aiButton.setTextColor(Color.WHITE);

        aiButton.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "AI charging analysis coming next 🤖",
                        Toast.LENGTH_SHORT
                ).show()
        );

        root.addView(logo);
        root.addView(tagline);
        root.addView(batteryText);
        root.addView(chargingText);
        root.addView(insightText);

        addSpace(root, 18);

        root.addView(alertsButton);
        root.addView(historyButton);
        root.addView(aiButton);

        setContentView(root);
    }

    private void addSpace(LinearLayout layout, int height) {

        View space = new View(this);

        layout.addView(
                space,
                new LinearLayout.LayoutParams(
                        1,
                        height
                )
        );
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            "chargeguard_alerts",
                            "ChargeGuard Alerts",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "Battery charging alerts from ChargeGuard AI"
            );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        try {
            unregisterReceiver(batteryReceiver);
        } catch (Exception ignored) {
        }
    }
}
    