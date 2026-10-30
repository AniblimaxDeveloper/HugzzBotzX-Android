package com.hugzzbotzx.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * HugzzBotzX Premium V2.0
 * Transparent, owner-consented device-support dashboard.
 * This build is intentionally local-only: no remote-control, surveillance,
 * screen capture, camera, microphone, Accessibility Service, or hidden service.
 */
public class MainActivity extends Activity {
    private static final int BG = Color.rgb(8, 9, 13);
    private static final int PANEL = Color.rgb(18, 20, 28);
    private static final int PANEL_ALT = Color.rgb(25, 28, 38);
    private static final int WHITE = Color.rgb(244, 245, 249);
    private static final int MUTED = Color.rgb(151, 157, 171);
    private static final int BORDER = Color.rgb(43, 47, 61);
    private static final int SILVER = Color.rgb(220, 224, 235);
    private static final int PURPLE = Color.rgb(169, 153, 255);
    private static final int GREEN = Color.rgb(116, 220, 163);
    private static final int ORANGE = Color.rgb(255, 197, 120);

    private static final int TAB_HOME = 0;
    private static final int TAB_TOOLS = 1;
    private static final int TAB_ACTIVITY = 2;
    private static final int TAB_SETTINGS = 3;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ArrayList<String> activityLog = new ArrayList<>();
    private final SecureRandom secureRandom = new SecureRandom();

    private LinearLayout pageContainer;
    private LinearLayout navContainer;
    private int currentTab = TAB_HOME;
    private boolean sessionActive = false;
    private long sessionStartedAt = 0L;
    private String pairingCode = "";
    private long pairingExpiresAt = 0L;
    private TextView sessionTimerView;
    private TextView pairingCountdownView;

    private final Runnable uiTicker = new Runnable() {
        @Override public void run() {
            if (sessionTimerView != null && sessionActive) {
                sessionTimerView.setText("Durasi sesi: " + formatDuration(
                        (SystemClock.elapsedRealtime() - sessionStartedAt) / 1000L));
            }
            if (pairingCountdownView != null) {
                if (TextUtils.isEmpty(pairingCode)) {
                    pairingCountdownView.setText("Buat kode demo baru untuk memulai.");
                } else {
                    long remaining = pairingExpiresAt - System.currentTimeMillis();
                    if (remaining <= 0L) {
                        pairingCountdownView.setText("Kode demo kedaluwarsa. Buat kode baru.");
                    } else {
                        pairingCountdownView.setText("Kedaluwarsa dalam " + (remaining / 60000L)
                                + "m " + ((remaining / 1000L) % 60L) + "d • kode demo lokal");
                    }
                }
            }
            handler.postDelayed(this, 1000L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        loadActivityLog();
        buildShell();
        renderPage();
    }

    @Override protected void onResume() {
        super.onResume();
        handler.removeCallbacks(uiTicker);
        handler.post(uiTicker);
    }

    @Override protected void onPause() {
        handler.removeCallbacks(uiTicker);
        super.onPause();
    }

    @Override protected void onDestroy() {
        handler.removeCallbacks(uiTicker);
        super.onDestroy();
    }

    @Override public void onBackPressed() {
        if (currentTab != TAB_HOME) {
            currentTab = TAB_HOME;
            renderPage();
        } else {
            super.onBackPressed();
        }
    }

    private void buildShell() {
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(BG);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        pageContainer = new LinearLayout(this);
        pageContainer.setOrientation(LinearLayout.VERTICAL);
        pageContainer.setPadding(dp(18), dp(16), dp(18), dp(24));
        scroll.addView(pageContainer, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        shell.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        navContainer = new LinearLayout(this);
        navContainer.setOrientation(LinearLayout.VERTICAL);
        shell.addView(navContainer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
            shell.setOnApplyWindowInsetsListener((view, insets) -> {
                Insets bars = insets.getInsets(WindowInsets.Type.systemBars()
                        | WindowInsets.Type.displayCutout());
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return insets;
            });
        }
        setContentView(shell);
        if (Build.VERSION.SDK_INT >= 30) shell.requestApplyInsets();
    }

    private LinearLayout buildBottomNavigation() {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(dp(8), dp(9), dp(8), dp(10));
        nav.setBackground(rounded(Color.rgb(13, 14, 19), 18, BORDER, 1));
        nav.setElevation(dp(10));
        nav.addView(navItem("⌂", "Home", TAB_HOME), navWeight());
        nav.addView(navItem("▦", "Tools", TAB_TOOLS), navWeight());
        nav.addView(navItem("◷", "Activity", TAB_ACTIVITY), navWeight());
        nav.addView(navItem("⚙", "Settings", TAB_SETTINGS), navWeight());
        return nav;
    }

    private LinearLayout.LayoutParams navWeight() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(58), 1f);
        p.setMargins(dp(3), 0, dp(3), 0);
        return p;
    }

    private View navItem(String icon, String label, int tab) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        boolean selected = currentTab == tab;
        item.setBackground(rounded(selected ? Color.rgb(37, 38, 51) : Color.TRANSPARENT,
                14, selected ? Color.rgb(74, 74, 99) : Color.TRANSPARENT, selected ? 1 : 0));
        TextView glyph = text(icon, 20, selected ? SILVER : MUTED, true);
        glyph.setGravity(Gravity.CENTER);
        item.addView(glyph);
        TextView title = text(label, 10, selected ? WHITE : MUTED, selected);
        title.setGravity(Gravity.CENTER);
        item.addView(title);
        item.setClickable(true);
        item.setOnClickListener(v -> {
            currentTab = tab;
            renderPage();
            haptic(v);
        });
        return item;
    }

