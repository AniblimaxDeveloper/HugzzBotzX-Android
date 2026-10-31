package com.hugzzbotzx.app;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
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
import org.json.JSONObject;
import org.json.JSONArray;
import javax.net.ssl.HttpsURLConnection;
import java.security.cert.X509Certificate;
import java.net.URL;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.ByteArrayOutputStream;
import android.widget.Spinner;
import android.widget.CheckBox;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.provider.OpenableColumns;
import android.provider.MediaStore;
import android.net.Uri;
import android.net.TrafficStats;
import android.database.Cursor;
import android.content.ContentUris;
import android.app.PendingIntent;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.Notification;
import android.app.AlarmManager;

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
 * HugzzBotzX Ultimate System V7.1 — comic-style local device control and authorized diagnostics.
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
    private static final int REQ_MEDIA_PERMS = 701;
    private static final int REQ_PICK_IMAGE = 702;
    private static final int REQ_PICK_VIDEO = 703;
    private static final int REQ_PICK_AUDIO = 704;
    private static final int REQ_PICK_APK = 705;
    private static final int REQ_PICK_HASH = 706;
    private static final int REQ_EXPORT_BACKUP = 707;
    private static final int REQ_IMPORT_BACKUP = 708;
    private static final int REQ_POST_NOTIFICATIONS = 709;
    private Uri lastSelectedUri;
    private String lastSelectedMime = "application/octet-stream";
    private int pendingReminderMinutes = 0;
    private String pendingReminderText = "HugzzBotzX reminder";
    private Runnable monitorRunnable;
    private TextView monitorReadout;
    private boolean monitorRunning = false;

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
        TextView edition = text("V7.1 • LIVE DEVICE MANAGEMENT", 10, VIOLET, true);
        brand.addView(appName);
        brand.addView(edition);
        LinearLayout.LayoutParams brandParams = new LinearLayout.LayoutParams(0, -2, 1f);
        brandParams.leftMargin = dp(10);
        header.addView(brand, brandParams);

        TextView badge = text("71", 13, Color.WHITE, true);
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
        String[] tabs = {"HOME", "CONTROL", "DEVICES", "APPS", "TOOLS", "BUG LAB", "SENDER", "MEDIA", "SECURITY", "ADMIN", "BOTS", "MONITOR", "NOTIFY", "BACKUP", "FAMILY", "LOGS", "MORE"};
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
            case "SENDER": showSender(); break;
            case "MEDIA": showMediaCenter(); break;
            case "SECURITY": showSecurityCenter(); break;
            case "ADMIN": showAdminPanel(); break;
            case "BOTS": showBotManager(); break;
            case "MONITOR": showLiveMonitor(); break;
            case "NOTIFY": showNotificationCenter(); break;
            case "BACKUP": showBackupCenter(); break;
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
        if (monitorRunnable != null) handler.removeCallbacks(monitorRunnable);
        monitorRunning = false;
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
        addButton("OPEN ULTIMATE CONTROL PANEL", PINK, v -> showAdminPanel());
        addButton("MEDIA & FILE CENTER", VIOLET, v -> showMediaCenter());
        addButton("SECURITY AUDIT", PINK, v -> showSecurityCenter());
        card("V7 MODULES", "GLOBAL SENDER (opt-in drafts) • DEVICE REGISTRY • MEDIA LIBRARY • APK PERMISSION AUDIT • LOCAL ADMIN ROLES • BOT FAQ TEMPLATES • NOTIFICATION REMINDERS • BACKUP / RESTORE", YELLOW);
        card("SAFETY STATUS", "LOCAL-FIRST • Visible actions • User-authorized ADB • No hidden remote access • No destructive payloads • Media is accessed only after user selection or runtime permission", MINT);
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

    private void openDeveloperOptions() {
        try {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
        } catch (Exception e) {
            openSettings(Settings.ACTION_SETTINGS);
            toast("Open Developer options manually, then choose Wireless debugging.");
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
        comic("LIVE DEVICE DESK — pair over Wireless ADB only after the device owner approves it.", YELLOW);
        card("PAIRING FLOW", "1. Connect both devices to a trusted Wi-Fi network.\n2. On the target, open Developer options → Wireless debugging.\n3. Tap Pair device with pairing code.\n4. Enter the target IP and PAIRING port here.\n5. Copy the command to Termux and enter the one-time code shown on the target.\n6. After pairing, read the separate CONNECT port from the Wireless debugging main screen and save it below.", SKY);
        addButton("OPEN DEVELOPER OPTIONS", VIOLET, v -> openDeveloperOptions());
        addButton("PAIR A DEVICE — COMMAND WIZARD", PINK, v -> showAdbPairingWizard());
        addButton("VIEW DEVICE REGISTRY", VIOLET, v -> showDeviceRegistry());
        addButton("SUPPORT SESSION MANAGER", PINK, v -> showSupportSessions());
        addButton("COPY ADB DEVICE LIST", VIOLET, v -> copyCommand("adb devices -l"));
        card("READ-ONLY DIAGNOSTICS", "Select a saved device to copy safe diagnostic commands: Android version, battery, storage, app package list and recent logs. Commands are never run silently by HugzzBotzX.", LAV);
        addButton("COPY ADB SERVER / STATUS COMMANDS", PINK, v -> copyCommand("adb devices -l && adb version"));
        addButton("OPEN TERMUX (IF INSTALLED)", VIOLET, v -> launchPackage("com.termux"));
        addButton("CREATE SUPPORT INVITATION NOTE", PINK, v -> createSupportInvitation());
        card("LIVE STATUS HONESTY", "The registry stores pairing details and owner-consent notes on this phone. A saved record is not proof that a remote connection is currently active. Confirm the actual device state using the output of adb devices in Termux.", PEACH);
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
        card("ABOUT HUGZZBOTZX V7.1", "Ultimate Comic System Edition\nSender opt-in • media center • local security audit • admin tools • local bot templates • authorized ADB helpers • app manager • usage analytics • bounded Bug Lab.", SKY);
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
        String report = "HugzzBotzX Ultimate System V7.1\n" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())
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

    // --------------------------- V7 MEDIA CENTER ---------------------------
    private void showMediaCenter() {
        clearPage();
        comic("MEDIA HQ — use Android's picker or grant media-library access only when you open this feature.", CYAN);
        card("PRIVACY & PERMISSIONS", "V7 requests images, video and audio access only after you tap the permission button. Android 14 can grant selected photos/videos only. The safer picker works without broad storage access. A granted runtime permission remains until revoked in Settings; this app reads library rows only when you open Media Center. No MANAGE_EXTERNAL_STORAGE permission is requested.", LAV);
        card("CURRENT ACCESS", mediaAccessSummary(), SKY);
        addButton("GRANT IMAGE ACCESS", VIOLET, v -> requestMediaCategoryAccess("images"));
        addButton("GRANT VIDEO ACCESS", PINK, v -> requestMediaCategoryAccess("videos"));
        addButton("GRANT AUDIO ACCESS", VIOLET, v -> requestMediaCategoryAccess("audio"));
        addButton("GRANT ALL MEDIA CATEGORIES", PINK, v -> new AlertDialog.Builder(this)
                .setTitle("Grant media categories?")
                .setMessage("Android will ask separately for the media categories supported by this version. You can choose limited photo/video access on supported Android versions.")
                .setNegativeButton("Cancel", null).setPositiveButton("Continue", (d, w) -> requestMediaLibraryAccess()).show());
        addButton("PICK AN IMAGE", PINK, v -> pickDocument("image/*", REQ_PICK_IMAGE));
        addButton("PICK A VIDEO", VIOLET, v -> pickDocument("video/*", REQ_PICK_VIDEO));
        addButton("PICK AN AUDIO FILE", PINK, v -> pickDocument("audio/*", REQ_PICK_AUDIO));
        addButton("BROWSE ACCESSIBLE MEDIA", VIOLET, v -> showMediaLibrary());
        addButton("REFRESH MEDIA PERMISSION STATUS", PINK, v -> showMediaCenter());
        addButton("SHARE LAST SELECTED MEDIA", PINK, v -> shareLastSelectedMedia());
        addButton("ANALYZE AN APK FILE", VIOLET, v -> pickDocument("application/vnd.android.package-archive", REQ_PICK_APK));
        addButton("CALCULATE FILE SHA-256", PINK, v -> pickDocument("*/*", REQ_PICK_HASH));
        String last = prefs.getString("v7_last_media_name", "No media selected yet.");
        card("LAST SELECTION", last + "\n" + prefs.getString("v7_last_media_type", "Choose a file above."), SKY);
    }

    private String mediaAccessSummary() {
        boolean selectedVisual = Build.VERSION.SDK_INT >= 34 && checkSelfPermission("android.permission.READ_MEDIA_VISUAL_USER_SELECTED") == PackageManager.PERMISSION_GRANTED;
        boolean fullImages = Build.VERSION.SDK_INT >= 33 && checkSelfPermission("android.permission.READ_MEDIA_IMAGES") == PackageManager.PERMISSION_GRANTED;
        boolean fullVideos = Build.VERSION.SDK_INT >= 33 && checkSelfPermission("android.permission.READ_MEDIA_VIDEO") == PackageManager.PERMISSION_GRANTED;
        String legacy = Build.VERSION.SDK_INT < 33 && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ? "granted" : "not granted";
        String images = Build.VERSION.SDK_INT >= 33 ? (fullImages ? "full" : selectedVisual ? "limited selection" : "not granted") : legacy;
        String videos = Build.VERSION.SDK_INT >= 33 ? (fullVideos ? "full" : selectedVisual ? "limited selection" : "not granted") : legacy;
        String audio = Build.VERSION.SDK_INT >= 33 ? (checkSelfPermission("android.permission.READ_MEDIA_AUDIO") == PackageManager.PERMISSION_GRANTED ? "granted" : "not granted") : legacy;
        String selected = selectedVisual ? " • Android selected-media access active" : "";
        return "Images: " + images + "\nVideos: " + videos + "\nAudio: " + audio + selected + "\nThe Android picker can still grant access to one chosen item without library permission.";
    }

    private void requestMediaLibraryAccess() {
        ArrayList<String> pending = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 33) {
            addPermissionIfMissing(pending, "android.permission.READ_MEDIA_IMAGES");
            addPermissionIfMissing(pending, "android.permission.READ_MEDIA_VIDEO");
            addPermissionIfMissing(pending, "android.permission.READ_MEDIA_AUDIO");
            if (Build.VERSION.SDK_INT >= 34) addPermissionIfMissing(pending, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED");
        } else {
            addPermissionIfMissing(pending, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (pending.isEmpty()) { toast("Requested media categories are already accessible."); showMediaLibrary(); }
        else requestPermissions(pending.toArray(new String[0]), REQ_MEDIA_PERMS);
    }

    private void requestMediaCategoryAccess(String category) {
        ArrayList<String> pending = new ArrayList<>();
        if (Build.VERSION.SDK_INT < 33) {
            addPermissionIfMissing(pending, Manifest.permission.READ_EXTERNAL_STORAGE);
        } else if ("images".equals(category)) {
            addPermissionIfMissing(pending, "android.permission.READ_MEDIA_IMAGES");
            if (Build.VERSION.SDK_INT >= 34) addPermissionIfMissing(pending, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED");
        } else if ("videos".equals(category)) {
            addPermissionIfMissing(pending, "android.permission.READ_MEDIA_VIDEO");
            if (Build.VERSION.SDK_INT >= 34) addPermissionIfMissing(pending, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED");
        } else if ("audio".equals(category)) {
            addPermissionIfMissing(pending, "android.permission.READ_MEDIA_AUDIO");
        }
        if (pending.isEmpty()) { toast("This category is already available."); showMediaLibrary(); }
        else requestPermissions(pending.toArray(new String[0]), REQ_MEDIA_PERMS);
    }

    private void addPermissionIfMissing(List<String> list, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) list.add(permission);
    }

    private void pickDocument(String mimeType, int requestCode) {
        try {
            Intent pick;
            // Android 13+ Photo Picker grants URI access to selected images/videos without library permission.
            if (Build.VERSION.SDK_INT >= 33 && (requestCode == REQ_PICK_IMAGE || requestCode == REQ_PICK_VIDEO)) {
                pick = new Intent(MediaStore.ACTION_PICK_IMAGES);
                pick.setType(mimeType);
            } else {
                pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                pick.addCategory(Intent.CATEGORY_OPENABLE);
                pick.setType(mimeType);
                if (requestCode == REQ_PICK_APK) pick.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/vnd.android.package-archive", "application/octet-stream"});
            }
            startActivityForResult(pick, requestCode);
        } catch (Exception e) {
            toast("Android media/file picker is unavailable on this device.");
        }
    }

    private boolean hasImageVideoAccess() {
        if (Build.VERSION.SDK_INT >= 34) {
            return checkSelfPermission("android.permission.READ_MEDIA_IMAGES") == PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission("android.permission.READ_MEDIA_VIDEO") == PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission("android.permission.READ_MEDIA_VISUAL_USER_SELECTED") == PackageManager.PERMISSION_GRANTED;
        }
        if (Build.VERSION.SDK_INT >= 33) {
            return checkSelfPermission("android.permission.READ_MEDIA_IMAGES") == PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission("android.permission.READ_MEDIA_VIDEO") == PackageManager.PERMISSION_GRANTED;
        }
        return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasAudioAccess() {
        if (Build.VERSION.SDK_INT >= 33) return checkSelfPermission("android.permission.READ_MEDIA_AUDIO") == PackageManager.PERMISSION_GRANTED;
        return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void showMediaLibrary() {
        clearPage();
        comic("MEDIA LIBRARY — only items covered by the permission you selected are listed.", CYAN);
        if (!hasImageVideoAccess() && !hasAudioAccess()) {
            card("NO LIBRARY ACCESS", "Tap Grant Media Library Access, or use the Pick buttons to choose individual files without granting library access.", PEACH);
            addButton("REQUEST MEDIA ACCESS", VIOLET, v -> requestMediaLibraryAccess());
            return;
        }
        if (hasImageVideoAccess()) {
            queryMediaRows(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "IMAGES", SKY);
            queryMediaRows(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "VIDEOS", LAV);
        }
        if (hasAudioAccess()) queryMediaRows(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "AUDIO", PEACH);
        addButton("PICK A FILE WITHOUT LIBRARY ACCESS", PINK, v -> pickDocument("*/*", REQ_PICK_HASH));
    }

    private void queryMediaRows(Uri collection, String section, int tint) {
        String[] projection = {MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.MIME_TYPE, MediaStore.MediaColumns.DATE_ADDED, MediaStore.MediaColumns.SIZE};
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(collection, projection, null, null, MediaStore.MediaColumns.DATE_ADDED + " DESC");
            if (cursor == null) return;
            int shown = 0;
            while (cursor.moveToNext() && shown < 15) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
                String mime = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE));
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE));
                Uri item = ContentUris.withAppendedId(collection, id);
                String label = (name == null ? "Unnamed media" : name) + "\n" + (mime == null ? section : mime) + " • " + readableBytes(size);
                addButton(section + " • " + label, tint, v -> openMediaUri(item, mime));
                shown++;
            }
            if (shown == 0) card(section, "No items visible in the granted media scope.", tint);
        } catch (SecurityException e) {
            card(section, "Permission is not available. Re-open Media Center and grant access, or select a file manually.", PEACH);
        } catch (Exception e) {
            card(section, "Could not read this media collection on the current Android build.", PEACH);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private String readableBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0);
        return String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private void rememberSelectedUri(Uri uri, String kind) {
        lastSelectedUri = uri;
        lastSelectedMime = getContentResolver().getType(uri);
        String name = queryDisplayName(uri);
        prefs.edit().putString("v7_last_media_uri", uri.toString())
                .putString("v7_last_media_name", name)
                .putString("v7_last_media_type", kind + " • " + (lastSelectedMime == null ? "unknown type" : lastSelectedMime))
                .apply();
        log("Selected " + kind + " from Android document picker");
        new AlertDialog.Builder(this).setTitle("Media selected")
                .setMessage(name + "\n" + kind + "\n\nAccess is limited to the URI chosen by you.")
                .setPositiveButton("OPEN", (d, w) -> openMediaUri(uri, lastSelectedMime))
                .setNeutralButton("SHARE", (d, w) -> shareMediaUri(uri, lastSelectedMime))
                .setNegativeButton("CLOSE", null).show();
    }

    private String queryDisplayName(Uri uri) {
        Cursor c = null;
        try {
            c = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (c != null && c.moveToFirst()) return c.getString(0);
        } catch (Exception ignored) { }
        finally { if (c != null) c.close(); }
        return uri.getLastPathSegment() == null ? "Selected file" : uri.getLastPathSegment();
    }

    private void openMediaUri(Uri uri, String mime) {
        try {
            Intent view = new Intent(Intent.ACTION_VIEW);
            view.setDataAndType(uri, mime == null ? "*/*" : mime);
            view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(view);
        } catch (Exception e) { toast("No compatible app can open this media."); }
    }

    private void shareMediaUri(Uri uri, String mime) {
        try {
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType(mime == null ? "*/*" : mime);
            send.putExtra(Intent.EXTRA_STREAM, uri);
            send.setClipData(ClipData.newUri(getContentResolver(), "HugzzBotzX media", uri));
            send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(send, "Share selected media"));
        } catch (Exception e) { toast("Unable to share the selected media."); }
    }

    private void shareLastSelectedMedia() {
        String value = prefs.getString("v7_last_media_uri", "");
        if (value.isEmpty()) { toast("Select media first."); return; }
        shareMediaUri(Uri.parse(value), lastSelectedMime);
    }

    // --------------------------- V7 GLOBAL SENDER ---------------------------
    private JSONArray storedArray(String key) {
        try { return new JSONArray(prefs.getString(key, "[]")); }
        catch (Exception e) { return new JSONArray(); }
    }

    private void saveArray(String key, JSONArray array) {
        prefs.edit().putString(key, array.toString()).apply();
    }

    private void showSender() {
        clearPage();
        comic("GLOBAL SENDER — consent-first message drafts, one recipient at a time.", PINK);
        JSONArray contacts = storedArray("v7_sender_contacts");
        int active = 0;
        for (int i = 0; i < contacts.length(); i++) if (contacts.optJSONObject(i) != null && contacts.optJSONObject(i).optBoolean("optIn", false)) active++;
        card("OPT-IN CONTACTS", active + " recipient(s) with recorded consent. No address-book scraping and no bulk auto-send.", LAV);
        addButton("ADD OPT-IN RECIPIENT", VIOLET, v -> addConsentRecipient());
        addButton("MANAGE / UNSUBSCRIBE CONTACTS", PINK, v -> manageConsentRecipients());
        addButton("COMPOSE MESSAGE TO ONE RECIPIENT", VIOLET, v -> composeConsentedMessage());
        addButton("MESSAGE TEMPLATE MANAGER", PINK, v -> manageMessageTemplates());
        addButton("SCHEDULE LOCAL CAMPAIGN REMINDER", VIOLET, v -> promptReminder());
        card("CHANNELS", "SMS and email open a draft in the user's messaging app. WhatsApp, Telegram and other channels use Android's share sheet. The user reviews and sends manually. Official business APIs need your own approved provider account and backend.", SKY);
        if (active == 0) card("GET STARTED", "Add a recipient and tick the consent confirmation before composing a message.", YELLOW);
    }

    private void addConsentRecipient() {
        LinearLayout form = new LinearLayout(this); form.setOrientation(LinearLayout.VERTICAL); form.setPadding(dp(12), dp(8), dp(12), dp(4));
        EditText name = new EditText(this); name.setHint("Recipient name"); form.addView(name);
        EditText address = new EditText(this); address.setHint("Phone number / email (if needed)"); form.addView(address);
        Spinner channel = new Spinner(this);
        String[] choices = {"SMS", "EMAIL", "WHATSAPP SHARE", "TELEGRAM SHARE", "GENERAL SHARE"};
        channel.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, choices)); form.addView(channel);
        CheckBox consent = new CheckBox(this); consent.setText("I confirm this person opted in to receive messages."); form.addView(consent);
        new AlertDialog.Builder(this).setTitle("Add consented recipient").setView(form)
                .setNegativeButton("Cancel", null).setPositiveButton("Save", (d, w) -> {
                    String n = name.getText().toString().trim(); String a = address.getText().toString().trim(); String ch = String.valueOf(channel.getSelectedItem());
                    if (n.isEmpty() || !consent.isChecked()) { toast("Name and explicit opt-in confirmation are required."); return; }
                    if ((ch.equals("SMS") || ch.equals("EMAIL")) && a.isEmpty()) { toast("This channel needs a phone number or email address."); return; }
                    JSONArray arr = storedArray("v7_sender_contacts");
                    JSONObject item = new JSONObject();
                    try { item.put("name", n); item.put("address", a); item.put("channel", ch); item.put("optIn", true); item.put("consentAt", System.currentTimeMillis()); arr.put(item); saveArray("v7_sender_contacts", arr); log("Opt-in recipient added: " + n); toast("Recipient saved."); showSender(); }
                    catch (Exception e) { toast("Could not save recipient."); }
                }).show();
    }

    private void manageConsentRecipients() {
        JSONArray arr = storedArray("v7_sender_contacts");
        if (arr.length() == 0) { toast("No recipients saved."); return; }
        String[] labels = new String[arr.length()];
        for (int i=0;i<arr.length();i++) { JSONObject o=arr.optJSONObject(i); labels[i]= (o==null?"Unknown":o.optString("name")) + " • " + (o!=null && o.optBoolean("optIn",false)?"OPTED IN":"UNSUBSCRIBED"); }
        new AlertDialog.Builder(this).setTitle("Select a recipient to unsubscribe / remove")
                .setItems(labels, (d, which) -> new AlertDialog.Builder(this).setTitle("Revoke consent?")
                        .setMessage("This disables future message drafts for this recipient in HugzzBotzX.")
                        .setNegativeButton("Cancel", null).setPositiveButton("UNSUBSCRIBE", (dd, ww) -> {
                            JSONArray updated=storedArray("v7_sender_contacts"); JSONObject o=updated.optJSONObject(which);
                            if(o!=null){o.remove("optIn"); try{o.put("optIn",false);}catch(Exception ignored){} saveArray("v7_sender_contacts",updated);log("Recipient unsubscribed");showSender();}
                        }).setNeutralButton("REMOVE", (dd, ww) -> { JSONArray updated=storedArray("v7_sender_contacts"); if(which<updated.length())updated.remove(which); saveArray("v7_sender_contacts",updated);log("Recipient removed from local sender list");showSender(); }).show()).show();
    }

    private void composeConsentedMessage() {
        JSONArray contacts = storedArray("v7_sender_contacts");
        ArrayList<Integer> indices = new ArrayList<>(); ArrayList<String> labels = new ArrayList<>();
        for(int i=0;i<contacts.length();i++){JSONObject o=contacts.optJSONObject(i);if(o!=null&&o.optBoolean("optIn",false)){indices.add(i);labels.add(o.optString("name")+" • "+o.optString("channel"));}}
        if(labels.isEmpty()){toast("Add an opted-in recipient first.");return;}
        LinearLayout form=new LinearLayout(this);form.setOrientation(LinearLayout.VERTICAL);form.setPadding(dp(10),dp(4),dp(10),dp(4));
        Spinner target=new Spinner(this);target.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,labels));form.addView(target);
        EditText subject=new EditText(this);subject.setHint("Subject (email / optional)");form.addView(subject);
        EditText body=new EditText(this);body.setHint("Message text");body.setMinLines(4);body.setGravity(Gravity.TOP);form.addView(body);
        JSONArray templates=storedArray("v7_sender_templates");
        if(templates.length()>0){String[] opts=new String[templates.length()+1];opts[0]="Use a saved template…";for(int i=0;i<templates.length();i++)opts[i+1]=templates.optJSONObject(i)==null?"Template":templates.optJSONObject(i).optString("title");Spinner template=new Spinner(this);template.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,opts));form.addView(template);template.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){} public void onItemSelected(AdapterView<?> p,View v,int pos,long id){if(pos>0){JSONObject t=templates.optJSONObject(pos-1);if(t!=null){subject.setText(t.optString("title"));body.setText(t.optString("body"));}}}});}
        new AlertDialog.Builder(this).setTitle("Compose one consented message").setView(form)
                .setNegativeButton("Cancel",null).setPositiveButton("OPEN DRAFT",(d,w)->{int selected=indices.get(target.getSelectedItemPosition());JSONObject contact=contacts.optJSONObject(selected);if(contact!=null)openConsentedDraft(contact,subject.getText().toString(),body.getText().toString());}).show();
    }

    private void openConsentedDraft(JSONObject contact, String subject, String body) {
        if (!contact.optBoolean("optIn", false)) { toast("Recipient is unsubscribed."); return; }
        if (body == null || body.trim().isEmpty()) { toast("Message cannot be empty."); return; }
        long now=System.currentTimeMillis(); long last=prefs.getLong("v7_last_sender_open",0L);
        if(now-last<8000L){toast("Please wait a few seconds before opening another draft.");return;}
        String channel=contact.optString("channel","GENERAL SHARE"); String address=contact.optString("address","");
        Intent send;
        if(channel.equals("SMS")){send=new Intent(Intent.ACTION_SENDTO,Uri.fromParts("smsto",address,null));send.putExtra("sms_body",body);}
        else if(channel.equals("EMAIL")){send=new Intent(Intent.ACTION_SENDTO,Uri.fromParts("mailto",address,null));send.putExtra(Intent.EXTRA_SUBJECT,subject);send.putExtra(Intent.EXTRA_TEXT,body);}
        else {send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");send.putExtra(Intent.EXTRA_SUBJECT,subject);send.putExtra(Intent.EXTRA_TEXT,body);}
        try{prefs.edit().putLong("v7_last_sender_open",now).apply();log("Opened user-reviewed message draft for " + contact.optString("name"));startActivity(Intent.createChooser(send,"Review and send manually"));}
        catch(Exception e){toast("No compatible messaging app is available.");}
    }

    private void manageMessageTemplates() {
        JSONArray arr=storedArray("v7_sender_templates");
        String[] names=new String[arr.length()+1];names[0]="+ Add new template";
        for(int i=0;i<arr.length();i++){JSONObject o=arr.optJSONObject(i);names[i+1]=o==null?"Template":o.optString("title");}
        new AlertDialog.Builder(this).setTitle("Message templates").setItems(names,(d,which)->{if(which==0)addMessageTemplate();else{JSONObject o=arr.optJSONObject(which-1);if(o!=null)new AlertDialog.Builder(this).setTitle(o.optString("title")).setMessage(o.optString("body")).setPositiveButton("Done",null).setNeutralButton("USE",(dd,ww)->composeConsentedMessage()).show();}})
                .setNegativeButton("Close",null).show();
    }

    private void addMessageTemplate() {
        LinearLayout form=new LinearLayout(this);form.setOrientation(LinearLayout.VERTICAL);EditText title=new EditText(this);title.setHint("Template title");form.addView(title);EditText body=new EditText(this);body.setHint("Template body");body.setMinLines(3);form.addView(body);
        new AlertDialog.Builder(this).setTitle("New message template").setView(form).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{if(title.getText().toString().trim().isEmpty()||body.getText().toString().trim().isEmpty())return;JSONArray a=storedArray("v7_sender_templates");JSONObject o=new JSONObject();try{o.put("title",title.getText().toString().trim());o.put("body",body.getText().toString());a.put(o);saveArray("v7_sender_templates",a);log("Message template saved");toast("Template saved locally.");}catch(Exception ignored){}}).show();
    }

    // --------------------------- V7 DEVICE REGISTRY ---------------------------
    private void showAdbPairingWizard() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(12), dp(6), dp(12), dp(6));
        EditText name = new EditText(this); name.setHint("Device alias (e.g. Family Phone)"); form.addView(name);
        EditText host = new EditText(this); host.setHint("Target IP / hostname (e.g. 192.168.1.20)"); host.setSingleLine(true); form.addView(host);
        EditText pairPort = new EditText(this); pairPort.setHint("PAIRING port shown in Pair device dialog"); pairPort.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); pairPort.setSingleLine(true); form.addView(pairPort);
        EditText connectPort = new EditText(this); connectPort.setHint("CONNECT port shown on Wireless debugging page"); connectPort.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); connectPort.setSingleLine(true); form.addView(connectPort);
        CheckBox consent = new CheckBox(this); consent.setText("I own this device or its owner is present and approves this pairing."); form.addView(consent);
        new AlertDialog.Builder(this).setTitle("Wireless ADB pairing setup")
                .setMessage("Pairing port and connect port are different. Do not enter or save the one-time pairing code in this app.")
                .setView(form).setNegativeButton("Cancel", null)
                .setPositiveButton("SAVE & COPY PAIR COMMAND", (d, w) -> {
                    String alias = name.getText().toString().trim();
                    String h = host.getText().toString().trim();
                    String pp = pairPort.getText().toString().trim();
                    String cp = connectPort.getText().toString().trim();
                    if (alias.isEmpty() || !consent.isChecked() || !isSafeHost(h) || !isValidPort(pp) || (!cp.isEmpty() && !isValidPort(cp))) {
                        toast("Enter a valid alias, host, pairing port and owner approval."); return;
                    }
                    JSONObject item = new JSONObject();
                    try {
                        item.put("name", alias);
                        item.put("host", h);
                        item.put("pairPort", Integer.parseInt(pp));
                        item.put("connectPort", cp.isEmpty() ? 0 : Integer.parseInt(cp));
                        item.put("id", cp.isEmpty() ? h : h + ":" + cp);
                        item.put("transport", "Wireless ADB");
                        item.put("createdAt", System.currentTimeMillis());
                        item.put("ownerConsentConfirmed", true);
                        item.put("pairingStatus", "SETUP_SAVED_NOT_VERIFIED");
                        JSONArray a = storedArray("v7_device_registry");
                        a.put(item);
                        saveArray("v7_device_registry", a);
                        String command = "adb pair " + h + ":" + pp;
                        copyCommand(command);
                        log("Wireless ADB pairing details saved for " + alias + "; pair command copied");
                        new AlertDialog.Builder(this).setTitle("Pair command copied")
                                .setMessage(command + "\n\nRun it in Termux. When ADB prompts, enter the one-time pairing code displayed on the target device. The code is intentionally not stored by HugzzBotzX.\n\nAfter pairing, return to the target Wireless debugging main page, enter its CONNECT port in the device registry and copy the connect command.")
                                .setPositiveButton("OPEN REGISTRY", (dd, ww) -> showDeviceRegistry()).setNegativeButton("OK", null).show();
                    } catch (Exception e) { toast("Could not save device setup."); }
                }).show();
    }

    private boolean isSafeHost(String host) {
        if (host == null || host.length() < 3 || host.length() > 253) return false;
        return host.matches("[A-Za-z0-9][A-Za-z0-9.-]{1,251}[A-Za-z0-9]") && !host.contains("..");
    }

    private boolean isValidPort(String value) {
        try { int p = Integer.parseInt(value); return p >= 1 && p <= 65535; }
        catch (Exception e) { return false; }
    }

    private void registerAuthorizedDevice() {
        showAdbPairingWizard();
    }

    private void showDeviceRegistry() {
        clearPage();
        comic("DEVICE REGISTRY — saved authorized pairing profiles, not a live connection list.", CYAN);
        JSONArray a = storedArray("v7_device_registry");
        if (a.length() == 0) card("EMPTY REGISTRY", "Add a device after its owner opens Wireless debugging and agrees to pair.", LAV);
        for (int i = 0; i < a.length(); i++) {
            final int index = i;
            JSONObject o = a.optJSONObject(i);
            if (o == null) continue;
            String host = o.optString("host", "");
            int pairPort = o.optInt("pairPort", 0);
            int connectPort = o.optInt("connectPort", 0);
            String endpoint = host.isEmpty() ? o.optString("id", "not set") : host + (connectPort > 0 ? ":" + connectPort : " (connect port not set)");
            card(o.optString("name", "Saved device"), "Endpoint: " + endpoint + "\nTransport: " + o.optString("transport", "Manual ADB note") + "\nPairing setup: " + (o.optBoolean("ownerConsentConfirmed", o.optBoolean("authorizedByUser", false)) ? "owner approval recorded" : "not confirmed") + "\nConnection status: NOT LIVE-VERIFIED", SKY);
            if (!host.isEmpty() && pairPort > 0) addButton("COPY PAIR COMMAND • " + o.optString("name"), VIOLET, v -> copyCommand("adb pair " + host + ":" + pairPort));
            if (!host.isEmpty() && connectPort > 0) {
                addButton("COPY CONNECT COMMAND • " + o.optString("name"), PINK, v -> copyCommand("adb connect " + host + ":" + connectPort));
                addButton("COPY SAFE DIAGNOSTICS • " + o.optString("name"), VIOLET, v -> copyDeviceDiagnostics(host + ":" + connectPort));
                addButton("COPY DISCONNECT COMMAND • " + o.optString("name"), PINK, v -> copyCommand("adb disconnect " + host + ":" + connectPort));
            } else if (!host.isEmpty()) {
                addButton("ADD CONNECT PORT • " + o.optString("name"), PINK, v -> updateDeviceConnectPort(index));
            }
            addButton("REMOVE LOCAL PROFILE • " + o.optString("name"), PINK, v -> new AlertDialog.Builder(this)
                    .setTitle("Remove device profile?").setMessage("This removes only the saved HugzzBotzX record. It does not revoke Android ADB trust on the other device.")
                    .setNegativeButton("Cancel", null).setPositiveButton("Remove", (dd, ww) -> {
                        JSONArray now = storedArray("v7_device_registry");
                        if (index < now.length()) now.remove(index);
                        saveArray("v7_device_registry", now); log("Device profile removed locally"); showDeviceRegistry();
                    }).show());
        }
        addButton("PAIR / ADD ANOTHER DEVICE", VIOLET, v -> showAdbPairingWizard());
        addButton("OPEN TARGET DEVELOPER OPTIONS", PINK, v -> openDeveloperOptions());
        card("REVOKING TRUST", "To revoke this host's ADB trust, use Developer options → Wireless debugging → Paired devices on the target and forget this workstation. Removing a local record is not the same as revoking target-side authorization.", PEACH);
    }

    private void updateDeviceConnectPort(int index) {
        EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Connect port shown on Wireless debugging screen");
        new AlertDialog.Builder(this).setTitle("Save ADB connect port").setMessage("Use the separate port from the main Wireless debugging screen, not the temporary pairing port.")
                .setView(input).setNegativeButton("Cancel", null).setPositiveButton("Save", (d, w) -> {
                    String value = input.getText().toString().trim();
                    if (!isValidPort(value)) { toast("Port must be between 1 and 65535."); return; }
                    JSONArray a = storedArray("v7_device_registry");
                    JSONObject o = a.optJSONObject(index);
                    if (o != null) { o.remove("connectPort"); try { o.put("connectPort", Integer.parseInt(value)); o.put("id", o.optString("host") + ":" + value); } catch (Exception ignored) { } saveArray("v7_device_registry", a); log("ADB connect port saved"); showDeviceRegistry(); }
                }).show();
    }

    private void copyDeviceDiagnostics(String serial) {
        String block = "# Read-only diagnostics for the device you authorized\n"
                + "adb -s " + serial + " shell getprop ro.build.version.release\n"
                + "adb -s " + serial + " shell dumpsys battery\n"
                + "adb -s " + serial + " shell df -h /data\n"
                + "adb -s " + serial + " shell pm list packages -3\n"
                + "adb -s " + serial + " logcat -d -t 200";
        copyCommand(block);
        log("Copied read-only diagnostic bundle for " + serial);
    }

    private void showSupportSessions() {
        clearPage();
        comic("SUPPORT SESSION MANAGER — record an agreed support session; this screen does not stream or control another device.", PINK);
        String active = prefs.getString("v71_active_support_session", "");
        if (!active.isEmpty()) {
            try {
                JSONObject o = new JSONObject(active);
                card("SESSION ACTIVE (LOCAL RECORD)", "Device: " + o.optString("device") + "\nPurpose: " + o.optString("purpose") + "\nStarted: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(o.optLong("startedAt"))), MINT);
                addButton("END SUPPORT SESSION", PINK, v -> endSupportSession());
            } catch (Exception e) { prefs.edit().remove("v71_active_support_session").apply(); }
        } else {
            card("NO ACTIVE SESSION", "Start a session record only while the device owner is present and has approved the support. The record is stored locally for audit.", LAV);
            addButton("START APPROVED SUPPORT SESSION", VIOLET, v -> startSupportSession());
        }
        JSONArray history = storedArray("v71_support_sessions");
        card("SESSION HISTORY", history.length() + " completed local session record(s).", SKY);
        for (int i = history.length() - 1; i >= 0 && i >= history.length() - 10; i--) {
            JSONObject o = history.optJSONObject(i);
            if (o != null) card(o.optString("device", "Device"), o.optString("purpose", "Support") + "\nDuration: " + DateUtils.formatElapsedTime(Math.max(0, o.optLong("durationMs")) / 1000L), LAV);
        }
        addButton("VIEW / REVOKE DEVICE PROFILES", PINK, v -> showDeviceRegistry());
    }

    private void startSupportSession() {
        JSONArray devices = storedArray("v7_device_registry");
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        for (int i = 0; i < devices.length(); i++) {
            JSONObject o = devices.optJSONObject(i);
            if (o != null && o.optBoolean("ownerConsentConfirmed", o.optBoolean("authorizedByUser", false))) {
                ids.add(i); labels.add(o.optString("name", "Device") + " • " + o.optString("id", "saved profile"));
            }
        }
        if (labels.isEmpty()) { toast("Add a device profile and record owner approval first."); showDeviceRegistry(); return; }
        new AlertDialog.Builder(this).setTitle("Choose approved device").setItems(labels.toArray(new String[0]), (d, which) -> {
            JSONObject device = devices.optJSONObject(ids.get(which));
            if (device == null) return;
            LinearLayout form = new LinearLayout(this); form.setOrientation(LinearLayout.VERTICAL); form.setPadding(dp(12), dp(4), dp(12), dp(4));
            EditText purpose = new EditText(this); purpose.setHint("Purpose (e.g. help with Wi-Fi setup)"); form.addView(purpose);
            CheckBox approval = new CheckBox(this); approval.setText("The device owner has approved this support session now."); form.addView(approval);
            new AlertDialog.Builder(this).setTitle("Confirm support consent").setView(form).setNegativeButton("Cancel", null).setPositiveButton("START LOCAL SESSION", (dd, ww) -> {
                String why = purpose.getText().toString().trim();
                if (!approval.isChecked() || why.isEmpty()) { toast("Current owner approval and a purpose are required."); return; }
                JSONObject session = new JSONObject();
                try {
                    session.put("device", device.optString("name", "Device"));
                    session.put("deviceId", device.optString("id", ""));
                    session.put("purpose", why);
                    session.put("startedAt", System.currentTimeMillis());
                    session.put("ownerApproved", true);
                    prefs.edit().putString("v71_active_support_session", session.toString()).apply();
                    log("Approved support session record started for " + device.optString("name"));
                    toast("Session record started. No remote connection was opened.");
                    showSupportSessions();
                } catch (Exception e) { toast("Could not save session."); }
            }).show();
        }).show();
    }

    private void endSupportSession() {
        String active = prefs.getString("v71_active_support_session", "");
        if (active.isEmpty()) { showSupportSessions(); return; }
        try {
            JSONObject o = new JSONObject(active);
            long ended = System.currentTimeMillis();
            o.put("endedAt", ended);
            o.put("durationMs", Math.max(0, ended - o.optLong("startedAt")));
            JSONArray history = storedArray("v71_support_sessions");
            history.put(o);
            while (history.length() > 100) history.remove(0);
            saveArray("v71_support_sessions", history);
            prefs.edit().remove("v71_active_support_session").apply();
            log("Support session record ended for " + o.optString("device"));
            toast("Support session record ended.");
        } catch (Exception e) { toast("Could not close session record."); }
        showSupportSessions();
    }

    private void createSupportInvitation() {
        byte[] b=new byte[4];new SecureRandom().nextBytes(b);String code=String.format(Locale.US,"%02X%02X-%02X%02X",b[0]&255,b[1]&255,b[2]&255,b[3]&255);
        String message="HugzzBotzX support invitation code: "+code+"\nThis is only a reference note, not an authentication credential. It does not establish a remote session. The device owner must separately approve ADB or screen sharing.";
        Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,message);startActivity(Intent.createChooser(i,"Share support invitation"));log("Support invitation note created");
    }

    // --------------------------- V7 SECURITY CENTER ---------------------------
    private void showSecurityCenter() {
        clearPage();comic("SECURITY LAB — local permission audit, APK inspection and TLS certificate checks.",YELLOW);
        card("SCOPE", "This is a static permission/risk review, not a certified antivirus or vulnerability database. It does not exploit or modify other apps.",LAV);
        addButton("AUDIT LAUNCHER APP PERMISSIONS",VIOLET,v->auditVisibleAppPermissions());
        addButton("ANALYZE AN APK FILE",PINK,v->pickDocument("application/vnd.android.package-archive",REQ_PICK_APK));
        addButton("VERIFY FILE INTEGRITY (SHA-256)",VIOLET,v->pickDocument("*/*",REQ_PICK_HASH));
        addButton("CHECK HTTPS CERTIFICATE",PINK,v->promptTlsCheck());
        addButton("CHECK NETWORK SECURITY STATUS",VIOLET,v->{String m="Connection: "+networkSummary()+"\nSecurity patch: "+Build.VERSION.SECURITY_PATCH+"\nCleartext traffic for this app: disabled in manifest.";new AlertDialog.Builder(this).setTitle("Network / platform status").setMessage(m).setPositiveButton("OK",null).show();});
        addButton("OPEN ANDROID SECURITY SETTINGS",PINK,v->openSettings(Settings.ACTION_SECURITY_SETTINGS));
    }

    private boolean isSensitivePermission(String permission) {
        String[] flagged={"READ_SMS","RECEIVE_SMS","SEND_SMS","READ_CONTACTS","WRITE_CONTACTS","READ_CALL_LOG","WRITE_CALL_LOG","RECORD_AUDIO","CAMERA","ACCESS_FINE_LOCATION","ACCESS_COARSE_LOCATION","READ_PHONE_STATE","MANAGE_EXTERNAL_STORAGE","SYSTEM_ALERT_WINDOW","REQUEST_INSTALL_PACKAGES","BIND_ACCESSIBILITY_SERVICE"};
        for(String f:flagged)if(permission.endsWith(f))return true;return false;
    }

    private void auditVisibleAppPermissions() {
        clearPage();comic("APP PERMISSION AUDIT — apps visible in the launcher only.",PINK);
        Intent q=new Intent(Intent.ACTION_MAIN,null);q.addCategory(Intent.CATEGORY_LAUNCHER);List<ResolveInfo> rs=getPackageManager().queryIntentActivities(q,0);
        ArrayList<String> packages=new ArrayList<>();for(ResolveInfo r:rs)if(!packages.contains(r.activityInfo.packageName))packages.add(r.activityInfo.packageName);
        int scanned=0,flaggedApps=0;
        for(String pkg:packages){try{android.content.pm.PackageInfo pi=getPackageManager().getPackageInfo(pkg,PackageManager.GET_PERMISSIONS);String[] requested=pi.requestedPermissions;if(requested==null)continue;ArrayList<String> risk=new ArrayList<>();for(String p:requested)if(isSensitivePermission(p))risk.add(p.substring(p.lastIndexOf('.')+1));if(!risk.isEmpty()){flaggedApps++;if(scanned++<30)card(resolveAppLabel(pkg),pkg+"\nSensitive declarations: "+risk,PEACH);}}catch(Exception ignored){}}
        card("AUDIT SUMMARY",packages.size()+" launcher-visible package(s) inspected. "+flaggedApps+" declare one or more sensitive permissions. A declared permission is not proof of malicious behavior; review the app's purpose and Android permission grant state.",YELLOW);
        log("Visible-app permission audit completed: "+packages.size()+" packages");
    }

    private void analyzeApk(Uri uri) {
        card("APK ANALYSIS", "Analyzing selected APK…", SKY);
        new Thread(()->{String result;File tmp=null;try{tmp=copyToCache(uri,".apk",200L*1024L*1024L);String hash=sha256File(tmp);android.content.pm.PackageInfo pi=getPackageManager().getPackageArchiveInfo(tmp.getAbsolutePath(),PackageManager.GET_PERMISSIONS|PackageManager.GET_SIGNATURES);if(pi==null)throw new Exception("Android PackageManager could not parse this file as an APK.");StringBuilder sb=new StringBuilder();sb.append("Package: ").append(pi.packageName).append("\nVersion: ").append(pi.versionName).append("\nSHA-256: ").append(hash).append("\n");String[] perms=pi.requestedPermissions;int flagged=0;if(perms!=null){sb.append("\nDeclared permissions:\n");for(String p:perms){sb.append("• ").append(p).append(isSensitivePermission(p)?"  [REVIEW]":"").append("\n");if(isSensitivePermission(p))flagged++;}}sb.append("\nSensitive permission declarations: ").append(flagged).append("\nThis is static metadata analysis, not a malware verdict.");result=sb.toString();}catch(Exception e){result="APK analysis failed: "+e.getMessage();}finally{if(tmp!=null)tmp.delete();}final String out=result;runOnUiThread(()->new AlertDialog.Builder(this).setTitle("APK analysis report").setMessage(out).setPositiveButton("OK",null).setNeutralButton("SHARE",(d,w)->shareText("HugzzBotzX APK report",out)).show());}).start();
    }

    private void hashSelectedFile(Uri uri) {
        new Thread(()->{String out;try(InputStream in=getContentResolver().openInputStream(uri)){if(in==null)throw new Exception("Cannot open selected file");java.security.MessageDigest md=java.security.MessageDigest.getInstance("SHA-256");byte[] buf=new byte[8192];int n;while((n=in.read(buf))>0)md.update(buf,0,n);out="File: "+queryDisplayName(uri)+"\nSHA-256: "+bytesToHex(md.digest());}catch(Exception e){out="Hash failed: "+e.getMessage();}final String result=out;runOnUiThread(()->new AlertDialog.Builder(this).setTitle("File integrity result").setMessage(result).setPositiveButton("OK",null).setNeutralButton("SHARE",(d,w)->shareText("SHA-256 result",result)).show());}).start();
    }

    private File copyToCache(Uri uri,String suffix,long maxBytes)throws Exception {
        File target=File.createTempFile("hugzz_v7_",suffix,getCacheDir());long total=0;try(InputStream in=getContentResolver().openInputStream(uri);FileOutputStream out=new FileOutputStream(target)){if(in==null)throw new Exception("Selected file cannot be opened");byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1){total+=n;if(total>maxBytes)throw new Exception("File exceeds the 200 MB analysis limit");out.write(buffer,0,n);}}catch(Exception e){target.delete();throw e;}return target;
    }

    private String sha256File(File file)throws Exception {java.security.MessageDigest md=java.security.MessageDigest.getInstance("SHA-256");try(InputStream in=new java.io.FileInputStream(file)){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)md.update(b,0,n);}return bytesToHex(md.digest());}
    private String bytesToHex(byte[] b){StringBuilder s=new StringBuilder();for(byte x:b)s.append(String.format(Locale.US,"%02x",x&255));return s.toString();}

    private void promptTlsCheck() {
        EditText input=new EditText(this);input.setHint("example.com or https://example.com");
        new AlertDialog.Builder(this).setTitle("HTTPS certificate check").setMessage("Connects to the host you enter using Android's default TLS trust store. No port scanning.").setView(input).setNegativeButton("Cancel",null).setPositiveButton("CHECK",(d,w)->checkHttpsCertificate(input.getText().toString().trim())).show();
    }

    private void checkHttpsCertificate(String hostInput) {
        if(hostInput.isEmpty()){toast("Enter a host name.");return;}String target=hostInput.startsWith("https://")?hostInput:"https://"+hostInput;
        new Thread(()->{String out;HttpsURLConnection conn=null;try{URL u=new URL(target);if(!"https".equalsIgnoreCase(u.getProtocol())||u.getHost().isEmpty())throw new Exception("Only a valid HTTPS URL is accepted.");conn=(HttpsURLConnection)u.openConnection();conn.setConnectTimeout(8000);conn.setReadTimeout(8000);conn.setInstanceFollowRedirects(true);conn.connect();java.security.cert.Certificate[] certs=conn.getServerCertificates();if(certs.length==0)throw new Exception("No peer certificate presented.");X509Certificate cert=(X509Certificate)certs[0];cert.checkValidity();out="TLS connection: trusted by Android\nHost: "+u.getHost()+"\nSubject: "+cert.getSubjectX500Principal()+"\nIssuer: "+cert.getIssuerX500Principal()+"\nValid from: "+cert.getNotBefore()+"\nValid until: "+cert.getNotAfter()+"\nHTTP status: "+conn.getResponseCode();}catch(Exception e){out="HTTPS check failed: "+e.getClass().getSimpleName()+" — "+e.getMessage();}finally{if(conn!=null)conn.disconnect();}final String result=out;runOnUiThread(()->new AlertDialog.Builder(this).setTitle("TLS certificate result").setMessage(result).setPositiveButton("OK",null).setNeutralButton("SHARE",(d,w)->shareText("TLS check",result)).show());}).start();
    }

    // --------------------------- V7 ADMIN / INTEGRATIONS ---------------------------
    private void showAdminPanel() {
        clearPage();comic("ADMIN HQ — local operator roles, secure secret storage, configuration and backup.",YELLOW);
        card("MODE", "This build provides a local administration workspace. Server users, Google OAuth, 2FA, API requests and cloud database require a separately deployed backend.",LAV);
        addButton("LOCAL OPERATOR / ROLE MANAGER",VIOLET,v->manageLocalRoles());
        addButton("ENCRYPTED API TOKEN VAULT",PINK,v->saveIntegrationSecret());
        addButton("HTTPS WEBHOOK ENDPOINT",VIOLET,v->configureWebhook());
        addButton("TOGGLE LOCAL MAINTENANCE MODE",PINK,v->toggleMaintenanceMode());
        addButton("DATABASE / BACKUP MANAGER",VIOLET,v->showBackupCenter());
        addButton("SYSTEM HEALTH CHECK",PINK,v->showLiveMonitor());
        card("SECURITY", "API tokens are encrypted with an Android Keystore AES-GCM key on this device. Do not paste production secrets into screenshots or public repositories.",SKY);
    }

    private void manageLocalRoles() {
        JSONArray arr=storedArray("v7_local_roles");StringBuilder list=new StringBuilder("Saved local operators:\n");for(int i=0;i<arr.length();i++){JSONObject o=arr.optJSONObject(i);if(o!=null)list.append("• ").append(o.optString("name")).append(" — ").append(o.optString("role")).append("\n");}
        LinearLayout form=new LinearLayout(this);form.setOrientation(LinearLayout.VERTICAL);EditText name=new EditText(this);name.setHint("Operator label");form.addView(name);Spinner role=new Spinner(this);String[] roles={"ADMIN","MODERATOR","VIEWER"};role.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,roles));form.addView(role);
        new AlertDialog.Builder(this).setTitle("Local roles").setMessage(list.toString()).setView(form).setNegativeButton("Close",null).setPositiveButton("ADD OPERATOR",(d,w)->{String n=name.getText().toString().trim();if(n.isEmpty())return;JSONArray a=storedArray("v7_local_roles");JSONObject o=new JSONObject();try{o.put("name",n);o.put("role",String.valueOf(role.getSelectedItem()));o.put("createdAt",System.currentTimeMillis());a.put(o);saveArray("v7_local_roles",a);log("Local operator role added: "+n);toast("Local role saved. This is not a server account.");}catch(Exception ignored){}}).show();
    }

    private void saveIntegrationSecret() {
        LinearLayout form=new LinearLayout(this);form.setOrientation(LinearLayout.VERTICAL);Spinner service=new Spinner(this);String[] services={"telegram_bot_token","whatsapp_business_token","backend_api_key"};service.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,services));form.addView(service);EditText secret=new EditText(this);secret.setHint("Paste token / API key");secret.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);form.addView(secret);
        new AlertDialog.Builder(this).setTitle("Encrypted token vault").setMessage("Saved only in Android Keystore-backed local encryption. No API request is made by this screen.").setView(form).setNegativeButton("Cancel",null).setPositiveButton("SAVE",(d,w)->{String value=secret.getText().toString();if(value.isEmpty())return;try{SecretVault.put(this,String.valueOf(service.getSelectedItem()),value);log("Integration secret saved to Android Keystore vault");toast("Encrypted secret saved locally.");}catch(Exception e){toast("Could not encrypt this secret on the device.");}}).show();
    }

    private void configureWebhook() {
        EditText input=new EditText(this);input.setHint("https://your-domain.example/webhook");input.setText(prefs.getString("v7_webhook_url",""));
        new AlertDialog.Builder(this).setTitle("Webhook endpoint").setMessage("Only HTTPS endpoints are accepted. This saves configuration locally; it does not send events until a backend connector is implemented.").setView(input).setNegativeButton("Cancel",null).setPositiveButton("SAVE",(d,w)->{String u=input.getText().toString().trim();if(!u.isEmpty()&&!u.startsWith("https://")){toast("Webhook must use HTTPS.");return;}prefs.edit().putString("v7_webhook_url",u).apply();log("Webhook endpoint configuration changed");toast("Webhook URL saved locally.");}).show();
    }

    private void toggleMaintenanceMode() {
        boolean on=!prefs.getBoolean("v7_maintenance",false);prefs.edit().putBoolean("v7_maintenance",on).apply();log("Local maintenance mode "+(on?"enabled":"disabled"));toast("Local maintenance mode "+(on?"ON":"OFF"));
    }

    // --------------------------- V7 BOT MANAGER ---------------------------
    private void showBotManager() {
        clearPage();comic("BOT STUDIO — reusable FAQ templates and channel handoff.",PINK);
        card("INTEGRATION STATUS", "Telegram / WhatsApp API tokens can be stored in the encrypted local vault. A real bot needs an approved provider account, HTTPS webhook backend and server-side message handling.",LAV);
        addButton("ADD FAQ / AUTO-REPLY TEMPLATE",VIOLET,v->addFaqTemplate());
        addButton("VIEW FAQ / REPLY TEMPLATES",PINK,v->viewFaqTemplates());
        addButton("SAVE TELEGRAM / WHATSAPP TOKEN",VIOLET,v->saveIntegrationSecret());
        addButton("CONFIGURE HTTPS WEBHOOK",PINK,v->configureWebhook());
        addButton("SHARE A TEMPLATE THROUGH AN APP",VIOLET,v->shareFaqTemplate());
    }

    private void addFaqTemplate() {
        LinearLayout form=new LinearLayout(this);form.setOrientation(LinearLayout.VERTICAL);EditText trigger=new EditText(this);trigger.setHint("Keyword / trigger");form.addView(trigger);EditText reply=new EditText(this);reply.setHint("FAQ answer / reply");reply.setMinLines(3);form.addView(reply);
        new AlertDialog.Builder(this).setTitle("New local FAQ template").setView(form).setNegativeButton("Cancel",null).setPositiveButton("SAVE",(d,w)->{if(trigger.getText().toString().trim().isEmpty()||reply.getText().toString().trim().isEmpty())return;JSONArray a=storedArray("v7_faq_templates");JSONObject o=new JSONObject();try{o.put("trigger",trigger.getText().toString().trim());o.put("reply",reply.getText().toString());a.put(o);saveArray("v7_faq_templates",a);log("FAQ template saved");toast("FAQ saved locally.");}catch(Exception ignored){}}).show();
    }

    private void viewFaqTemplates() {
        JSONArray a=storedArray("v7_faq_templates");if(a.length()==0){toast("No FAQ templates saved.");return;}String[] labels=new String[a.length()];for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);labels[i]=o==null?"Template":o.optString("trigger");}
        new AlertDialog.Builder(this).setTitle("FAQ templates").setItems(labels,(d,w)->{JSONObject o=a.optJSONObject(w);if(o!=null)new AlertDialog.Builder(this).setTitle(o.optString("trigger")).setMessage(o.optString("reply")).setPositiveButton("Done",null).setNeutralButton("SHARE",(dd,ww)->shareText("FAQ: "+o.optString("trigger"),o.optString("reply"))).show();}).setNegativeButton("Close",null).show();
    }

    private void shareFaqTemplate() {
        JSONArray a=storedArray("v7_faq_templates");if(a.length()==0){toast("Add a FAQ template first.");return;}String[] labels=new String[a.length()];for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);labels[i]=o==null?"Template":o.optString("trigger");}
        new AlertDialog.Builder(this).setTitle("Choose FAQ to share").setItems(labels,(d,w)->{JSONObject o=a.optJSONObject(w);if(o!=null)shareText("FAQ: "+o.optString("trigger"),o.optString("reply"));}).setNegativeButton("Cancel",null).show();
    }

    private void shareText(String subject,String body) {
        Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_SUBJECT,subject);i.putExtra(Intent.EXTRA_TEXT,body);startActivity(Intent.createChooser(i,"Share using"));
    }

    // --------------------------- V7 MONITORING / NOTIFICATIONS ---------------------------
    private void showLiveMonitor() {
        clearPage();comic("LIVE MONITOR — refreshes local metrics while this page is open.",CYAN);
        monitorReadout=text("Ready to monitor this device.",13,INK,false);monitorReadout.setPadding(dp(12),dp(12),dp(12),dp(12));monitorReadout.setBackgroundResource(R.drawable.panel);content.addView(monitorReadout);
        addButton("START LIVE MONITOR",VIOLET,v->startLiveMonitor());addButton("STOP LIVE MONITOR",PINK,v->stopLiveMonitor());
        card("METRICS SCOPE","RAM and app network byte counters are local to this app process. Battery, storage and active network type come from Android system APIs.",LAV);
    }

    private void startLiveMonitor() {
        if(monitorReadout==null)return;if(monitorRunnable!=null)handler.removeCallbacks(monitorRunnable);monitorRunning=true;
        monitorRunnable=new Runnable(){public void run(){if(!monitorRunning||monitorReadout==null)return;monitorReadout.setText(deviceSummary()+"\nApp RX: "+readableBytes(Math.max(0L,TrafficStats.getUidRxBytes(android.os.Process.myUid())))+"\nApp TX: "+readableBytes(Math.max(0L,TrafficStats.getUidTxBytes(android.os.Process.myUid()))));handler.postDelayed(this,2000L);}};monitorRunnable.run();log("Live monitor started");
    }

    private void stopLiveMonitor() {monitorRunning=false;if(monitorRunnable!=null)handler.removeCallbacks(monitorRunnable);toast("Live monitor stopped.");log("Live monitor stopped");}

    private void showNotificationCenter() {
        clearPage();comic("NOTIFICATION CENTER — local reminders and visible device prompts.",YELLOW);
        card("PERMISSION","Android 13+ asks for notification permission when you schedule a reminder. No silent notification sending.",LAV);
        addButton("REMIND ME IN 5 MINUTES",VIOLET,v->scheduleReminderWithPermission(5,"HugzzBotzX: review your device dashboard."));
        addButton("REMIND ME IN 15 MINUTES",PINK,v->scheduleReminderWithPermission(15,"HugzzBotzX: scheduled task reminder."));
        addButton("REMIND ME IN 60 MINUTES",VIOLET,v->scheduleReminderWithPermission(60,"HugzzBotzX: hourly check-in reminder."));
        addButton("OPEN NOTIFICATION SETTINGS",PINK,v->openAppNotificationSettings());
        card("RECENT LOCAL EVENTS",prefs.getString("logs","No activity yet."),SKY);
    }

    private void promptReminder() {scheduleReminderWithPermission(15,"HugzzBotzX: review your pending opt-in campaign.");}

    private void scheduleReminderWithPermission(int minutes,String message) {
        pendingReminderMinutes=minutes;pendingReminderText=message;
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},REQ_POST_NOTIFICATIONS);return;}
        scheduleLocalReminder(minutes,message);
    }

    private void scheduleLocalReminder(int minutes,String message) {
        try {
            Intent i=new Intent(this,ReminderReceiver.class);i.putExtra("message",message);
            PendingIntent pi=PendingIntent.getBroadcast(this,771,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+Math.max(1,minutes)*60_000L,pi);
            log("Local reminder scheduled for "+minutes+" minute(s)");toast("Reminder scheduled in "+minutes+" minutes.");
        }catch(Exception e){toast("Could not schedule reminder.");}
    }

    // --------------------------- V7 BACKUP / RESTORE ---------------------------
    private void showBackupCenter() {
        clearPage();comic("BACKUP CENTER — export or restore HugzzBotzX local settings and opt-in records.",MINT);
        card("WHAT IS INCLUDED","Local preferences such as sender opt-in list, device notes, roles, templates, routines and activity log. Android Keystore secrets are not exported.",LAV);
        addButton("EXPORT JSON BACKUP",VIOLET,v->startBackupExport());
        addButton("RESTORE JSON BACKUP",PINK,v->startBackupImport());
        addButton("VIEW DATA RETENTION INFO",VIOLET,v->new AlertDialog.Builder(this).setTitle("Data retention").setMessage("Data remains in this app's private storage until you clear it or uninstall. Backup files are written only to a location you choose. API secrets in Android Keystore are excluded.").setPositiveButton("OK",null).show());
    }

    private void startBackupExport() {
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");i.putExtra(Intent.EXTRA_TITLE,"hugzzbotzx-v7-backup.json");startActivityForResult(i,REQ_EXPORT_BACKUP);
    }

    private void startBackupImport() {Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");startActivityForResult(i,REQ_IMPORT_BACKUP);}

    private void writeBackup(Uri uri) {
        try {
            JSONObject root=new JSONObject();root.put("application","HugzzBotzX");root.put("schema",1);root.put("createdAt",System.currentTimeMillis());JSONObject data=new JSONObject();
            for(Map.Entry<String,?> e:prefs.getAll().entrySet()){Object v=e.getValue();if(v instanceof String||v instanceof Boolean||v instanceof Integer||v instanceof Long||v instanceof Float||v instanceof Double)data.put(e.getKey(),v);}
            root.put("preferences",data);try(OutputStream out=getContentResolver().openOutputStream(uri,"w")){if(out==null)throw new Exception("Destination cannot be opened");out.write(root.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));}
            log("Local settings backup exported");toast("Backup saved to your chosen location.");
        }catch(Exception e){toast("Backup export failed: "+e.getMessage());}
    }

    private void readBackup(Uri uri) {
        try(InputStream in=getContentResolver().openInputStream(uri);ByteArrayOutputStream bytes=new ByteArrayOutputStream()){
            if(in==null)throw new Exception("File cannot be opened");byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)bytes.write(b,0,n);
            JSONObject root=new JSONObject(new String(bytes.toByteArray(),java.nio.charset.StandardCharsets.UTF_8));if(!"HugzzBotzX".equals(root.optString("application")))throw new Exception("This backup does not belong to HugzzBotzX.");JSONObject data=root.getJSONObject("preferences");SharedPreferences.Editor edit=prefs.edit();edit.clear();java.util.Iterator<String> keys=data.keys();while(keys.hasNext()){String key=keys.next();Object value=data.get(key);if(value instanceof String)edit.putString(key,(String)value);else if(value instanceof Boolean)edit.putBoolean(key,(Boolean)value);else if(value instanceof Integer)edit.putInt(key,(Integer)value);else if(value instanceof Number)edit.putLong(key,((Number)value).longValue());}edit.apply();log("Local settings restored from JSON backup");toast("Backup restored. Reopen the page to refresh.");
        }catch(Exception e){toast("Backup restore failed: "+e.getMessage());}
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode!=RESULT_OK||data==null||data.getData()==null)return;
        Uri uri=data.getData();
        try{getContentResolver().takePersistableUriPermission(uri,data.getFlags()&Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}
        if(requestCode==REQ_PICK_IMAGE)rememberSelectedUri(uri,"IMAGE");
        else if(requestCode==REQ_PICK_VIDEO)rememberSelectedUri(uri,"VIDEO");
        else if(requestCode==REQ_PICK_AUDIO)rememberSelectedUri(uri,"AUDIO");
        else if(requestCode==REQ_PICK_APK)analyzeApk(uri);
        else if(requestCode==REQ_PICK_HASH)hashSelectedFile(uri);
        else if(requestCode==REQ_EXPORT_BACKUP)writeBackup(uri);
        else if(requestCode==REQ_IMPORT_BACKUP)new AlertDialog.Builder(this).setTitle("Restore backup?").setMessage("This replaces local HugzzBotzX preferences with the selected backup. Continue only if you trust this file.").setNegativeButton("Cancel",null).setPositiveButton("RESTORE",(d,w)->readBackup(uri)).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode==REQ_MEDIA_PERMS){boolean any=false;for(int r:grantResults)if(r==PackageManager.PERMISSION_GRANTED)any=true;if(any){toast("Media permission updated. Only granted media categories are visible.");showMediaLibrary();}else{toast("No media library permission granted. You can still pick individual files.");showMediaCenter();}}
        else if(requestCode==REQ_POST_NOTIFICATIONS){if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)scheduleLocalReminder(pendingReminderMinutes,pendingReminderText);else toast("Notification permission denied. Reminder was not scheduled.");}
        else if(requestCode==61||requestCode==62){if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)toast("Camera permission granted. Tap the flashlight action again.");}
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
