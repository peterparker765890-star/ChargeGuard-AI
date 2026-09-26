package com.chargeguard.ai;

import android.app.Activity;
import android.os.Bundle;
import android.os.BatteryManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView batteryText;
    private TextView chargingText;

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
            } else {
                chargingText.setText("● NOT CHARGING");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(35, 60, 35, 40);
        root.setBackgroundColor(Color.rgb(9, 9, 11));

        TextView logo = new TextView(this);
        logo.setText("⚡ ChargeGuard AI");
        logo.setTextColor(Color.WHITE);
        logo.setTextSize(28);
        logo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        logo.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("Your battery. Your rules. ⚡");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 12, 0, 50);

        batteryText = new TextView(this);
        batteryText.setText("--%");
        batteryText.setTextColor(Color.WHITE);
        batteryText.setTextSize(64);
        batteryText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        batteryText.setGravity(Gravity.CENTER);

        chargingText = new TextView(this);
        chargingText.setText("Checking...");
        chargingText.setTextColor(Color.rgb(192, 132, 252));
        chargingText.setTextSize(18);
        chargingText.setGravity(Gravity.CENTER);
        chargingText.setPadding(0, 5, 0, 45);

        TextView status = new TextView(this);
        status.setText(
                "🧠 AI STATUS\n\n" +
                "ChargeGuard is watching your battery.\n" +
                "90% and 100% alerts will be added next."
        );
        status.setTextColor(Color.WHITE);
        status.setTextSize(16);
        status.setPadding(30, 30, 30, 30);
        status.setGravity(Gravity.CENTER);
        status.setBackgroundColor(Color.rgb(24, 24, 27));

        root.addView(logo);
        root.addView(subtitle);
        root.addView(batteryText);
        root.addView(chargingText);
        root.addView(status);

        setContentView(root);

        IntentFilter filter = new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED
        );

        registerReceiver(batteryReceiver, filter);
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