    private void renderPage() {
        sessionTimerView = null;
        pairingCountdownView = null;
        pageContainer.removeAllViews();
        navContainer.removeAllViews();
        navContainer.addView(buildBottomNavigation(), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addHeader();
        switch (currentTab) {
            case TAB_TOOLS: buildToolsPage(); break;
            case TAB_ACTIVITY: buildActivityPage(); break;
            case TAB_SETTINGS: buildSettingsPage(); break;
            default: buildHomePage(); break;
        }
        pageContainer.invalidate();
    }

    private void addHeader() {
        LinearLayout row = row();
        row.setGravity(Gravity.CENTER_VERTICAL);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.hugzzbotzx_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        logo.setBackground(rounded(PANEL_ALT, 13, BORDER, 1));
        logo.setClipToOutline(true);
        row.addView(logo, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout brand = column();
        brand.setPadding(dp(11), 0, 0, 0);
        brand.addView(text("HUGZZBOTZX", 16, WHITE, true));
        brand.addView(text("PREMIUM DEVICE SUPPORT", 9, MUTED, true));
        row.addView(brand, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView local = pill("LOCAL • PRIVATE", PURPLE);
        row.addView(local);
        pageContainer.addView(row, margin(0, 0, 0, 23));
    }

    private void buildHomePage() {
        addEyebrow("CONTROL CENTER / OVERVIEW");
        LinearLayout hero = card(true);
        LinearLayout top = row();
        top.addView(text("SUPPORT CONSOLE", 10, MUTED, true), new LinearLayout.LayoutParams(0, -2, 1f));
        top.addView(pill(sessionActive ? "SESSION ACTIVE" : "SYSTEM READY",
                sessionActive ? GREEN : PURPLE));
        hero.addView(top);
        hero.addView(text("Support, with\npermission.", 29, WHITE, true), margin(0, 15, 0, 8));
        hero.addView(text("A premium workspace to inspect this device, prepare a support invitation, and manage transparent help sessions.",
                13, MUTED, false), margin(0, 0, 0, 16));
        TextView status = text(sessionActive ? "●  DEMO SESSION IS VISIBLE ON THIS DEVICE"
                : "●  NO SUPPORT SESSION IS RUNNING", 10,
                sessionActive ? GREEN : MUTED, true);
        hero.addView(status, margin(0, 0, 0, 13));
        if (sessionActive) {
            sessionTimerView = text("Durasi sesi: " + formatDuration(
                    (SystemClock.elapsedRealtime() - sessionStartedAt) / 1000L), 12, GREEN, true);
            hero.addView(sessionTimerView, margin(0, 0, 0, 12));
            addButton(hero, "END SUPPORT SESSION", false, v -> endSession());
        } else {
            addButton(hero, "START SESSION WITH CONSENT", true, v -> showConsentDialog());
        }
        pageContainer.addView(hero, margin(0, 0, 0, 19));

        addSection("LIVE DEVICE SNAPSHOT", "This phone only");
        LinearLayout stats = row();
        int currentBattery = batteryPercent();
        stats.addView(metricCard("BATTERY", currentBattery >= 0 ? currentBattery + "%" : "—",
                batteryStatus()), metricWeight());
        stats.addView(metricCard("STORAGE", storageFreeShort(), "Available"), metricWeight());
        stats.addView(metricCard("MEMORY", memoryFreeShort(), "RAM free"), metricWeight());
        pageContainer.addView(stats, margin(0, 0, 0, 19));

        addSection("DEVICE OVERVIEW", "Live system details");
        LinearLayout device = card(false);
        addInfoRow(device, "Device", deviceName());
        addInfoRow(device, "Android", Build.VERSION.RELEASE + " • API " + Build.VERSION.SDK_INT);
        addInfoRow(device, "Network", networkLabel());
        addInfoRow(device, "Display", displayResolution());
        addInfoRow(device, "Uptime", uptimeLabel());
        pageContainer.addView(device, margin(0, 0, 0, 19));

        addSection("QUICK ACTIONS", "Useful shortcuts");
        LinearLayout quick = card(false);
        addButton(quick, "OPEN DEVICE DIAGNOSTICS", true, v -> navigate(TAB_TOOLS));
        addButton(quick, "VIEW ACTIVITY HISTORY", false, v -> navigate(TAB_ACTIVITY));
        addButton(quick, "EXPORT DEVICE REPORT", false, v -> shareReport());
        pageContainer.addView(quick, margin(0, 0, 0, 18));

        LinearLayout note = card(false);
        note.addView(text("BUILT AROUND OWNER CONTROL", 11, SILVER, true));
        note.addView(text("This version is a local device-support dashboard. Pairing codes are demo invitations only; no remote connection or hidden monitoring is performed.",
                12, MUTED, false), margin(0, 7, 0, 0));
        pageContainer.addView(note);
        addFooter();
    }

    private void buildToolsPage() {
        addEyebrow("TOOLS / DEVICE HEALTH");
        addPageTitle("Tools & diagnostics", "Useful utilities for the device running HugzzBotzX.");

        LinearLayout invite = card(true);
        invite.addView(text("DEMO INVITATION • LOCAL ONLY", 10, PURPLE, true));
        invite.addView(text("One-time pairing code", 19, WHITE, true), margin(0, 9, 0, 4));
        TextView code = text(TextUtils.isEmpty(pairingCode) ? "— — — — — —" : spacedCode(pairingCode),
                30, SILVER, true);
        code.setLetterSpacing(0.13f);
        invite.addView(code, margin(0, 9, 0, 4));
        pairingCountdownView = text("Create a demo code to begin.", 11, MUTED, false);
        invite.addView(pairingCountdownView, margin(0, 0, 0, 13));
        LinearLayout inviteButtons = row();
        inviteButtons.addView(button("GENERATE", true, v -> createPairingCode()),
                new LinearLayout.LayoutParams(0, dp(48), 1f));
        inviteButtons.addView(button("COPY", false, v -> copyPairingCode()),
                new LinearLayout.LayoutParams(0, dp(48), 1f));
        LinearLayout.LayoutParams second = (LinearLayout.LayoutParams) inviteButtons.getChildAt(1).getLayoutParams();
        second.setMargins(dp(8), 0, 0, 0);
        inviteButtons.getChildAt(1).setLayoutParams(second);
        invite.addView(inviteButtons);
        addButton(invite, "SHARE INVITATION", false, v -> sharePairingCode());
        invite.addView(text("Important: this is a local demo code. It does not authenticate or connect a second device.",
                10, ORANGE, false), margin(0, 10, 0, 0));
        pageContainer.addView(invite, margin(0, 0, 0, 19));

        addSection("DIAGNOSTIC REPORT", "Read-only information");
        LinearLayout diagnostic = card(false);
        addInfoRow(diagnostic, "Manufacturer", Build.MANUFACTURER);
        addInfoRow(diagnostic, "Model", Build.MODEL);
        addInfoRow(diagnostic, "Android build", Build.DISPLAY);
        addInfoRow(diagnostic, "Security patch", securityPatch());
        addInfoRow(diagnostic, "Battery", batterySummary());
        addInfoRow(diagnostic, "Storage", storageSummary());
        addInfoRow(diagnostic, "Memory", memorySummary());
        addInfoRow(diagnostic, "Network", networkLabel());
        addInfoRow(diagnostic, "Screen", displayResolution());
        addInfoRow(diagnostic, "Device uptime", uptimeLabel());
        addInfoRow(diagnostic, "App version", appVersion());
        addButton(diagnostic, "REFRESH DIAGNOSTICS", true, v -> {
            appendLog("Device diagnostics refreshed");
            renderPage();
            toast("Diagnostics refreshed");
        });
        addButton(diagnostic, "EXPORT REPORT", false, v -> shareReport());
        pageContainer.addView(diagnostic, margin(0, 0, 0, 19));

        addSection("PRIVACY CHECK", "App capability summary");
        LinearLayout privacy = card(false);
        addCheckRow(privacy, "No camera or microphone access");
        addCheckRow(privacy, "No screen recording or screenshots");
        addCheckRow(privacy, "No Accessibility Service or remote input");
        addCheckRow(privacy, "No message, contact, or file collection");
        addCheckRow(privacy, "No hidden background monitoring");
        addCheckRow(privacy, "No Internet permission in this build");
        pageContainer.addView(privacy);
        addFooter();
    }

    private void buildActivityPage() {
        addEyebrow("ACTIVITY / LOCAL HISTORY");
        addPageTitle("Activity history", "A local record of actions performed inside this app.");
        LinearLayout actions = row();
        TextView count = text(activityLog.size() + " EVENTS SAVED", 10, MUTED, true);
        actions.addView(count, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView export = text("EXPORT REPORT  ↗", 10, SILVER, true);
        export.setPadding(dp(10), dp(8), dp(10), dp(8));
        export.setBackground(rounded(PANEL_ALT, 12, BORDER, 1));
        export.setOnClickListener(v -> shareReport());
        actions.addView(export);
        pageContainer.addView(actions, margin(0, 0, 0, 12));

        LinearLayout logCard = card(false);
        if (activityLog.isEmpty()) {
            logCard.addView(text("No activity yet", 16, WHITE, true));
            logCard.addView(text("When you create a demo invitation, refresh diagnostics, or start/end a consent session, the event will appear here.",
                    12, MUTED, false), margin(0, 7, 0, 0));
        } else {
            int shown = Math.min(activityLog.size(), 40);
            for (int i = 0; i < shown; i++) {
                String item = activityLog.get(i);
                LinearLayout eventRow = row();
                TextView dot = text("●", 11, i == 0 ? PURPLE : MUTED, true);
                eventRow.addView(dot, new LinearLayout.LayoutParams(dp(18), -2));
                TextView event = text(item, 12, i == 0 ? WHITE : MUTED, i == 0);
                event.setLineSpacing(dp(2), 1f);
                eventRow.addView(event, new LinearLayout.LayoutParams(0, -2, 1f));
                logCard.addView(eventRow, margin(0, 0, 0, 12));
                if (i < shown - 1) logCard.addView(divider(), margin(dp(18), 0, 0, 12));
            }
        }
        pageContainer.addView(logCard, margin(0, 0, 0, 14));
        addButton(pageContainer, "CLEAR LOCAL HISTORY", false, v -> confirmClearHistory());
        pageContainer.addView(text("History is stored on this phone in the app's private preferences. It is not uploaded.",
                11, MUTED, false), margin(2, 10, 2, 0));
        addFooter();
    }

    private void buildSettingsPage() {
        addEyebrow("SETTINGS / APP CONTROL");
        addPageTitle("Settings", "Manage local app data and review how this build works.");

        LinearLayout appearance = card(false);
        appearance.addView(text("EXPERIENCE", 10, PURPLE, true));
        addInfoRow(appearance, "Appearance", "Midnight • monochrome premium");
        addInfoRow(appearance, "App version", appVersion());
        addInfoRow(appearance, "Package", "com.hugzzbotzx.app");
        addInfoRow(appearance, "Build mode", "Local / consent-first");
        pageContainer.addView(appearance, margin(0, 0, 0, 15));

        LinearLayout privacy = card(false);
        privacy.addView(text("PRIVACY & PERMISSIONS", 10, PURPLE, true));
        privacy.addView(text("HugzzBotzX V2 does not request sensitive permissions. Network state is read only to label the current connection type. No server connection is configured.",
                12, MUTED, false), margin(0, 8, 0, 11));
        addCheckRow(privacy, "Owner approval required to start a demo session");
        addCheckRow(privacy, "Visible session state and stop control");
        addCheckRow(privacy, "Pairing code is local demo data only");
        addCheckRow(privacy, "Activity history stays on this device");
        pageContainer.addView(privacy, margin(0, 0, 0, 15));

        LinearLayout management = card(false);
        management.addView(text("DATA & SUPPORT", 10, PURPLE, true));
        addButton(management, "EXPORT DEVICE REPORT", true, v -> shareReport());
        addButton(management, "CLEAR ACTIVITY HISTORY", false, v -> confirmClearHistory());
        addButton(management, "RESET DEMO INVITATION", false, v -> {
            pairingCode = "";
            pairingExpiresAt = 0L;
            appendLog("Demo invitation reset");
            renderPage();
            toast("Demo invitation reset");
        });
        addButton(management, "ABOUT HUGZZBOTZX", false, v -> showAboutDialog());
        pageContainer.addView(management, margin(0, 0, 0, 15));

        LinearLayout help = card(false);
        help.addView(text("NEED HELP?", 10, PURPLE, true));
        help.addView(text("This release is a local-only dashboard. Real remote assistance would require an authenticated service, clear approval on the device being assisted, a persistent visible indicator, and revocable access.",
                12, MUTED, false), margin(0, 8, 0, 0));
        pageContainer.addView(help);
        addFooter();
    }

    private void addEyebrow(String label) {
        TextView tv = text(label, 10, PURPLE, true);
        tv.setLetterSpacing(0.14f);
        pageContainer.addView(tv, margin(0, 0, 0, 8));
    }

    private void addPageTitle(String title, String subtitle) {
        pageContainer.addView(text(title, 27, WHITE, true), margin(0, 0, 0, 5));
        pageContainer.addView(text(subtitle, 12, MUTED, false), margin(0, 0, 0, 18));
    }

    private void addSection(String title, String subtitle) {
        LinearLayout line = row();
        line.setGravity(Gravity.CENTER_VERTICAL);
        line.addView(text(title, 10, MUTED, true), new LinearLayout.LayoutParams(0, -2, 1f));
        line.addView(text(subtitle, 9, MUTED, false));
        pageContainer.addView(line, margin(1, 0, 1, 9));
    }

    private LinearLayout metricCard(String label, String value, String helper) {
        LinearLayout box = column();
        box.setPadding(dp(11), dp(13), dp(10), dp(13));
        box.setBackground(rounded(PANEL, 16, BORDER, 1));
        box.addView(text(label, 9, MUTED, true));
        box.addView(text(value, 18, WHITE, true), margin(0, 8, 0, 2));
        box.addView(text(helper, 9, MUTED, false));
        return box;
    }

    private LinearLayout.LayoutParams metricWeight() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1f);
        p.setMargins(dp(3), 0, dp(3), 0);
        return p;
    }

    private void addInfoRow(LinearLayout parent, String label, String value) {
        LinearLayout line = row();
        line.setGravity(Gravity.CENTER_VERTICAL);
        TextView left = text(label, 11, MUTED, false);
        line.addView(left, new LinearLayout.LayoutParams(dp(112), -2));
        TextView right = text(TextUtils.isEmpty(value) ? "Not available" : value,
                11, WHITE, true);
        right.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        right.setMaxWidth(dp(190));
        right.setEllipsize(TextUtils.TruncateAt.END);
        right.setMaxLines(2);
        line.addView(right, new LinearLayout.LayoutParams(0, -2, 1f));
        parent.addView(line, margin(0, 0, 0, 12));
    }

    private void addCheckRow(LinearLayout parent, String label) {
        LinearLayout line = row();
        TextView check = text("✓", 14, GREEN, true);
        line.addView(check, new LinearLayout.LayoutParams(dp(22), -2));
        line.addView(text(label, 11, WHITE, false), new LinearLayout.LayoutParams(0, -2, 1f));
        parent.addView(line, margin(0, 0, 0, 9));
    }

    private void addButton(LinearLayout parent, String label, boolean primary,
                           View.OnClickListener listener) {
        Button b = button(label, primary, listener);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(49));
        p.topMargin = dp(8);
        parent.addView(b, p);
    }

    private Button button(String label, boolean primary, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(10);
        b.setAllCaps(false);
        b.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        b.setLetterSpacing(0.06f);
        b.setGravity(Gravity.CENTER);
        b.setTextColor(primary ? Color.rgb(13, 14, 18) : WHITE);
        b.setPadding(dp(10), dp(8), dp(10), dp(8));
        b.setMinHeight(dp(48));
        b.setBackground(rounded(primary ? SILVER : PANEL_ALT, 14,
                primary ? SILVER : BORDER, 1));
        b.setOnClickListener(v -> {
            haptic(v);
            if (listener != null) listener.onClick(v);
        });
        return b;
    }

    private TextView pill(String label, int color) {
        TextView p = text(label, 9, color, true);
        p.setPadding(dp(10), dp(7), dp(10), dp(7));
        p.setBackground(rounded(Color.rgb(29, 29, 41), 30, BORDER, 1));
        return p;
    }

    private LinearLayout card(boolean gradient) {
        LinearLayout box = column();
        box.setPadding(dp(17), dp(17), dp(17), dp(17));
        if (gradient) {
            GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    new int[]{Color.rgb(35, 37, 52), Color.rgb(17, 19, 27)});
            bg.setCornerRadius(dp(22));
            bg.setStroke(dp(1), Color.rgb(66, 68, 88));
            box.setBackground(bg);
        } else {
            box.setBackground(rounded(PANEL, 19, BORDER, 1));
        }
        return box;
    }

