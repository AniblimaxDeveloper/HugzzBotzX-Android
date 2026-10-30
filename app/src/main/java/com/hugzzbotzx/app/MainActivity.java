package com.hugzzbotzx.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * HugzzBotzX Premium — transparent, consent-based device support dashboard.
 * All working features in this build are local to this Android device.
 * It intentionally does not include covert monitoring, remote input,
 * screen/audio capture, camera capture, Accessibility abuse, or hidden services.
 */
public class MainActivity extends Activity {
    private static final int BG = Color.rgb(7, 8, 11);
    private static final int PANEL = Color.rgb(19, 21, 27);
    private static final int PANEL_2 = Color.rgb(26, 28, 35);
    private static final int TEXT = Color.rgb(245, 246, 250);
    private static final int MUTED = Color.rgb(151, 155, 169);
    private static final int LINE = Color.rgb(48, 51, 61);
    private static final int SILVER = Color.rgb(226, 229, 238);
    private static final int GREEN = Color.rgb(128, 222, 164);
    private static final int AMBER = Color.rgb(245, 194, 112);
    private static final int RED = Color.rgb(255, 125, 125);

    private static final String PREFS = "hugzzbotzx_premium_local";
    private static final String KEY_LOGS = "audit_logs";
    private static final String KEY_DIAGNOSTICS = "last_diagnostics";
    private static final String KEY_DIAGNOSTICS_AT = "last_diagnostics_at";
    private static final int TAB_HOME = 0;
    private static final int TAB_DEVICE = 1;
    private static final int TAB_SESSIONS = 2;
    private static final int TAB_TOOLS = 3;
    private static final long PAIRING_VALID_MS = 5 * 60 * 1000L;

