package com.hugzzbotzx.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.provider.Settings;
import android.text.InputType;
import android.text.format.DateUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * HugzzBotzX DevLab V6.1 — comic-style local device control and authorized diagnostics.
 * All control actions open Android's own settings UI or operate on this device.
 * ADB snippets are copied for the user to run in Termux after target-side authorization.
 */
public class MainActivity extends Activity {
    private static final int INK = Color.rgb(21, 19, 42);
    private static final int PAPER = Color.rgb(255, 253, 248);
    private static final int VIOLET = Color.rgb(109, 74, 255);
    private static final int PINK = Color.rgb(255, 79, 163);
    private static final int CYAN = Color.rgb(25, 190, 245);
    private static final int YELLOW = Color.rgb(255, 216, 77);
    private static final int MINT = Color.rgb(57, 205, 158);
    private static final int LAV = Color.rgb(238, 232, 255);
    private static final int PEACH = Color.rgb(255, 229, 212);
    private static final int SKY = Color.rgb(221, 246, 255);

    private LinearLayout root;
    private LinearLayout content;
    private SharedPreferences prefs;
    private Handler handler = new Handler(Looper.getMainLooper());
    private CameraManager cameraManager;
    private String cameraId;
    private boolean torchOn = false;
    private boolean sosRunning = false;
    private Runnable sosRunnable;
    private Runnable focusRunnable;
    private long focusEndAt = 0L;
    private TextView focusStatus;
    private boolean familyUnlocked = false;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("hugzz_v61", MODE_PRIVATE);
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        buildShell();
        showHome();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSos(false);
    }

    @Override
    protected void onDestroy() {
        stopSos(false);
        stopFocusTimer(false);
        super.onDestroy();
    }

    private void buildShell() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PAPER);

        LinearLayout header = horizontal();
        header.setPadding(dp(14), dp(10), dp(14), dp(8));
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.hugzzbotzx_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        header.addView(logo, new LinearLayout.LayoutParams(dp(54), dp(54)));

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.VERTICAL);
        TextView appName = text("HUGZZBOTZX", 22, INK, true);
        TextView edition = text("V6.1 • COMIC COMMAND CONTROL", 10, VIOLET, true);
        brand.addView(appName);
        brand.addView(edition);
        LinearLayout.LayoutParams brandParams = new LinearLayout.LayoutParams(0, -2, 1f);
        brandParams.leftMargin = dp(10);
        header.addView(brand, brandParams);

        TextView badge = text("61", 13, Color.WHITE, true);
        badge.setGravity(Gravity.CENTER);
        badge.setBackgroundColor(PINK);
        header.addView(badge, new LinearLayout.LayoutParams(dp(42), dp(38)));
        root.addView(header);

        ScrollView pageScroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(8), dp(16), dp(20));
        pageScroll.addView(content);
        root.addView(pageScroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        HorizontalScrollView navScroll = new HorizontalScrollView(this);
        navScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout nav = horizontal();
        nav.setPadding(dp(5), dp(5), dp(5), dp(5));
        nav.setBackgroundColor(INK);
        String[] tabs = {"HOME", "CONTROL", "DEVICES", "APPS", "TOOLS", "BUG LAB", "FAMILY", "LOGS", "MORE"};
        for (String tab : tabs) {
            Button b = new Button(this);
            b.setText(tab);
            b.setAllCaps(false);
            b.setTextSize(10);
            b.setTextColor(Color.WHITE);
            b.setMinWidth(dp(78));
            b.setBackgroundColor(Color.TRANSPARENT);
            b.setOnClickListener(v -> navigate(tab));
            nav.addView(b, new LinearLayout.LayoutParams(-2, dp(52)));
        }
        navScroll.addView(nav);
        root.addView(navScroll);
        setContentView(root);
    }

    private void navigate(String tab) {
        if (!"FAMILY".equals(tab)) familyUnlocked = false;
        switch (tab) {
            case "HOME": showHome(); break;
            case "CONTROL": showControlCenter(); break;
            case "DEVICES": showDevices(); break;
            case "APPS": showApps(); break;
            case "TOOLS": showTools(); break;
            case "BUG LAB": showBugLab(); break;
            case "FAMILY": requestFamilyAccess(); break;
            case "LOGS": showLogs(); break;
            default: showMore(); break;
        }
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private LinearLayout horizontal() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        return row;
    }

    private TextView text(String value, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        t.setPadding(dp(3), dp(3), dp(3), dp(3));
        return t;
    }

    private Button button(String label, int color) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setTextSize(12);
        b.setMinHeight(dp(50));
        b.setBackgroundResource(color == PINK ? R.drawable.button_pink : R.drawable.button);
        return b;
    }

    private LinearLayout card(String heading, String body, int tint) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(14), dp(16), dp(14));
        box.setBackgroundResource(R.drawable.panel);
        box.setBackgroundTintList(android.content.res.ColorStateList.valueOf(tint));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(13);
        box.setLayoutParams(p);
        box.addView(text(heading, 17, INK, true));
        TextView detail = text(body, 12, INK, false);
        detail.setPadding(dp(3), dp(7), dp(3), dp(3));
        box.addView(detail);
        content.addView(box);
        return box;
    }

    private void addButton(String label, int tint, View.OnClickListener action) {
        Button b = button(label, tint);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(9);
        content.addView(b, p);
        b.setOnClickListener(action);
    }

    private void comic(String line, int color) {
        TextView bubble = text("★  " + line, 14, INK, true);
        bubble.setBackgroundColor(color);
        bubble.setPadding(dp(16), dp(14), dp(16), dp(14));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(14);
        content.addView(bubble, p);
    }

    private void clearPage() {
        content.removeAllViews();
    }

    private void showHome() {
        clearPage();
        comic("WELCOME, OPERATOR! A colorful command center for your own device and authorized support.", YELLOW);
        card("DEVICE SNAPSHOT", deviceSummary(), SKY);
        card("QUICK CONTROL", "Open Android panels for Wi-Fi, Bluetooth, location, display, sound, battery and notifications. Android remains in charge of protected settings.", LAV);
        addButton("OPEN CONTROL CENTER", VIOLET, v -> showControlCenter());
        addButton("RUN FULL DIAGNOSTIC", PINK, v -> showDiagnostics());
        addButton("OPEN ADB TOOLKIT", VIOLET, v -> showDevices());
        card("SAFETY STATUS", "LOCAL-FIRST • Visible actions • User-authorized ADB • No hidden remote access • No destructive payloads", MINT);
    }

    private String deviceSummary() {
        ActivityManager.MemoryInfo mem = new ActivityManager.MemoryInfo();
        ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).getMemoryInfo(mem);
        long ramTotal = mem.totalMem / 1048576L;
        long ramFree = mem.availMem / 1048576L;
        return "Device: " + Build.MANUFACTURER + " " + Build.MODEL
                + "\nAndroid: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")"
                + "\nRAM: " + ramFree + " MB free / " + ramTotal + " MB"
                + "\nStorage: " + storageSummary()
                + "\nBattery: " + batterySummary()
                + "\nNetwork: " + networkSummary();
    }

    private String storageSummary() {
        try {
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            long free = stat.getAvailableBytes() / 1073741824L;
            long total = stat.getTotalBytes() / 1073741824L;
            return free + " GB free / " + total + " GB total";
        } catch (Exception e) {
            return "Unavailable";
        }
    }

    private String batterySummary() {
        Intent i = registerReceiver(null, new android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (i == null) return "Unknown";
        int level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        int temp = i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        int plugged = i.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        String state = plugged == 0 ? "On battery" : "Charging/plugged";
        return (level < 0 ? "Unknown" : Math.round(level * 100f / Math.max(scale, 1)) + "%")
                + (temp < 0 ? "" : " • " + String.format(Locale.getDefault(), "%.1f°C", temp / 10f))
                + " • " + state;
    }

    private String networkSummary() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkCapabilities c = cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (c == null) return "Offline";
            if (c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return "Wi-Fi";
            if (c.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return "Mobile data";
            if (c.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) return "Ethernet";
            return "Connected";
        } catch (Exception e) {
            return "Unavailable";
        }
    }

    private void showControlCenter() {
        clearPage();
        comic("CONTROL CENTER — quick access to Android's own system controls.", CYAN);
        card("HOW IT WORKS", "Each button opens the official Android settings/panel. Protected switches are changed by the device owner, not silently by this app.", LAV);
        addButton("WI-FI PANEL", VIOLET, v -> openWifiPanel());
        addButton("INTERNET CONNECTIVITY PANEL", PINK, v -> openSystemPanel(Settings.Panel.ACTION_INTERNET_CONNECTIVITY, Settings.ACTION_WIRELESS_SETTINGS));
        addButton("BLUETOOTH SETTINGS", VIOLET, v -> openSettings(Settings.ACTION_BLUETOOTH_SETTINGS));
        addButton("LOCATION SETTINGS", PINK, v -> openSettings(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        addButton("DISPLAY & BRIGHTNESS", VIOLET, v -> openSettings(Settings.ACTION_DISPLAY_SETTINGS));
        addButton("VOLUME PANEL", PINK, v -> openSystemPanel(Settings.Panel.ACTION_VOLUME, Settings.ACTION_SOUND_SETTINGS));
        addButton("SOUND & VIBRATION", VIOLET, v -> openSettings(Settings.ACTION_SOUND_SETTINGS));
        addButton("BATTERY / POWER SAVER", PINK, v -> openSettings(Settings.ACTION_BATTERY_SAVER_SETTINGS));
        addButton("STORAGE SETTINGS", VIOLET, v -> openSettings(Settings.ACTION_INTERNAL_STORAGE_SETTINGS));
        addButton("APP NOTIFICATIONS", PINK, v -> openAppNotificationSettings());
        addButton("APP PERMISSIONS", VIOLET, v -> openAppDetails());
        addButton("ALL APPS MANAGER", PINK, v -> openSettings(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS));
        addButton("DATE & TIME", VIOLET, v -> openSettings(Settings.ACTION_DATE_SETTINGS));
        addButton("PRIVACY & SECURITY", PINK, v -> openSettings(Settings.ACTION_PRIVACY_SETTINGS));
        addButton("AIRPLANE MODE SETTINGS", VIOLET, v -> openSettings(Settings.ACTION_AIRPLANE_MODE_SETTINGS));
        addButton("BATTERY OPTIMIZATION", PINK, v -> openSettings(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
    }

    private void openWifiPanel() {
        openSystemPanel(Settings.Panel.ACTION_WIFI, Settings.ACTION_WIFI_SETTINGS);
    }

    private void openSystemPanel(String panelAction, String fallbackAction) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                startActivity(new Intent(panelAction));
                return;
            } catch (Exception ignored) { }
        }
        openSettings(fallbackAction);
    }

    private void openSettings(String action) {
        try {
            startActivity(new Intent(action));
        } catch (ActivityNotFoundException e) {
            toast("This settings page is not available on this Android build.");
        } catch (Exception e) {
            toast("Unable to open system settings.");
        }
    }

    private void openAppDetails() {
        try {
            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            i.setData(android.net.Uri.parse("package:" + getPackageName()));
            startActivity(i);
        } catch (Exception e) {
            toast("Unable to open app settings.");
        }
    }

    private void openAppNotificationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent i = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            i.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            try { startActivity(i); } catch (Exception e) { openAppDetails(); }
        } else {
            openAppDetails();
        }
    }

    private void showDevices() {
        clearPage();
        comic("AUTHORIZED DEVICE DESK — prepare an ADB session with approval on the target device.", YELLOW);
        card("PAIRING CHECKLIST", "1. Put both devices on a trusted network.\n2. On target: Developer options → Wireless debugging.\n3. Choose Pair device with pairing code.\n4. Enter the displayed address, port and pairing code in Termux.\n5. Confirm the RSA authorization prompt on the target.", SKY);
        addButton("OPEN WIRELESS DEBUGGING", VIOLET, v -> openSettings(Settings.ACTION_WIRELESS_SETTINGS));
        addButton("COPY ADB DEVICE LIST", PINK, v -> copyCommand("adb devices -l"));
        addButton("COPY ANDROID VERSION QUERY", VIOLET, v -> copyCommand("adb -s SERIAL shell getprop ro.build.version.release"));
        addButton("COPY BATTERY REPORT QUERY", PINK, v -> copyCommand("adb -s SERIAL shell dumpsys battery"));
        addButton("COPY STORAGE REPORT QUERY", VIOLET, v -> copyCommand("adb -s SERIAL shell df -h /data"));
        addButton("COPY RECENT LOGCAT QUERY", PINK, v -> copyCommand("adb -s SERIAL logcat -d -t 300"));
        addButton("COPY USER APP LIST QUERY", VIOLET, v -> copyCommand("adb -s SERIAL shell pm list packages -3"));
        card("IMPORTANT", "These are read-only diagnostic commands. Replace SERIAL with the authorized device ID and run them yourself in Termux. Pairing-note text generated by this app is only a reminder, not an ADB pairing credential.", PEACH);
        addButton("OPEN TERMUX (IF INSTALLED)", PINK, v -> launchPackage("com.termux"));
    }

    private void copyCommand(String command) {
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("HugzzBotzX ADB diagnostic", command));
        log("Copied ADB diagnostic command");
        toast("Command copied. Review it before running in Termux.");
    }

    private void showApps() {
        clearPage();
        comic("APP MANAGER — launch apps or open their Android info page.", PINK);
        card("PRIVACY NOTE", "This list uses launcher-visible apps only. It does not request broad access to every installed package.", LAV);
        Intent query = new Intent(Intent.ACTION_MAIN, null);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> results = getPackageManager().queryIntentActivities(query, 0);
        Collections.sort(results, (a, b) -> String.valueOf(a.loadLabel(getPackageManager())).compareToIgnoreCase(String.valueOf(b.loadLabel(getPackageManager()))));
        if (results.isEmpty()) {
            card("NO LAUNCHER APPS FOUND", "Android did not return any launchable apps.", PEACH);
            return;
        }
        for (ResolveInfo info : results) {
            String label = String.valueOf(info.loadLabel(getPackageManager()));
            String pkg = info.activityInfo.packageName;
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setPadding(dp(10), dp(8), dp(10), dp(8));
            item.setBackgroundResource(R.drawable.panel);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
            p.bottomMargin = dp(7);
            content.addView(item, p);
            ImageView icon = new ImageView(this);
            try { icon.setImageDrawable(info.loadIcon(getPackageManager())); } catch (Exception ignored) { }
            item.addView(icon, new LinearLayout.LayoutParams(dp(38), dp(38)));
            LinearLayout names = new LinearLayout(this);
            names.setOrientation(LinearLayout.VERTICAL);
            TextView appName = text(label, 13, INK, true);
            TextView packageName = text(pkg, 10, VIOLET, false);
            names.addView(appName);
            names.addView(packageName);
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, -2, 1f);
            nameParams.leftMargin = dp(8);
            item.addView(names, nameParams);
            Button launch = button("OPEN", VIOLET);
            launch.setTextSize(10);
            item.addView(launch, new LinearLayout.LayoutParams(dp(76), dp(44)));
            launch.setOnClickListener(v -> launchPackage(pkg));
            item.setOnLongClickListener(v -> { openPackageInfo(pkg); return true; });
        }
        card("TIP", "Tap OPEN to launch. Long-press an app row to open Android's App info page, where its owner can review permissions or uninstall it.", SKY);
    }

    private void launchPackage(String packageName) {
        try {
            Intent launch = getPackageManager().getLaunchIntentForPackage(packageName);
            if (launch == null) { toast("No launch activity for this app."); return; }
            startActivity(launch);
        } catch (Exception e) {
            toast("Unable to launch app.");
        }
    }

    private void openPackageInfo(String packageName) {
        try {
            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            i.setData(android.net.Uri.parse("package:" + packageName));
            startActivity(i);
        } catch (Exception e) {
            toast("App info unavailable.");
        }
    }

    private void showTools() {
        clearPage();
        comic("TOOLS — utilities for this device, with visible Android permission prompts.", PEACH);
        addButton("FLASHLIGHT ON / OFF", VIOLET, v -> toggleFlashlight());
        addButton("SLOW SOS FLASH • 10 SEC", PINK, v -> startSos());
        addButton("STOP SOS / FLASH", VIOLET, v -> stopSos(true));
        addButton("SCREEN TIME / USAGE ACCESS", PINK, v -> openSettings(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        addButton("OPEN APP INVENTORY", VIOLET, v -> showApps());
        addButton("FOCUS TIMER", PINK, v -> showFocusTimer());
        card("FLASH SAFETY", "SOS uses a slow pattern and stops automatically after 10 seconds. No rapid strobe mode is provided.", YELLOW);
    }

    private boolean findFlashCamera() {
        if (cameraId != null) return true;
        try {
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
                Boolean available = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer facing = c.get(CameraCharacteristics.LENS_FACING);
                if (Boolean.TRUE.equals(available) && Integer.valueOf(CameraCharacteristics.LENS_FACING_BACK).equals(facing)) {
                    cameraId = id;
                    return true;
                }
            }
        } catch (Exception ignored) { }
        return false;
    }

    private void toggleFlashlight() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { toast("Flashlight API unavailable."); return; }
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 61);
            toast("Allow camera permission, then tap flashlight again.");
            return;
        }
        if (!findFlashCamera()) { toast("No rear flash found."); return; }
        try {
            torchOn = !torchOn;
            cameraManager.setTorchMode(cameraId, torchOn);
            log("Flashlight " + (torchOn ? "ON" : "OFF"));
            toast(torchOn ? "Flash ON" : "Flash OFF");
        } catch (Exception e) {
            torchOn = false;
            toast("Unable to control flashlight on this device.");
        }
    }

    private void startSos() {
        if (sosRunning) return;
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 62);
            toast("Allow camera permission, then tap SOS again.");
            return;
        }
        if (!findFlashCamera()) { toast("No rear flash found."); return; }
        sosRunning = true;
        final long finishAt = System.currentTimeMillis() + 10000L;
        final boolean[] on = {false};
        sosRunnable = new Runnable() {
            @Override public void run() {
                if (!sosRunning || System.currentTimeMillis() >= finishAt) { stopSos(true); return; }
                try {
                    on[0] = !on[0];
                    cameraManager.setTorchMode(cameraId, on[0]);
                    torchOn = on[0];
                    handler.postDelayed(this, on[0] ? 450L : 550L);
                } catch (Exception e) {
                    stopSos(false);
                    toast("SOS stopped: flashlight unavailable.");
                }
            }
        };
        log("SOS flashlight started");
        handler.post(sosRunnable);
    }

    private void stopSos(boolean notify) {
        sosRunning = false;
        if (sosRunnable != null) handler.removeCallbacks(sosRunnable);
        sosRunnable = null;
        if (torchOn && cameraManager != null && cameraId != null) {
            try { cameraManager.setTorchMode(cameraId, false); } catch (Exception ignored) { }
        }
        torchOn = false;
        if (notify) { log("SOS / flashlight stopped"); toast("Flashlight stopped."); }
    }

    private void showFocusTimer() {
        clearPage();
        comic("FOCUS MODE — a local timer that never blocks device functions.", YELLOW);
        focusStatus = text(focusEndAt > System.currentTimeMillis() ? remainingFocus() : "Ready for a focus session", 22, VIOLET, true);
        focusStatus.setGravity(Gravity.CENTER);
        content.addView(focusStatus, new LinearLayout.LayoutParams(-1, dp(76)));
        addButton("START 15 MINUTES", VIOLET, v -> startFocusTimer(15));
        addButton("START 25 MINUTES", PINK, v -> startFocusTimer(25));
        addButton("START 45 MINUTES", VIOLET, v -> startFocusTimer(45));
        addButton("STOP TIMER", PINK, v -> stopFocusTimer(true));
        if (focusEndAt > System.currentTimeMillis()) updateFocusDisplay();
        card("WHAT IT DOES", "Shows a countdown and records the completed/stopped session. It does not lock apps or restrict another person's device.", LAV);
    }

    private void startFocusTimer(int minutes) {
        stopFocusTimer(false);
        focusEndAt = System.currentTimeMillis() + minutes * 60000L;
        prefs.edit().putLong("focus_end", focusEndAt).apply();
        log("Focus timer started: " + minutes + " min");
        showFocusTimer();
        updateFocusDisplay();
    }

    private String remainingFocus() {
        long seconds = Math.max(0L, (focusEndAt - System.currentTimeMillis()) / 1000L);
        return String.format(Locale.getDefault(), "%02d:%02d", seconds / 60L, seconds % 60L);
    }

    private void updateFocusDisplay() {
        if (focusStatus == null) return;
        if (focusEndAt <= System.currentTimeMillis()) {
            focusStatus.setText("FOCUS COMPLETE!");
            focusEndAt = 0L;
            prefs.edit().remove("focus_end").apply();
            log("Focus timer completed");
            return;
        }
        focusStatus.setText(remainingFocus());
        if (focusRunnable != null) handler.removeCallbacks(focusRunnable);
        focusRunnable = this::updateFocusDisplay;
        handler.postDelayed(focusRunnable, 1000L);
    }

    private void stopFocusTimer(boolean logAction) {
        if (focusRunnable != null) handler.removeCallbacks(focusRunnable);
        focusRunnable = null;
        focusEndAt = 0L;
        prefs.edit().remove("focus_end").apply();
        if (logAction) { log("Focus timer stopped"); toast("Focus timer stopped."); }
    }

    private void showBugLab() {
        clearPage();
        comic("BUG LAB — bounded performance checks on THIS phone or your emulator.", PEACH);
        addButton("CPU MICRO-BENCH • 2 SEC", VIOLET, v -> runCpuTest());
        addButton("MEMORY ALLOCATION • 8 MB", PINK, v -> runMemoryTest());
        addButton("SAFE EXCEPTION HANDLER DEMO", VIOLET, v -> runExceptionDemo());
        addButton("NETWORK CONNECTIVITY CHECK", PINK, v -> { log("Network check: " + networkSummary()); toast(networkSummary()); });
        addButton("OPEN DIAGNOSTIC SUMMARY", VIOLET, v -> showDiagnostics());
        card("LIMITS", "The tests are time- and memory-bounded, run only inside this app, and do not target another device or app. No crash payloads, flooding, exploits or bootloop tools.", YELLOW);
    }

    private void runCpuTest() {
        Toast.makeText(this, "Running 2-second CPU check…", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            long start = System.nanoTime();
            long end = start + 2_000_000_000L;
            long x = 1L;
            while (System.nanoTime() < end) x = x * 2862933555777941757L + 3037000493L;
            final String result = "CPU micro-test complete • checksum " + Long.toHexString(x);
            runOnUiThread(() -> { log("CPU micro-benchmark completed"); toast(result); });
        }, "HugzzCpuTest").start();
    }

    private void runMemoryTest() {
        try {
            byte[] memory = new byte[8 * 1024 * 1024];
            for (int i = 0; i < memory.length; i += 4096) memory[i] = (byte) i;
            log("8 MB memory allocation test completed");
            toast("8 MB allocation test completed.");
        } catch (OutOfMemoryError e) {
            log("Memory test skipped: insufficient memory");
            toast("Test skipped: insufficient available memory.");
        }
    }

    private void runExceptionDemo() {
        try {
            throw new IllegalStateException("HugzzBotzX controlled demo exception");
        } catch (RuntimeException e) {
            log("Exception demo caught successfully");
            new AlertDialog.Builder(this).setTitle("Exception handled")
                    .setMessage("A demo exception was caught safely. The application remains running.")
                    .setPositiveButton("OK", null).show();
        }
    }

    private void showFamily() {
        clearPage();
        comic("FAMILY HQ — shared routines, transparent reminders and local privacy.", PINK);
        card("PARENT PIN", prefs.contains("parent_pin_hash") ? "A local parent PIN is configured." : "No local parent PIN configured yet.", LAV);
        addButton("SET / CHANGE PARENT PIN", VIOLET, v -> setParentPin());
        addButton("ADD FAMILY ROUTINE", PINK, v -> addRoutine());
        addButton("VIEW FAMILY ROUTINES", VIOLET, v -> viewRoutines());
        addButton("OPEN DIGITAL WELLBEING", PINK, v -> openSettings(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        addButton("OPEN EMERGENCY DIALER", VIOLET, v -> openDialer());
        card("PARENTAL CONTROL NOTE", "This app offers planning tools and Usage Access reports. Hard app blocking, device-admin enforcement and remote control are not silently enabled; use Android Family Link or managed-device enrollment for those functions.", SKY);
    }

    private void requestFamilyAccess() {
        if (familyUnlocked) { showFamily(); return; }
        if (!prefs.contains("parent_pin_hash") || !prefs.contains("parent_pin_salt")) {
            familyUnlocked = true;
            showFamily();
            return;
        }
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setHint("Enter parent PIN");
        new AlertDialog.Builder(this).setTitle("Family Center locked").setMessage("Enter the local parent PIN to open Family Center.")
                .setView(input).setNegativeButton("Cancel", null)
                .setPositiveButton("Unlock", (d, w) -> {
                    if (verifyParentPin(input.getText().toString())) {
                        familyUnlocked = true;
                        showFamily();
                    } else {
                        toast("Incorrect PIN.");
                        log("Family Center unlock rejected");
                    }
                }).show();
    }

    private void setParentPin() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setHint("Enter 4–8 digits");
        new AlertDialog.Builder(this).setTitle("Set / change parent PIN").setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (d, w) -> {
                    String value = input.getText().toString();
                    if (!value.matches("[0-9]{4,8}")) { toast("PIN must contain 4–8 digits."); return; }
                    try {
                        byte[] salt = new byte[16];
                        new SecureRandom().nextBytes(salt);
                        byte[] hash = derivePin(value.toCharArray(), salt);
                        prefs.edit()
                                .putString("parent_pin_salt", Base64.encodeToString(salt, Base64.NO_WRAP))
                                .putString("parent_pin_hash", Base64.encodeToString(hash, Base64.NO_WRAP))
                                .apply();
                        familyUnlocked = true;
                        log("Parent PIN changed");
                        toast("Parent PIN saved on this device.");
                    } catch (Exception e) {
                        toast("Could not save PIN on this Android version.");
                    }
                }).show();
    }

    private byte[] derivePin(char[] pin, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(pin, salt, 120000, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
            java.util.Arrays.fill(pin, '\0');
        }
    }

    private boolean verifyParentPin(String candidate) {
        if (candidate == null || candidate.length() < 4 || candidate.length() > 8) return false;
        try {
            byte[] salt = Base64.decode(prefs.getString("parent_pin_salt", ""), Base64.NO_WRAP);
            byte[] expected = Base64.decode(prefs.getString("parent_pin_hash", ""), Base64.NO_WRAP);
            byte[] actual = derivePin(candidate.toCharArray(), salt);
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    private void addRoutine() {
        EditText input = new EditText(this);
        input.setHint("Example: Homework at 16:00");
        new AlertDialog.Builder(this).setTitle("New family routine").setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", (d, w) -> {
                    String value = input.getText().toString().trim();
                    if (value.isEmpty()) return;
                    String old = prefs.getString("routines", "");
                    prefs.edit().putString("routines", value + "\n" + old).apply();
                    log("Family routine added");
                    toast("Routine saved.");
                }).show();
    }

    private void viewRoutines() {
        String items = prefs.getString("routines", "");
        if (items.trim().isEmpty()) items = "No routines saved yet.";
        new AlertDialog.Builder(this).setTitle("Family routines").setMessage(items)
                .setPositiveButton("Done", null)
                .setNeutralButton("Clear all", (d, w) -> { prefs.edit().remove("routines").apply(); log("Family routines cleared"); }).show();
    }

    private void openDialer() {
        try { startActivity(new Intent(Intent.ACTION_DIAL)); }
        catch (Exception e) { toast("Dialer unavailable."); }
    }

    private void showLogs() {
        clearPage();
        comic("LOCAL ACTIVITY LOG — actions recorded on this phone.", YELLOW);
        String logs = prefs.getString("logs", "");
        card("RECENT ACTIVITY", logs.isEmpty() ? "No activity recorded." : logs, LAV);
        addButton("EXPORT / SHARE LOG", VIOLET, v -> shareReport());
        addButton("CLEAR LOCAL LOG", PINK, v -> new AlertDialog.Builder(this)
                .setTitle("Clear log?").setMessage("This removes the locally stored activity log.")
                .setNegativeButton("Cancel", null).setPositiveButton("Clear", (d, w) -> {
                    prefs.edit().remove("logs").apply();
                    showLogs();
                }).show());
    }

    private void showMore() {
        clearPage();
        comic("MORE — app permissions, usage report, support and privacy.", MINT);
        addButton("APP PERMISSIONS", VIOLET, v -> openAppDetails());
        addButton("USAGE ACCESS SETTINGS", PINK, v -> openSettings(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        addButton("USAGE ANALYTICS REPORT", VIOLET, v -> showUsageReport());
        addButton("OPEN ANDROID SETTINGS", PINK, v -> openSettings(Settings.ACTION_SETTINGS));
        addButton("SHARE FULL DEVICE REPORT", VIOLET, v -> shareReport());
        card("ABOUT HUGZZBOTZX V6.1", "Comic Command Control Edition\nLocal device utilities • authorized ADB helpers • app manager • usage analytics • bounded Bug Lab.", SKY);
    }

    private void showDiagnostics() {
        clearPage();
        comic("DIAGNOSTIC SCAN — live snapshot from Android APIs.", CYAN);
        card("DEVICE", deviceSummary(), SKY);
        card("CPU", Runtime.getRuntime().availableProcessors() + " logical processors visible to this app.", LAV);
        card("STORAGE", storageSummary(), PEACH);
        card("BATTERY", batterySummary(), MINT);
        card("NETWORK", networkSummary(), YELLOW);
        addButton("SHARE DIAGNOSTIC REPORT", PINK, v -> shareReport());
    }

    private void showUsageReport() {
        clearPage();
        comic("USAGE ANALYTICS — data is available only after the user grants Usage Access.", CYAN);
        if (!hasUsageAccess()) {
            card("PERMISSION REQUIRED", "Enable Usage Access for HugzzBotzX in Android Settings. Then return and reopen this report.", PEACH);
            addButton("OPEN USAGE ACCESS SETTINGS", VIOLET, v -> openSettings(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            return;
        }
        long end = System.currentTimeMillis();
        long start = end - DateUtils.DAY_IN_MILLIS;
        UsageStatsManager manager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        Map<String, UsageStats> stats = manager.queryAndAggregateUsageStats(start, end);
        if (stats == null || stats.isEmpty()) {
            card("NO USAGE DATA", "Android has not returned usage information for the last 24 hours.", LAV);
            return;
        }
        List<UsageStats> list = new ArrayList<>(stats.values());
        Collections.sort(list, (a, b) -> Long.compare(b.getTotalTimeInForeground(), a.getTotalTimeInForeground()));
        long total = 0L;
        int shown = 0;
        for (UsageStats s : list) {
            long time = s.getTotalTimeInForeground();
            if (time <= 0) continue;
            total += time;
            if (shown++ >= 25) continue;
            String label = resolveAppLabel(s.getPackageName());
            card(label, s.getPackageName() + "\nForeground today: " + DateUtils.formatElapsedTime(time / 1000L), shown % 2 == 0 ? SKY : LAV);
        }
        card("TOTAL VISIBLE USAGE", DateUtils.formatElapsedTime(total / 1000L) + " across apps returned by Android.", YELLOW);
    }

    private boolean hasUsageAccess() {
        try {
            UsageStatsManager m = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
            List<UsageStats> s = m.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS, System.currentTimeMillis());
            return s != null && !s.isEmpty();
        } catch (Exception e) { return false; }
    }

    private String resolveAppLabel(String packageName) {
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(packageName, 0);
            return String.valueOf(getPackageManager().getApplicationLabel(ai));
        } catch (Exception e) { return packageName; }
    }

    private void shareReport() {
        String report = "HugzzBotzX DevLab V6.1\n" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())
                + "\n\n" + deviceSummary()
                + "\nCPU cores: " + Runtime.getRuntime().availableProcessors()
                + "\n\nLOCAL LOGS\n" + prefs.getString("logs", "No log entries")
                + "\n\nNote: report contains device diagnostics selected by the user.";
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, "HugzzBotzX device report");
        share.putExtra(Intent.EXTRA_TEXT, report);
        startActivity(Intent.createChooser(share, "Share diagnostic report"));
    }

    private void log(String message) {
        String old = prefs.getString("logs", "");
        String line = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()) + " • " + message;
        String updated = line + "\n" + old;
        if (updated.length() > 8000) updated = updated.substring(0, 8000);
        prefs.edit().putString("logs", updated).apply();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