    private LinearLayout row() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        return box;
    }

    private LinearLayout column() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        return box;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(color);
        view.setTextSize(sp);
        view.setIncludeFontPadding(true);
        view.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        return view;
    }

    private View divider() {
        View v = new View(this);
        v.setBackgroundColor(BORDER);
        return v;
    }

    private GradientDrawable rounded(int color, int radiusDp, int strokeColor, int strokeDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) d.setStroke(dp(strokeDp), strokeColor);
        return d;
    }

    private LinearLayout.LayoutParams margin(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return p;
    }

    private void addFooter() {
        TextView footer = text("HUGZZBOTZX  •  PRIVATE BY DESIGN\nV2.0 PREMIUM / LOCAL BUILD",
                9, MUTED, true);
        footer.setGravity(Gravity.CENTER);
        footer.setLineSpacing(dp(3), 1f);
        pageContainer.addView(footer, margin(0, 23, 0, 3));
    }

    private void navigate(int tab) {
        currentTab = tab;
        renderPage();
    }

    private void showConsentDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Start a support demo session?")
                .setMessage("Please confirm that you are the owner or have permission to use this device. This starts a visible local demo timer only. No screen, camera, microphone, files, messages, notifications, or keyboard input are shared. You can end the session at any time.")
                .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("I AGREE — START", (dialog, which) -> startSession())
                .show();
    }

    private void startSession() {
        if (sessionActive) return;
        sessionActive = true;
        sessionStartedAt = SystemClock.elapsedRealtime();
        appendLog("Support demo session started with owner consent");
        renderPage();
        toast("Visible demo session started");
    }

    private void endSession() {
        if (!sessionActive) {
            toast("No active session");
            return;
        }
        long seconds = Math.max(0L, (SystemClock.elapsedRealtime() - sessionStartedAt) / 1000L);
        sessionActive = false;
        sessionStartedAt = 0L;
        appendLog("Support demo session ended • duration " + formatDuration(seconds));
        renderPage();
        toast("Session ended");
    }

    private void createPairingCode() {
        pairingCode = String.format(Locale.US, "%06d", secureRandom.nextInt(900000) + 100000);
        pairingExpiresAt = System.currentTimeMillis() + 5L * 60L * 1000L;
        appendLog("Local demo invitation generated");
        renderPage();
        toast("Demo invitation generated");
    }

    private boolean isPairingCodeValid() {
        if (TextUtils.isEmpty(pairingCode)) {
            toast("Generate a demo code first");
            return false;
        }
        if (System.currentTimeMillis() >= pairingExpiresAt) {
            toast("Demo code expired — generate a new one");
            return false;
        }
        return true;
    }

    private void copyPairingCode() {
        if (!isPairingCodeValid()) return;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("HugzzBotzX demo code", pairingCode));
            appendLog("Demo invitation copied to clipboard");
            toast("Demo code copied");
        } else {
            toast("Clipboard unavailable");
        }
    }

    private void sharePairingCode() {
        if (!isPairingCodeValid()) return;
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "HugzzBotzX demo invitation");
        send.putExtra(Intent.EXTRA_TEXT, "HugzzBotzX local demo invitation\nCode: " + pairingCode
                + "\nThis is a demo code only. It does not connect to or control another device.");
        appendLog("Demo invitation shared using Android share sheet");
        startActivity(Intent.createChooser(send, "Share demo invitation"));
    }

    private void confirmClearHistory() {
        new AlertDialog.Builder(this)
                .setTitle("Clear local activity history?")
                .setMessage("This permanently removes the event list stored by HugzzBotzX on this phone.")
                .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("CLEAR HISTORY", (dialog, which) -> {
                    activityLog.clear();
                    getSharedPreferences("hugzzbotzx_state", MODE_PRIVATE).edit()
                            .putString("activity_log", "[]").apply();
                    renderPage();
                    toast("Local history cleared");
                }).show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("HugzzBotzX Premium")
                .setMessage("Version " + appVersion() + "\n\nA transparent, consent-first device support dashboard. This build runs locally and does not contain remote-control or surveillance features.\n\nPackage: com.hugzzbotzx.app")
                .setPositiveButton("CLOSE", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void shareReport() {
        StringBuilder report = new StringBuilder();
        report.append("HUGZZBOTZX — DEVICE DIAGNOSTIC REPORT\n");
        report.append("Generated: ").append(DateFormat.getDateTimeInstance().format(new Date())).append("\n\n");
        report.append("DEVICE\n");
        report.append("Manufacturer: ").append(Build.MANUFACTURER).append("\n");
        report.append("Model: ").append(Build.MODEL).append("\n");
        report.append("Android: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
        report.append("Build: ").append(Build.DISPLAY).append("\n");
        report.append("Security patch: ").append(securityPatch()).append("\n");
        report.append("Screen: ").append(displayResolution()).append("\n\n");
        report.append("STATUS\n");
        report.append("Battery: ").append(batterySummary()).append("\n");
        report.append("Storage: ").append(storageSummary()).append("\n");
        report.append("Memory: ").append(memorySummary()).append("\n");
        report.append("Network: ").append(networkLabel()).append("\n");
        report.append("Uptime: ").append(uptimeLabel()).append("\n");
        report.append("App version: ").append(appVersion()).append("\n\n");
        report.append("PRIVACY\nThis build is local-only. It does not capture screen, camera, microphone, messages, notifications, files, or keystrokes. No remote session is configured.\n\n");
        report.append("RECENT LOCAL EVENTS\n");
        if (activityLog.isEmpty()) report.append("No events recorded.\n");
        else for (int i = 0; i < Math.min(activityLog.size(), 30); i++) report.append("• ").append(activityLog.get(i)).append("\n");

        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "HugzzBotzX device diagnostic report");
        send.putExtra(Intent.EXTRA_TEXT, report.toString());
        appendLog("Device diagnostic report exported via Android share sheet");
        startActivity(Intent.createChooser(send, "Export device report"));
    }

    private void loadActivityLog() {
        String saved = getSharedPreferences("hugzzbotzx_state", MODE_PRIVATE)
                .getString("activity_log", "[]");
        try {
            JSONArray array = new JSONArray(saved);
            for (int i = 0; i < array.length(); i++) activityLog.add(array.optString(i));
        } catch (JSONException ignored) {
            activityLog.clear();
        }
    }

    private void appendLog(String event) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
        activityLog.add(0, time + "  •  " + event);
        while (activityLog.size() > 60) activityLog.remove(activityLog.size() - 1);
        JSONArray array = new JSONArray();
        for (String item : activityLog) array.put(item);
        getSharedPreferences("hugzzbotzx_state", MODE_PRIVATE).edit()
                .putString("activity_log", array.toString()).apply();
    }

    private int batteryPercent() {
        Intent battery = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (battery == null) return -1;
        int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level < 0 || scale <= 0) return -1;
        return Math.round(level * 100f / scale);
    }

    private String batteryStatus() {
        Intent battery = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (battery == null) return "Unavailable";
        int status = battery.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        if (status == BatteryManager.BATTERY_STATUS_CHARGING) return "Charging";
        if (status == BatteryManager.BATTERY_STATUS_FULL) return "Full";
        if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) return "In use";
        return "Standby";
    }

    private String batterySummary() {
        Intent battery = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (battery == null) return "Unavailable";
        int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int percent = (level >= 0 && scale > 0) ? Math.round(level * 100f / scale) : -1;
        int temp = battery.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        String value = percent >= 0 ? percent + "% • " + batteryStatus() : batteryStatus();
        if (temp >= 0) value += String.format(Locale.getDefault(), " • %.1f°C", temp / 10f);
        return value;
    }

    private String storageSummary() {
        try {
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            return formatBytes(stat.getAvailableBytes()) + " free / " + formatBytes(stat.getTotalBytes());
        } catch (Exception e) {
            return "Unavailable";
        }
    }

    private String storageFreeShort() {
        try {
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            return formatBytesShort(stat.getAvailableBytes());
        } catch (Exception e) { return "—"; }
    }

    private String memorySummary() {
        try {
            ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            manager.getMemoryInfo(info);
            return formatBytes(info.availMem) + " free / " + formatBytes(info.totalMem);
        } catch (Exception e) { return "Unavailable"; }
    }

    private String memoryFreeShort() {
        try {
            ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            manager.getMemoryInfo(info);
            return formatBytesShort(info.availMem);
        } catch (Exception e) { return "—"; }
    }

    private String networkLabel() {
        try {
            ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (manager == null) return "Unavailable";
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info == null || !info.isConnected()) return "Offline";
            if (info.getType() == ConnectivityManager.TYPE_WIFI) return "Wi-Fi connected";
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) return "Mobile data connected";
            return "Connected • " + info.getTypeName();
        } catch (SecurityException e) {
            return "Permission unavailable";
        } catch (Exception e) {
            return "Unavailable";
        }
    }

    private String displayResolution() {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        float density = getResources().getDisplayMetrics().density;
        return width + " × " + height + " px • " + String.format(Locale.getDefault(), "%.1fx", density);
    }

    private String uptimeLabel() {
        long seconds = SystemClock.elapsedRealtime() / 1000L;
        long days = seconds / 86400L;
        long hours = (seconds % 86400L) / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        if (days > 0) return days + "d " + hours + "h " + minutes + "m";
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }

    private String securityPatch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) return Build.VERSION.SECURITY_PATCH;
        return "Not available";
    }

    private String deviceName() {
        String manufacturer = Build.MANUFACTURER == null ? "Android" : Build.MANUFACTURER;
        String model = Build.MODEL == null ? "Device" : Build.MODEL;
        return manufacturer + " " + model;
    }

    private String appVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) { return "2.0.0"; }
    }

    private String formatBytes(long bytes) {
        if (bytes < 0L) return "Unavailable";
        if (bytes < 1024L) return bytes + " B";
        double value = bytes;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int index = 0;
        while (value >= 1024.0 && index < units.length - 1) {
            value /= 1024.0;
            index++;
        }
        return String.format(Locale.getDefault(), "%.2f %s", value, units[index]);
    }

    private String formatBytesShort(long bytes) {
        if (bytes < 0L) return "—";
        double value = bytes;
        String[] units = {"B", "K", "M", "G", "T"};
        int index = 0;
        while (value >= 1024.0 && index < units.length - 1) {
            value /= 1024.0;
            index++;
        }
        return String.format(Locale.getDefault(), "%.1f%s", value, units[index]);
    }

    private String formatDuration(long seconds) {
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long secs = seconds % 60L;
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, secs);
    }

    private String spacedCode(String code) {
        if (code == null || code.length() != 6) return code;
        return code.substring(0, 3) + "  " + code.substring(3);
    }

    private void haptic(View v) {
        if (v != null) v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
    }

    private void toast(String value) {
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