    private SharedPreferences preferences;
    private LinearLayout pageHost;
    private TextView pairingCodeView;
    private TextView pairingMetaView;
    private TextView sessionTimerView;
    private TextView homeBatteryView;
    private TextView homeStorageView;
    private TextView homeMemoryView;
    private TextView homeNetworkView;
    private TextView sessionStatusView;
    private TextView sessionBannerView;
    private String pairingCode;
    private long pairingExpiresAt;
    private boolean sessionActive;
    private long sessionStartedAt;
    private int selectedTab = TAB_HOME;
    private String lastDiagnostics = "Belum ada diagnostik yang dijalankan.";
    private String lastDiagnosticsAt = "-";
    private boolean resumedOnce = false;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Runnable uiTicker = new Runnable() {
        @Override public void run() {
            updatePairingViews();
            if (sessionTimerView != null && sessionActive) {
                sessionTimerView.setText(formatDuration(SystemClock.elapsedRealtime() - sessionStartedAt));
            }
            uiHandler.postDelayed(this, 1000L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        lastDiagnostics = preferences.getString(KEY_DIAGNOSTICS, lastDiagnostics);
        lastDiagnosticsAt = preferences.getString(KEY_DIAGNOSTICS_AT, lastDiagnosticsAt);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        buildShell();
        renderPage();
        refreshDeviceInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDeviceInfo();
        if (resumedOnce) renderPage();
        resumedOnce = true;
        uiHandler.removeCallbacks(uiTicker);
        uiHandler.post(uiTicker);
    }

    @Override
    protected void onPause() {
        uiHandler.removeCallbacks(uiTicker);
        if (sessionActive) {
            finishSession("Sesi otomatis berakhir saat aplikasi ditinggalkan.", false);
        }
        super.onPause();
    }

    private void buildShell() {
        LinearLayout shell = vertical();
        shell.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(12, 13, 18), BG, Color.rgb(5, 6, 9)}));
        if (Build.VERSION.SDK_INT >= 21) {
            shell.setOnApplyWindowInsetsListener((view, insets) -> {
                view.setPadding(0, insets.getSystemWindowInsetTop(), 0,
                        insets.getSystemWindowInsetBottom());
                return insets;
            });
        }

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);
        pageHost = vertical();
        pageHost.setPadding(dp(19), dp(18), dp(19), dp(24));
        scroll.addView(pageHost, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        shell.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        shell.addView(buildBottomNavigation(), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(shell);
    }

    private View buildBottomNavigation() {
        LinearLayout nav = horizontal();
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(8), dp(9), dp(8), dp(10));
        nav.setBackground(rounded(PANEL, 20, LINE, 1));
        nav.setElevation(dp(12));
        String[] labels = {"HOME", "DEVICE", "SESSIONS", "TOOLS"};
        String[] symbols = {"⌂", "▦", "◷", "⚙"};
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            LinearLayout item = vertical();
            item.setGravity(Gravity.CENTER);
            item.setPadding(dp(4), dp(4), dp(4), dp(4));
            TextView glyph = text(symbols[i], 19, selectedTab == i ? TEXT : MUTED, true);
            glyph.setGravity(Gravity.CENTER);
            TextView label = text(labels[i], 9, selectedTab == i ? SILVER : MUTED, true);
            label.setLetterSpacing(0.08f);
            label.setGravity(Gravity.CENTER);
            item.addView(glyph);
            item.addView(label, margin(0, 2, 0, 0));
            if (selectedTab == i) item.setBackground(rounded(PANEL_2, 13, LINE, 1));
            item.setOnClickListener(v -> {
                selectedTab = index;
                renderPage();
            });
            nav.addView(item, new LinearLayout.LayoutParams(0, dp(54), 1f));
        }
        return nav;
    }

    private void renderPage() {
        if (pageHost == null) return;
        pageHost.removeAllViews();
        pairingCodeView = null;
        pairingMetaView = null;
        sessionTimerView = null;
        sessionStatusView = null;
        sessionBannerView = null;
        homeBatteryView = null;
        homeStorageView = null;
        homeMemoryView = null;
        homeNetworkView = null;
        addHeader();
        if (selectedTab == TAB_HOME) renderHome();
        else if (selectedTab == TAB_DEVICE) renderDevicePage();
        else if (selectedTab == TAB_SESSIONS) renderSessionsPage();
        else renderToolsPage();
        rebuildNavigation();
    }

    private void rebuildNavigation() {
        // Rebuild the bottom bar after a tab change without replacing the content scroll.
        View root = (View) pageHost.getParent();
        if (!(root instanceof ScrollView)) return;
        View shell = root.getParent();
        if (!(shell instanceof LinearLayout)) return;
        LinearLayout layout = (LinearLayout) shell;
        if (layout.getChildCount() >= 2) {
            layout.removeViewAt(1);
            layout.addView(buildBottomNavigation(), new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    private void addHeader() {
        LinearLayout row = horizontal();
        row.setGravity(Gravity.CENTER_VERTICAL);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.hugzzbotzx_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        logo.setBackground(rounded(Color.BLACK, 14, LINE, 1));
        logo.setClipToOutline(true);
        row.addView(logo, new LinearLayout.LayoutParams(dp(52), dp(52)));

        LinearLayout brand = vertical();
        brand.setPadding(dp(11), 0, 0, 0);
        brand.addView(text("HUGZZBOTZX", 16, TEXT, true));
        TextView sub = text("PREMIUM DEVICE SUPPORT", 9, MUTED, true);
        sub.setLetterSpacing(0.12f);
        brand.addView(sub, margin(0, 3, 0, 0));
        row.addView(brand, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView version = pill("V2.0  •  LOCAL", GREEN, Color.rgb(19, 38, 29));
        row.addView(version);
        pageHost.addView(row, margin(0, 0, 0, 23));
    }

    private void renderHome() {
        LinearLayout hero = vertical();
        hero.setPadding(dp(19), dp(20), dp(19), dp(20));
        GradientDrawable heroBg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(49, 51, 60), Color.rgb(25, 27, 35), Color.rgb(16, 17, 23)});
        heroBg.setCornerRadius(dp(22));
        heroBg.setStroke(dp(1), Color.rgb(82, 85, 98));
        hero.setBackground(heroBg);
        TextView overline = text("PRIVATE SUPPORT CONSOLE  /  001", 9, SILVER, true);
        overline.setLetterSpacing(0.14f);
        hero.addView(overline);
        TextView title = text("SUPPORT, WITH\nPERMISSION.", 27, TEXT, true);
        title.setLineSpacing(dp(1), 1f);
        hero.addView(title, margin(0, 11, 0, 9));
        hero.addView(text("A premium toolkit for transparent, owner-approved device assistance.",
                12, MUTED, false));
        LinearLayout heroTags = horizontal();
        heroTags.setGravity(Gravity.CENTER_VERTICAL);
        heroTags.addView(pill("● CONSENT FIRST", GREEN, Color.rgb(19, 38, 29)));
        heroTags.addView(pill("NO HIDDEN ACCESS", SILVER, Color.rgb(37, 38, 46)), margin(7, 0, 0, 0));
        hero.addView(heroTags, margin(0, 16, 0, 0));
        pageHost.addView(hero, margin(0, 0, 0, 19));

        addSection("LIVE STATUS", "The current state of this app");
        LinearLayout status = card();
        sessionBannerView = text(sessionActive ? "●  LOCAL SESSION ACTIVE" : "●  NO ACTIVE SESSION",
                11, sessionActive ? GREEN : MUTED, true);
        sessionBannerView.setLetterSpacing(0.04f);
        status.addView(sessionBannerView);
        status.addView(text(sessionActive
                        ? "A visible, consent-approved demo session is running on this device."
                        : "Everything is idle. No device data is being shared.",
                11, MUTED, false), margin(0, 6, 0, 0));
        pageHost.addView(status, margin(0, 0, 0, 20));

        addSection("DEVICE SNAPSHOT", "Live values read from this phone");
        LinearLayout metricRow1 = horizontal();
        metricRow1.addView(metricCard("BATTERY", batteryText(), "Power status", "◒"),
                new LinearLayout.LayoutParams(0, -2, 1f));
        metricRow1.addView(metricCard("STORAGE FREE", freeStorageText(), "Internal storage", "▤"),
                new LinearLayout.LayoutParams(0, -2, 1f));
        pageHost.addView(metricRow1, margin(0, 0, 0, 9));
        LinearLayout metricRow2 = horizontal();
        metricRow2.addView(metricCard("RAM AVAILABLE", availableMemoryText(), "System memory", "▥"),
                new LinearLayout.LayoutParams(0, -2, 1f));
        metricRow2.addView(metricCard("CONNECTION", networkText(), "Network state", "⌁"),
                new LinearLayout.LayoutParams(0, -2, 1f));
        pageHost.addView(metricRow2, margin(0, 0, 0, 20));

        addPairingCard();
        addSection("QUICK ACTIONS", "Useful actions on this device");
        LinearLayout quick = card();
        quick.addView(actionRow("↻", "Refresh device snapshot", "Update battery, memory and network values",
                this::refreshAndNotify));
        quick.addView(separator(), margin(0, 8, 0, 8));
        quick.addView(actionRow("▣", "Copy device summary", "Copy a privacy-conscious report",
                this::copyDeviceSummary));
        quick.addView(separator(), margin(0, 8, 0, 8));
        quick.addView(actionRow("✓", "Run diagnostics", "Check local device readiness",
                () -> { runDiagnostics(); selectedTab = TAB_TOOLS; renderPage(); }));
        pageHost.addView(quick, margin(0, 0, 0, 19));

        LinearLayout trust = card();
        trust.addView(text("BUILT AROUND OWNER CONTROL", 11, TEXT, true));
        trust.addView(text("No silent monitoring • No camera or microphone capture • No screen recording • No remote input • No background control service.",
                11, MUTED, false), margin(0, 7, 0, 0));
        pageHost.addView(trust);
        addFooter();
    }

    private void addPairingCard() {
        addSection("PAIRING INVITE", "Local demo code • not a network connection");
        LinearLayout box = card();
        LinearLayout head = horizontal();
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.addView(text("TEMPORARY INVITE CODE", 10, MUTED, true),
                new LinearLayout.LayoutParams(0, -2, 1f));
        head.addView(pill("5 MIN EXPIRY", AMBER, Color.rgb(43, 34, 22)));
        box.addView(head);
        pairingCodeView = text(pairingCode == null ? "— — — — — —" : pairingCode,
                30, TEXT, true);
        pairingCodeView.setLetterSpacing(0.18f);
        pairingCodeView.setGravity(Gravity.CENTER);
        box.addView(pairingCodeView, margin(0, 13, 0, 4));
        pairingMetaView = text(pairingMeta(), 10, MUTED, false);
        pairingMetaView.setGravity(Gravity.CENTER);
        box.addView(pairingMetaView, margin(0, 0, 0, 15));
        LinearLayout buttons = horizontal();
        buttons.addView(actionButton("GENERATE CODE", true, this::createPairingCode),
                new LinearLayout.LayoutParams(0, dp(48), 1f));
        buttons.addView(actionButton("COPY", false, this::copyPairingCode),
                new LinearLayout.LayoutParams(0, dp(48), 1f));
        box.addView(buttons);
        box.addView(actionButton("REVOKE INVITE", false, this::revokePairingCode),
                margin(0, 8, 0, 0));
        pageHost.addView(box, margin(0, 0, 0, 20));
        updatePairingViews();
    }

    private void renderDevicePage() {
        pageHost.addView(text("DEVICE INTELLIGENCE", 24, TEXT, true), margin(0, 2, 0, 6));
        pageHost.addView(text("A transparent overview of this phone. Only standard, non-sensitive system information is shown.",
                12, MUTED, false), margin(0, 0, 0, 18));

        addSection("IDENTITY", "Hardware and operating system");
        LinearLayout identity = card();
        addInfoRow(identity, "Manufacturer", safe(Build.MANUFACTURER));
        addInfoRow(identity, "Brand", safe(Build.BRAND));
        addInfoRow(identity, "Model", safe(Build.MODEL));
        addInfoRow(identity, "Device family", safe(Build.DEVICE));
        addInfoRow(identity, "Android version", Build.VERSION.RELEASE);
        addInfoRow(identity, "API level", String.valueOf(Build.VERSION.SDK_INT));
        addInfoRow(identity, "Build ID", safe(Build.ID));
        addInfoRow(identity, "CPU ABI", joinAbis());
        addInfoRow(identity, "Display", displaySummary());
        addInfoRow(identity, "Language", Locale.getDefault().toLanguageTag());
        addInfoRow(identity, "Time zone", TimeZone.getDefault().getID());
        addInfoRow(identity, "App version", appVersion());
        pageHost.addView(identity, margin(0, 0, 0, 19));

        addSection("SYSTEM RESOURCES", "Usage values are refreshed when this page opens");
        LinearLayout resources = card();
        addInfoRow(resources, "Battery", batteryDetails());
        addInfoRow(resources, "Internal storage", storageDetails());
        addInfoRow(resources, "Memory (RAM)", memoryDetails());
        addInfoRow(resources, "Network", networkDetails());
        addInfoRow(resources, "Device uptime", uptimeText());
        pageHost.addView(resources, margin(0, 0, 0, 19));

        addSection("DEVICE SHORTCUTS", "Open Android's own settings");
        LinearLayout shortcuts = card();
        shortcuts.addView(actionRow("⚙", "Open system settings", "Manage the phone using Android settings",
                () -> openSettings(Settings.ACTION_SETTINGS)));
        shortcuts.addView(separator(), margin(0, 8, 0, 8));
        shortcuts.addView(actionRow("▤", "App information", "View HugzzBotzX permissions and storage",
                this::openAppInfo));
        shortcuts.addView(separator(), margin(0, 8, 0, 8));
        shortcuts.addView(actionRow("⇧", "Share device report", "Send a summary using Android share sheet",
                this::shareDeviceReport));
        pageHost.addView(shortcuts, margin(0, 0, 0, 18));
        addFooter();
    }

    private void renderSessionsPage() {
        pageHost.addView(text("SESSION CONTROL", 24, TEXT, true), margin(0, 2, 0, 6));
        pageHost.addView(text("Every session is visible, local, and started only after the device owner approves it.",
                12, MUTED, false), margin(0, 0, 0, 18));

        LinearLayout stateCard = card();
        stateCard.addView(pill(sessionActive ? "● SESSION ACTIVE" : "● SESSION IDLE",
                sessionActive ? GREEN : MUTED,
                sessionActive ? Color.rgb(19, 38, 29) : Color.rgb(35, 36, 43)));
        sessionStatusView = text(sessionActive ? "Owner-approved local demo" : "No support session running",
                18, TEXT, true);
        stateCard.addView(sessionStatusView, margin(0, 12, 0, 4));
        sessionTimerView = text(sessionActive ? formatDuration(SystemClock.elapsedRealtime() - sessionStartedAt)
                : "00:00:00", 30, sessionActive ? GREEN : SILVER, true);
        sessionTimerView.setLetterSpacing(0.08f);
        stateCard.addView(sessionTimerView, margin(0, 0, 0, 6));
        stateCard.addView(text("The timer runs only while this app remains in the foreground. Leaving the app ends the session automatically.",
                11, MUTED, false));
        pageHost.addView(stateCard, margin(0, 0, 0, 15));

        LinearLayout controls = card();
        controls.addView(text("SESSION ACTIONS", 10, MUTED, true));
        controls.addView(actionButton("START WITH OWNER CONSENT", true, this::showConsentDialog),
                margin(0, 12, 0, 0));
        controls.addView(actionButton("END SESSION NOW", false,
                () -> finishSession("Sesi dihentikan secara manual oleh pemilik perangkat.", true)),
                margin(0, 8, 0, 0));
        pageHost.addView(controls, margin(0, 0, 0, 19));

        addSection("LOCAL AUDIT TRAIL", "Recent events stored only on this phone");
        LinearLayout logs = card();
        String[] entries = preferences.getString(KEY_LOGS, "").split("\n");
        if (entries.length == 0 || (entries.length == 1 && entries[0].isEmpty())) {
            logs.addView(text("No activity recorded yet.", 12, MUTED, false));
        } else {
            int count = Math.min(entries.length, 30);
            for (int i = 0; i < count; i++) {
                TextView entry = text("•  " + entries[i], 11, i == 0 ? TEXT : MUTED, i == 0);
                entry.setLineSpacing(dp(3), 1f);
                logs.addView(entry, margin(0, i == 0 ? 0 : 8, 0, 0));
                if (i < count - 1) logs.addView(separator(), margin(0, 8, 0, 0));
            }
        }
        logs.addView(actionButton("CLEAR LOCAL HISTORY", false, this::confirmClearLogs),
                margin(0, 14, 0, 0));
        pageHost.addView(logs, margin(0, 0, 0, 18));
        addFooter();
    }

    private void renderToolsPage() {
        pageHost.addView(text("TOOLS & SECURITY", 24, TEXT, true), margin(0, 2, 0, 6));
        pageHost.addView(text("Diagnostics, reports, shortcuts, and privacy controls.",
                12, MUTED, false), margin(0, 0, 0, 18));

        addSection("DIAGNOSTIC CENTER", "Checks run on this phone only");
        LinearLayout diagnostic = card();
        diagnostic.addView(text("SYSTEM HEALTH CHECK", 13, TEXT, true));
        diagnostic.addView(text("Inspect battery, free storage, available memory, network state, and app permission footprint.",
                11, MUTED, false), margin(0, 6, 0, 13));
        diagnostic.addView(actionButton("RUN DIAGNOSTICS", true, () -> {
            runDiagnostics();
            renderPage();
        }));
        diagnostic.addView(separator(), margin(0, 13, 0, 11));
        diagnostic.addView(text("LAST RESULT  •  " + lastDiagnosticsAt, 9, MUTED, true));
        TextView result = text(lastDiagnostics, 11, TEXT, false);
        result.setLineSpacing(dp(3), 1f);
        diagnostic.addView(result, margin(0, 7, 0, 0));
        pageHost.addView(diagnostic, margin(0, 0, 0, 19));

        addSection("SYSTEM SHORTCUTS", "Handy links to Android settings");
        LinearLayout settings = card();
        settings.addView(actionRow("⚙", "Android settings", "System-wide settings", 
                () -> openSettings(Settings.ACTION_SETTINGS)));
        settings.addView(separator(), margin(0, 8, 0, 8));
        settings.addView(actionRow("◒", "Battery settings", "Battery usage and power options",
                () -> openSettings(Settings.ACTION_BATTERY_SAVER_SETTINGS)));
        settings.addView(separator(), margin(0, 8, 0, 8));
        settings.addView(actionRow("ⓘ", "HugzzBotzX app info", "Permissions, storage, and app details",
                this::openAppInfo));
        pageHost.addView(settings, margin(0, 0, 0, 19));

        addSection("REPORT CENTER", "Copy or share a local support report");
        LinearLayout reports = card();
        reports.addView(actionButton("COPY DIAGNOSTIC REPORT", true, this::copyDiagnosticReport));
        reports.addView(actionButton("SHARE REPORT", false, this::shareDeviceReport),
                margin(0, 8, 0, 0));
        pageHost.addView(reports, margin(0, 0, 0, 19));

        addSection("HELP & PRIVACY", "How HugzzBotzX handles your device");
        LinearLayout help = card();
        help.addView(actionRow("?", "How pairing works", "Understand demo codes and limitations",
                this::showPairingHelp));
        help.addView(separator(), margin(0, 8, 0, 8));
        help.addView(actionRow("✓", "Consent & safety", "See the boundaries of this build",
                this::showPrivacyHelp));
        help.addView(separator(), margin(0, 8, 0, 8));
        help.addView(actionRow("✦", "About HugzzBotzX", "Version and app information",
                this::showAbout));
        pageHost.addView(help, margin(0, 0, 0, 18));
        addFooter();
    }

    private void addSection(String title, String subtitle) {
        LinearLayout section = vertical();
        TextView label = text(title, 10, SILVER, true);
        label.setLetterSpacing(0.15f);
        section.addView(label);
        section.addView(text(subtitle, 10, MUTED, false), margin(0, 3, 0, 0));
        pageHost.addView(section, margin(0, 0, 0, 9));
    }

    private LinearLayout metricCard(String label, String value, String subtitle, String symbol) {
        LinearLayout box = card();
        LinearLayout top = horizontal();
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(text(label, 9, MUTED, true), new LinearLayout.LayoutParams(0, -2, 1f));
        top.addView(text(symbol, 16, SILVER, true));
        box.addView(top);
        TextView valueView = text(value, 20, TEXT, true);
        valueView.setSingleLine(true);
        valueView.setEllipsize(TextUtils.TruncateAt.END);
        box.addView(valueView, margin(0, 9, 0, 3));
        box.addView(text(subtitle, 10, MUTED, false));
        if ("BATTERY".equals(label)) homeBatteryView = valueView;
        if ("STORAGE FREE".equals(label)) homeStorageView = valueView;
        if ("RAM AVAILABLE".equals(label)) homeMemoryView = valueView;
        if ("CONNECTION".equals(label)) homeNetworkView = valueView;
        return box;
    }

    private LinearLayout card() {
        LinearLayout box = vertical();
        box.setPadding(dp(15), dp(15), dp(15), dp(15));
        box.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(26, 28, 35), PANEL}));
        ((GradientDrawable) box.getBackground()).setCornerRadius(dp(19));
        ((GradientDrawable) box.getBackground()).setStroke(dp(1), LINE);
        return box;
    }

    private LinearLayout actionRow(String symbol, String title, String subtitle, Runnable action) {
        LinearLayout row = horizontal();
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView icon = text(symbol, 18, SILVER, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(rounded(Color.rgb(39, 41, 50), 12, LINE, 1));
        row.addView(icon, new LinearLayout.LayoutParams(dp(40), dp(40)));
        LinearLayout labels = vertical();
        labels.setPadding(dp(11), 0, dp(5), 0);
        labels.addView(text(title, 12, TEXT, true));
        labels.addView(text(subtitle, 10, MUTED, false), margin(0, 3, 0, 0));
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView arrow = text("›", 23, MUTED, false);
        row.addView(arrow);
        row.setClickable(true);
        row.setFocusable(true);
        row.setBackground(ripple(rounded(Color.TRANSPARENT, 12, Color.TRANSPARENT, 0)));
        row.setOnClickListener(v -> action.run());
        return row;
    }

    private TextView actionButton(String label, boolean primary, Runnable action) {
        TextView button = text(label, 10, primary ? Color.rgb(15, 16, 20) : TEXT, true);
        button.setGravity(Gravity.CENTER);
        button.setLetterSpacing(0.05f);
        button.setPadding(dp(12), dp(13), dp(12), dp(13));
        button.setMinHeight(dp(46));
        button.setBackground(ripple(rounded(primary ? SILVER : Color.rgb(37, 39, 47), 13,
                primary ? SILVER : LINE, 1)));
        button.setClickable(true);
        button.setFocusable(true);
        button.setOnClickListener(v -> action.run());
        return button;
    }

    private TextView pill(String label, int foreground, int background) {
        TextView view = text(label, 9, foreground, true);
        view.setLetterSpacing(0.07f);
        view.setPadding(dp(10), dp(7), dp(10), dp(7));
        view.setBackground(rounded(background, 30, Color.TRANSPARENT, 0));
        return view;
    }

    private View separator() {
        View line = new View(this);
        line.setBackgroundColor(LINE);
        line.setMinimumHeight(dp(1));
        line.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
        return line;
    }

    private void addInfoRow(LinearLayout parent, String key, String value) {
        LinearLayout row = horizontal();
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));
        row.addView(text(key, 11, MUTED, false),
                new LinearLayout.LayoutParams(0, -2, 0.9f));
        TextView val = text(value, 11, TEXT, true);
        val.setGravity(Gravity.RIGHT);
        val.setMaxWidth(dp(190));
        val.setEllipsize(TextUtils.TruncateAt.END);
        row.addView(val, new LinearLayout.LayoutParams(0, -2, 1.1f));
        parent.addView(row);
        if (parent.getChildCount() > 1) parent.addView(separator());
    }

    private void addFooter() {
        TextView footer = text("HUGZZBOTZX  •  PRIVATE BY DESIGN\nLOCAL BUILD 2.0  /  OWNER CONTROL FIRST",
                9, MUTED, true);
        footer.setGravity(Gravity.CENTER);
        footer.setLetterSpacing(0.06f);
        footer.setLineSpacing(dp(3), 1f);
        pageHost.addView(footer, margin(0, 8, 0, 5));
    }

    private LinearLayout vertical() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout horizontal() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        return layout;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setIncludeFontPadding(true);
        view.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        return view;
    }

    private GradientDrawable rounded(int color, int radiusDp, int strokeColor, int strokeDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) drawable.setStroke(dp(strokeDp), strokeColor);
        return drawable;
    }

    private android.graphics.drawable.Drawable ripple(GradientDrawable content) {
        if (Build.VERSION.SDK_INT >= 21) {
            return new android.graphics.drawable.RippleDrawable(
                    android.content.res.ColorStateList.valueOf(Color.argb(50, 230, 232, 240)),
                    content, null);
        }
        return content;
    }

    private LinearLayout.LayoutParams margin(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "Unknown" : value;
    }

    private String joinAbis() {
        if (Build.SUPPORTED_ABIS == null || Build.SUPPORTED_ABIS.length == 0) return "Unknown";
        StringBuilder out = new StringBuilder();
        for (String abi : Build.SUPPORTED_ABIS) {
            if (out.length() > 0) out.append(", ");
            out.append(abi);
        }
        return out.toString();
    }

    private String appVersion() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            return info.versionName == null ? "2.0.0" : info.versionName;
        } catch (Exception ignored) {
            return "2.0.0";
        }
    }

    private String displaySummary() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return metrics.widthPixels + " × " + metrics.heightPixels + " px / "
                + String.format(Locale.getDefault(), "%.1f", metrics.density) + "x";
    }

    private String batteryText() {
        Intent battery = batteryIntent();
        if (battery == null) return "N/A";
        int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level < 0 || scale <= 0) return "N/A";
        return Math.round(level * 100f / scale) + "%";
    }

    private String batteryDetails() {
        Intent battery = batteryIntent();
        if (battery == null) return "Not available";
        StringBuilder result = new StringBuilder(batteryText());
        int status = battery.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        String state = "Unknown";
        if (status == BatteryManager.BATTERY_STATUS_CHARGING) state = "Charging";
        else if (status == BatteryManager.BATTERY_STATUS_FULL) state = "Full";
        else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) state = "Discharging";
        else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) state = "Not charging";
        result.append(" • ").append(state);
        int temp = battery.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        if (temp > 0) result.append(" • ").append(String.format(Locale.getDefault(), "%.1f°C", temp / 10f));
        return result.toString();
    }

    private Intent batteryIntent() {
        try {
            return registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        } catch (Exception ignored) {
            return null;
        }
    }

    private String freeStorageText() {
        try {
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            return formatBytes(stat.getAvailableBytes());
        } catch (Exception ignored) {
            return "N/A";
        }
    }

    private String storageDetails() {
        try {
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            return formatBytes(stat.getAvailableBytes()) + " free / " + formatBytes(stat.getTotalBytes());
        } catch (Exception ignored) {
            return "Not available";
        }
    }

    private String availableMemoryText() {
        ActivityManager.MemoryInfo info = memoryInfo();
        return info == null ? "N/A" : formatBytes(info.availMem);
    }

    private String memoryDetails() {
        ActivityManager.MemoryInfo info = memoryInfo();
        if (info == null) return "Not available";
        return formatBytes(info.availMem) + " available / " + formatBytes(info.totalMem);
    }

    private ActivityManager.MemoryInfo memoryInfo() {
        try {
            ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            if (manager == null) return null;
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            manager.getMemoryInfo(info);
            return info;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String networkText() {
        String details = networkDetails();
        if (details.startsWith("Connected")) return details.replace("Connected • ", "");
        return details;
    }

    private String networkDetails() {
        try {
            ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (manager == null) return "Not available";
            Network active = manager.getActiveNetwork();
            if (active == null) return "Offline";
            NetworkCapabilities caps = manager.getNetworkCapabilities(active);
            if (caps == null) return "Connected • type unknown";
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return "Connected • Wi-Fi";
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return "Connected • Mobile data";
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) return "Connected • Ethernet";
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) return "Connected • VPN";
            return "Connected • Other network";
        } catch (Exception ignored) {
            return "Network state unavailable";
        }
    }

    private String uptimeText() {
        long millis = SystemClock.elapsedRealtime();
        long seconds = millis / 1000;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        return days + "d " + hours + "h " + minutes + "m";
    }

    private String formatBytes(long bytes) {
        if (bytes < 0) return "N/A";
        double value = bytes;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int index = 0;
        while (value >= 1024 && index < units.length - 1) {
            value /= 1024;
            index++;
        }
        return String.format(Locale.getDefault(), index == 0 ? "%.0f %s" : "%.1f %s", value, units[index]);
    }

    private String pairingMeta() {
        if (pairingCode == null) return "Generate a demo code to display it here.";
        long remaining = pairingExpiresAt - System.currentTimeMillis();
        if (remaining <= 0) return "This demo code has expired. Generate another one.";
        long seconds = (remaining + 999) / 1000;
        return "Local demo only • expires in " + (seconds / 60) + ":"
                + String.format(Locale.US, "%02d", seconds % 60) + " • no remote connection";
    }

    private void updatePairingViews() {
        if (pairingCode != null && System.currentTimeMillis() >= pairingExpiresAt) {
            pairingCode = null;
            pairingExpiresAt = 0;
        }
        if (pairingCodeView != null) pairingCodeView.setText(pairingCode == null ? "— — — — — —" : pairingCode);
        if (pairingMetaView != null) pairingMetaView.setText(pairingMeta());
    }

    private void createPairingCode() {
        int value = new SecureRandom().nextInt(900000) + 100000;
        pairingCode = String.format(Locale.US, "%06d", value);
        pairingExpiresAt = System.currentTimeMillis() + PAIRING_VALID_MS;
        addLog("Temporary local demo invite generated (5 min)");
        updatePairingViews();
        toast("Demo invite created. It is not a network pairing token.");
    }

    private void copyPairingCode() {
        updatePairingViews();
        if (pairingCode == null) {
            toast("Generate a new demo code first.");
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("HugzzBotzX demo code", pairingCode));
            toast("Demo code copied.");
        }
    }

    private void revokePairingCode() {
        pairingCode = null;
        pairingExpiresAt = 0;
        addLog("Temporary local demo invite revoked");
        updatePairingViews();
        toast("Demo invite revoked.");
    }

    private void showConsentDialog() {
        if (sessionActive) {
            toast("A local demo session is already active.");
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Owner consent required")
                .setMessage("You are about to start a visible local demo session on THIS phone. No screen, camera, microphone, messages, files, or keystrokes will be captured or shared. This build has no remote-control connection. The session ends when you tap END or leave this app.")
                .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("I AGREE — START", (dialog, which) -> startSession())
                .show();
    }

    private void startSession() {
        sessionActive = true;
        sessionStartedAt = SystemClock.elapsedRealtime();
        addLog("Owner-approved local demo session started");
        renderPage();
        toast("Local demo session started.");
    }

    private void finishSession(String reason, boolean updateScreen) {
        if (!sessionActive) {
            if (updateScreen) toast("No active session.");
            return;
        }
        long duration = Math.max(0, SystemClock.elapsedRealtime() - sessionStartedAt);
        sessionActive = false;
        sessionStartedAt = 0;
        addLog(reason + " Duration: " + formatDuration(duration));
        if (updateScreen) {
            renderPage();
            toast("Session ended.");
        }
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, secs);
    }

    private void addLog(String message) {
        String stamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String line = stamp + "  |  " + message.replace('\n', ' ');
        String old = preferences.getString(KEY_LOGS, "");
        String[] lines = old.isEmpty() ? new String[0] : old.split("\n");
        StringBuilder updated = new StringBuilder(line);
        for (int i = 0; i < Math.min(lines.length, 29); i++) {
            updated.append('\n').append(lines[i]);
        }
        preferences.edit().putString(KEY_LOGS, updated.toString()).apply();
    }

    private void confirmClearLogs() {
        new AlertDialog.Builder(this)
                .setTitle("Clear local history?")
                .setMessage("This removes the activity log stored by HugzzBotzX on this phone. It does not affect Android system logs.")
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("CLEAR", (dialog, which) -> {
                    preferences.edit().remove(KEY_LOGS).apply();
                    renderPage();
                    toast("Local history cleared.");
                }).show();
    }

    private void runDiagnostics() {
        List<String> checks = new ArrayList<>();
        checks.add("✓ Battery reading: " + batteryDetails());
        try {
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            checks.add("✓ Storage available: " + formatBytes(stat.getAvailableBytes()));
        } catch (Exception e) {
            checks.add("! Storage reading unavailable");
        }
        ActivityManager.MemoryInfo memory = memoryInfo();
        checks.add(memory == null ? "! Memory reading unavailable"
                : "✓ Available RAM: " + formatBytes(memory.availMem));
        checks.add("• Network: " + networkDetails());
        checks.add("✓ Sensitive permissions requested: none");
        checks.add("✓ Background/hidden service: not implemented");
        checks.add("• Session: " + (sessionActive ? "local demo active" : "idle"));
        StringBuilder result = new StringBuilder();
        for (String check : checks) {
            if (result.length() > 0) result.append('\n');
            result.append(check);
        }
        lastDiagnostics = result.toString();
        lastDiagnosticsAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        preferences.edit()
                .putString(KEY_DIAGNOSTICS, lastDiagnostics)
                .putString(KEY_DIAGNOSTICS_AT, lastDiagnosticsAt)
                .apply();
        addLog("Local device diagnostics completed");
    }

    private String buildDeviceReport() {
        return "HUGZZBOTZX — DEVICE SUMMARY\n"
                + "Generated: " + DateFormat.getDateTimeInstance().format(new Date()) + "\n\n"
                + "Manufacturer: " + safe(Build.MANUFACTURER) + "\n"
                + "Brand: " + safe(Build.BRAND) + "\n"
                + "Model: " + safe(Build.MODEL) + "\n"
                + "Android: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")\n"
                + "CPU ABI: " + joinAbis() + "\n"
                + "Display: " + displaySummary() + "\n"
                + "Battery: " + batteryDetails() + "\n"
                + "Storage: " + storageDetails() + "\n"
                + "Memory: " + memoryDetails() + "\n"
                + "Network: " + networkDetails() + "\n"
                + "Uptime: " + uptimeText() + "\n\n"
                + "Privacy: local system summary only. No unique device ID, messages, files, or screen content included.\n"
                + "HugzzBotzX is local-only in this build; no remote connection is active.";
    }

    private String buildDiagnosticReport() {
        return "HUGZZBOTZX — DIAGNOSTIC REPORT\n"
                + "Generated: " + DateFormat.getDateTimeInstance().format(new Date()) + "\n\n"
                + lastDiagnostics + "\n\n"
                + buildDeviceReport();
    }

    private void copyDeviceSummary() {
        copyText("Device summary", buildDeviceReport());
    }

    private void copyDiagnosticReport() {
        copyText("Diagnostic report", buildDiagnosticReport());
    }

    private void copyText(String label, String value) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText(label, value));
            toast(label + " copied.");
        } else {
            toast("Clipboard is unavailable.");
        }
    }

    private void shareDeviceReport() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "HugzzBotzX device support report");
        send.putExtra(Intent.EXTRA_TEXT, buildDiagnosticReport());
        try {
            startActivity(Intent.createChooser(send, "Share support report"));
        } catch (Exception ignored) {
            toast("No compatible sharing app found.");
        }
    }

    private void refreshAndNotify() {
        refreshDeviceInfo();
        toast("Device snapshot refreshed.");
    }

    private void refreshDeviceInfo() {
        if (homeBatteryView != null) homeBatteryView.setText(batteryText());
        if (homeStorageView != null) homeStorageView.setText(freeStorageText());
        if (homeMemoryView != null) homeMemoryView.setText(availableMemoryText());
        if (homeNetworkView != null) homeNetworkView.setText(networkText());
        if (sessionTimerView != null && sessionActive) {
            sessionTimerView.setText(formatDuration(SystemClock.elapsedRealtime() - sessionStartedAt));
        }
        updatePairingViews();
    }

    private void openSettings(String action) {
        try {
            startActivity(new Intent(action));
        } catch (Exception ignored) {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            } catch (Exception ignoredAgain) {
                toast("Android settings could not be opened.");
            }
        }
    }

    private void openAppInfo() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception ignored) {
            toast("App information is unavailable.");
        }
    }

    private void showPairingHelp() {
        new AlertDialog.Builder(this)
                .setTitle("About pairing codes")
                .setMessage("The six-digit invite is generated locally with a secure random number and expires after five minutes. In this build it is a UI demonstration only: there is no backend, internet permission, device discovery, or remote connection. Do not treat this code as a real authentication credential.")
                .setPositiveButton("GOT IT", null).show();
    }

    private void showPrivacyHelp() {
        new AlertDialog.Builder(this)
                .setTitle("Consent & privacy")
                .setMessage("HugzzBotzX Premium 2.0 runs local utility functions only. It reads standard device facts such as model, Android version, battery, storage, available RAM, and network transport. Its only declared runtime-independent permission is ACCESS_NETWORK_STATE. It does not request Accessibility, Contacts, SMS, Location, Camera, Microphone, or screen-capture access. Session history is stored in this app's private preferences. A demo session ends when the app leaves the foreground.")
                .setPositiveButton("UNDERSTOOD", null).show();
    }

    private void showAbout() {
        new AlertDialog.Builder(this)
                .setTitle("HugzzBotzX Premium")
                .setMessage("Version 2.0.0\nPackage: com.hugzzbotzx.app\nBuild: Android Native / Java\nDesign: Obsidian Silver\n\nA transparent device-support dashboard. Current release is local-only and does not connect to or control other devices.")
                .setPositiveButton("CLOSE", null).show();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
