package com.hugzzbotzx.app;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.TimePickerDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.InputType;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.security.MessageDigest;
import java.security.SecureRandom;
import android.util.Base64;

/**
 * HugzzBotzX Family V4.0
 * Transparent, consent-based, on-device family wellbeing utilities.
 * It does not perform hidden monitoring or remote device takeover.
 */
public class MainActivity extends Activity {
    private static final int BG = Color.rgb(246, 249, 255);
    private static final int SURFACE = Color.WHITE;
    private static final int SURFACE_TINT = Color.rgb(235, 241, 255);
    private static final int TEXT = Color.rgb(39, 35, 58);
    private static final int MUTED = Color.rgb(119, 113, 139);
    private static final int BORDER = Color.rgb(222, 230, 244);
    private static final int PURPLE = Color.rgb(82, 105, 211);
    private static final int PURPLE_DARK = Color.rgb(58, 78, 173);
    private static final int PINK = Color.rgb(221, 139, 181);
    private static final int GREEN = Color.rgb(37, 145, 119);
    private static final int ORANGE = Color.rgb(193, 116, 42);
    private static final int RED = Color.rgb(190, 69, 87);

    private static final int TAB_HOME = 0;
    private static final int TAB_PARENT = 1;
    private static final int TAB_ROUTINE = 2;
    private static final int TAB_FLASH = 3;
    private static final int TAB_USAGE = 4;
    private static final int TAB_MORE = 5;
    private static final int REQ_CAMERA = 701;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ArrayList<String> activityLog = new ArrayList<>();
    private LinearLayout pageContainer;
    private LinearLayout navContainer;
    private int currentTab = TAB_HOME;
    private int dailyLimitHours = 3;
    private TextView flashStatusView;
    private TextView usageSummaryView;
    private TextView focusTimerStatus;
    private CountDownTimer focusTimer;
    private long focusRemainingMs;
    private CameraManager cameraManager;
    private String flashCameraId;
    private boolean flashOn;
    private boolean flashPatternRunning;
    private int flashPulses;
    private boolean parentUnlocked;
    private boolean pinDialogShowing;

    private final Runnable flashPattern = new Runnable() {
        @Override public void run() {
            if (!flashPatternRunning) return;
            flashOn = !flashOn;
            setTorch(flashOn);
            flashPulses++;
            if (flashPulses >= 20) {
                stopFlashPattern();
                appendLog("Safe SOS flash pattern completed");
                toast("SOS pattern selesai");
            } else {
                handler.postDelayed(this, 500L);
            }
        }
    };

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        loadState();
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        buildShell();
        renderPage();
    }

    @Override protected void onResume() {
        super.onResume();
        if (currentTab == TAB_USAGE) renderPage();
    }

    @Override protected void onPause() {
        stopFlashPattern();
        super.onPause();
    }

    @Override protected void onDestroy() {
        stopFlashPattern();
        if (focusTimer != null) focusTimer.cancel();
        super.onDestroy();
    }

    @Override public void onBackPressed() {
        if (currentTab != TAB_HOME) navigate(TAB_HOME);
        else super.onBackPressed();
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQ_CAMERA) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                toast("Izin senter diberikan. Tekan kontrol lagi untuk menyalakan.");
                appendLog("Camera permission granted for flashlight");
                renderPage();
            } else {
                toast("Izin kamera ditolak; senter tidak dapat digunakan.");
            }
        }
    }

    private void buildShell() {
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(BG);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);
        pageContainer = new LinearLayout(this);
        pageContainer.setOrientation(LinearLayout.VERTICAL);
        pageContainer.setPadding(dp(18), dp(14), dp(18), dp(28));
        scroll.addView(pageContainer, new ScrollView.LayoutParams(-1, -2));
        shell.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        navContainer = new LinearLayout(this);
        navContainer.setOrientation(LinearLayout.VERTICAL);
        navContainer.setPadding(dp(10), dp(6), dp(10), dp(8));
        navContainer.setBackgroundColor(SURFACE);
        navContainer.setElevation(dp(12));
        shell.addView(navContainer, new LinearLayout.LayoutParams(-1, -2));
        setContentView(shell);
    }

    private LinearLayout buildBottomNavigation() {
        LinearLayout nav = row();
        nav.setGravity(Gravity.CENTER);
        nav.addView(navItem("⌂", "Home", TAB_HOME), navWeight());
        nav.addView(navItem("♡", "Family", TAB_PARENT), navWeight());
        nav.addView(navItem("✓", "Routine", TAB_ROUTINE), navWeight());
        nav.addView(navItem("☼", "Flash", TAB_FLASH), navWeight());
        nav.addView(navItem("◷", "Usage", TAB_USAGE), navWeight());
        nav.addView(navItem("•••", "More", TAB_MORE), navWeight());
        return nav;
    }

    private LinearLayout.LayoutParams navWeight() {
        return new LinearLayout.LayoutParams(0, dp(56), 1f);
    }

    private View navItem(String icon, String label, int tab) {
        LinearLayout item = column();
        item.setGravity(Gravity.CENTER);
        boolean selected = currentTab == tab;
        item.setBackground(rounded(selected ? SURFACE_TINT : Color.TRANSPARENT, 15,
                selected ? BORDER : Color.TRANSPARENT, selected ? 1 : 0));
        TextView glyph = text(icon, 20, selected ? PURPLE : MUTED, true);
        glyph.setGravity(Gravity.CENTER);
        item.addView(glyph);
        TextView title = text(label, 10, selected ? PURPLE_DARK : MUTED, selected);
        title.setGravity(Gravity.CENTER);
        item.addView(title);
        item.setOnClickListener(v -> navigate(tab));
        return item;
    }

    private void navigate(int tab) {
        if (currentTab == TAB_PARENT && tab != TAB_PARENT) parentUnlocked = false;
        if (tab == TAB_PARENT && !parentUnlocked && hasParentPin()) {
            showParentUnlockDialog();
            return;
        }
        currentTab = tab;
        renderPage();
    }

    private void renderPage() {
        pageContainer.removeAllViews();
        navContainer.removeAllViews();
        navContainer.addView(buildBottomNavigation());
        addHeader();
        switch (currentTab) {
            case TAB_PARENT: buildParentalPage(); break;
            case TAB_ROUTINE: buildRoutinePage(); break;
            case TAB_FLASH: buildFlashPage(); break;
            case TAB_USAGE: buildUsagePage(); break;
            case TAB_MORE: buildMorePage(); break;
            default: buildHomePage(); break;
        }
    }

    private void addHeader() {
        LinearLayout header = row();
        header.setGravity(Gravity.CENTER_VERTICAL);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.hugzzbotzx_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        logo.setBackground(rounded(SURFACE, 15, BORDER, 1));
        logo.setClipToOutline(true);
        header.addView(logo, new LinearLayout.LayoutParams(dp(50), dp(50)));
        LinearLayout brand = column();
        brand.setPadding(dp(11), 0, 0, 0);
        brand.addView(text("HUGZZBOTZX", 16, TEXT, true));
        brand.addView(text("FAMILY • SAFETY • WELLBEING", 9, MUTED, true));
        header.addView(brand, new LinearLayout.LayoutParams(0, -2, 1f));
        header.addView(pill("V4 FAMILY", PURPLE));
        pageContainer.addView(header, margin(0, 2, 0, 22));
    }

    private void buildHomePage() {
        addEyebrow("FAMILY CONTROL CENTER");
        LinearLayout hero = card(true);
        hero.addView(pill("PRIVATE • ON-DEVICE", PURPLE));
        hero.addView(text("A calmer,\nsafer digital day.", 29, TEXT, true), margin(0, 14, 0, 7));
        hero.addView(text("Family routines, screen-time insights and practical safety tools — with clear controls and owner-first privacy.",
                13, MUTED, false), margin(0, 0, 0, 15));
        LinearLayout heroButtons = row();
        heroButtons.addView(button("PARENTAL TOOLS", true, v -> navigate(TAB_PARENT)), new LinearLayout.LayoutParams(0, dp(46), 1f));
        LinearLayout.LayoutParams flashButtonParams = new LinearLayout.LayoutParams(0, dp(46), 1f);
        flashButtonParams.setMargins(dp(8), 0, 0, 0);
        heroButtons.addView(button("FLASHLIGHT", false, v -> navigate(TAB_FLASH)), flashButtonParams);
        hero.addView(heroButtons);
        pageContainer.addView(hero, margin(0, 0, 0, 18));

        addSection("THIS DEVICE", "Live information from the phone running HugzzBotzX");
        LinearLayout metrics = row();
        metrics.addView(metric("BATTERY", batteryPercent() >= 0 ? batteryPercent() + "%" : "—", batteryStatus()), metricWeight());
        metrics.addView(metric("STORAGE FREE", storageFreeShort(), "Internal storage"), metricWeight());
        metrics.addView(metric("RAM FREE", memoryFreeShort(), "Available memory"), metricWeight());
        pageContainer.addView(metrics, margin(0, 0, 0, 16));

        addSection("QUICK TOOLS", "Useful actions on this phone");
        LinearLayout actions = card(false);
        addActionRow(actions, "◷", "Screen-time overview", "Review today's app usage", () -> navigate(TAB_USAGE));
        addDivider(actions);
        addActionRow(actions, "♡", "Family wellbeing", "Daily goals, profile and parent settings", () -> navigate(TAB_PARENT));
        addDivider(actions);
        addActionRow(actions, "✓", "Daily family routine", "Checklist, schedule and check-in", () -> navigate(TAB_ROUTINE));
        addDivider(actions);
        addActionRow(actions, "☼", "Flashlight & SOS", "Steady light or slow SOS pattern", () -> navigate(TAB_FLASH));
        addDivider(actions);
        addActionRow(actions, "▤", "Device report", "Share a local diagnostic summary", () -> shareReport());
        pageContainer.addView(actions, margin(0, 0, 0, 16));

        LinearLayout notice = card(false);
        notice.addView(text("A CLEAR NOTE ABOUT REMOTE DEVICES", 11, PURPLE_DARK, true));
        notice.addView(text("This release controls and reads only the phone where it is installed. Remote family pairing needs a secure server and a second app that visibly accepts the connection; those services are not configured in this build.",
                12, MUTED, false), margin(0, 7, 0, 0));
        pageContainer.addView(notice, margin(0, 0, 0, 14));

        LinearLayout familyQuick = card(false);
        familyQuick.addView(text("TODAY'S FAMILY CHECK-IN", 10, PURPLE_DARK, true));
        String checkin = getPreferences(MODE_PRIVATE).getString(todayKey("checkin"), "Not completed");
        familyQuick.addView(text(checkin, 17, TEXT, true), margin(0, 5, 0, 4));
        familyQuick.addView(text("A private, on-device wellbeing check-in.", 11, MUTED, false));
        addButton(familyQuick, "OPEN DAILY ROUTINE", true, v -> navigate(TAB_ROUTINE));
        pageContainer.addView(familyQuick);
        addFooter();
    }

    private void buildParentalPage() {
        addEyebrow("FAMILY / PARENTAL WELLBEING");
        addPageTitle("Family controls", "Transparent wellbeing settings for this phone. Remote supervision is not active in this release.");

        LinearLayout profile = card(false);
        profile.addView(text("LOCAL FAMILY PROFILE", 10, PURPLE_DARK, true));
        String childName = getPreferences(MODE_PRIVATE).getString("child_nickname", "My child");
        String emergencyPhone = getPreferences(MODE_PRIVATE).getString("emergency_phone", "Not set");
        profile.addView(text(childName, 19, TEXT, true), margin(0, 6, 0, 3));
        profile.addView(text("Supervised profile nickname • stored on this device", 10, MUTED, false));
        addButton(profile, "EDIT PROFILE NAME", false, v -> editChildNickname());
        addInfoRow(profile, "Emergency contact", emergencyPhone);
        addButton(profile, "SET EMERGENCY CONTACT", false, v -> editEmergencyContact());
        if (!"Not set".equals(emergencyPhone)) addButton(profile, "OPEN PHONE DIALER", true, v -> dialEmergencyContact());
        pageContainer.addView(profile, margin(0, 0, 0, 16));

        LinearLayout parentSecurity = card(false);
        parentSecurity.addView(text("PARENT DASHBOARD SECURITY", 10, PURPLE_DARK, true));
        parentSecurity.addView(text(hasParentPin() ? "Parent PIN is enabled" : "No parent PIN configured", 15, TEXT, true), margin(0, 6, 0, 3));
        parentSecurity.addView(text("The PIN is hashed locally and is used to protect entry to this Family page. It is not an online account password.", 11, MUTED, false));
        addButton(parentSecurity, hasParentPin() ? "CHANGE PARENT PIN" : "SET PARENT PIN", false, v -> showParentPinSetupDialog());
        pageContainer.addView(parentSecurity, margin(0, 0, 0, 16));

        LinearLayout schedule = card(false);
        schedule.addView(text("FAMILY TIME PLAN", 10, PURPLE_DARK, true));
        schedule.addView(text("Bedtime: " + savedTime("bedtime_minute", 21 * 60), 14, TEXT, true), margin(0, 7, 0, 3));
        schedule.addView(text("Study start: " + savedTime("study_minute", 16 * 60), 14, TEXT, true), margin(0, 0, 0, 3));
        schedule.addView(text("These are planning reminders only; Android app restrictions are not enforced by this ordinary app.", 10, MUTED, false), margin(0, 0, 0, 9));
        addButton(schedule, "SET BEDTIME", false, v -> chooseRoutineTime("bedtime_minute", "Choose bedtime"));
        addButton(schedule, "SET STUDY START", false, v -> chooseRoutineTime("study_minute", "Choose study start"));
        pageContainer.addView(schedule, margin(0, 0, 0, 16));

        LinearLayout daily = card(true);
        daily.addView(text("DAILY SCREEN-TIME GOAL", 10, PURPLE_DARK, true));
        daily.addView(text(dailyLimitHours + " hours", 28, TEXT, true), margin(0, 7, 0, 2));
        daily.addView(text("Choose a soft reminder threshold. This app reports usage; it does not forcibly lock or block other apps.",
                12, MUTED, false), margin(0, 0, 0, 12));
        LinearLayout choices = row();
        for (int h : new int[]{1, 2, 3, 4, 5}) {
            final int hours = h;
            choices.addView(button(h + "h", dailyLimitHours == h, v -> {
                dailyLimitHours = hours;
                getPreferences(MODE_PRIVATE).edit().putInt("daily_limit_hours", hours).apply();
                appendLog("Daily screen-time reminder goal set to " + hours + " hours");
                renderPage();
            }), new LinearLayout.LayoutParams(0, dp(42), 1f));
        }
        daily.addView(choices);
        pageContainer.addView(daily, margin(0, 0, 0, 17));

        addSection("PARENTAL UTILITIES", "Actions with clear limits");
        LinearLayout utilities = card(false);
        addActionRow(utilities, "◷", "Screen-time access", hasUsageAccess() ? "Permission enabled" : "Tap to grant optional usage access", () -> openUsageAccessSettings());
        addDivider(utilities);
        addActionRow(utilities, "⏱", "Start focus session", "25 minutes focus • 5 minutes break", () -> startFocus(25));
        addDivider(utilities);
        addActionRow(utilities, "☾", "Plan a device break", "Open Android Digital Wellbeing settings", () -> openDigitalWellbeing());
        addDivider(utilities);
        addActionRow(utilities, "⌂", "Android parental controls", "Open system parental settings if available", () -> openSystemParentalControls());
        addDivider(utilities);
        addActionRow(utilities, "⚙", "Review app permissions", "Open this app's Android permission page", () -> openAppSettings());
        addDivider(utilities);
        addActionRow(utilities, "✓", "Daily routine checklist", "Homework, reading, rest and more", () -> navigate(TAB_ROUTINE));
        pageContainer.addView(utilities, margin(0, 0, 0, 17));

        addSection("FOCUS & BREAK TIMER", "An in-app wellbeing timer; it does not lock other apps");
        LinearLayout focus = card(false);
        focusTimerStatus = text(focusRemainingMs > 0L ? "Remaining: " + formatClock(focusRemainingMs) : "Ready for a focus session", 16, TEXT, true);
        focus.addView(focusTimerStatus, margin(0, 0, 0, 9));
        LinearLayout focusButtons = row();
        focusButtons.addView(button("25 MIN", true, v -> startFocus(25)), new LinearLayout.LayoutParams(0, dp(44), 1f));
        LinearLayout.LayoutParams f45 = new LinearLayout.LayoutParams(0, dp(44), 1f); f45.setMargins(dp(7), 0, 0, 0);
        focusButtons.addView(button("45 MIN", false, v -> startFocus(45)), f45);
        focus.addView(focusButtons);
        addButton(focus, "STOP TIMER", false, v -> stopFocus());
        pageContainer.addView(focus, margin(0, 0, 0, 17));

        addSection("SECOND DEVICE / FAMILY LINK", "Transparent setup only");
        LinearLayout remote = card(false);
        remote.addView(pill("NOT CONNECTED", ORANGE));
        remote.addView(text("Connect another family device", 17, TEXT, true), margin(0, 9, 0, 4));
        remote.addView(text("This V4 package currently manages the phone where it is installed. Real parent-to-child remote control still needs a backend, parent sign-in, a second app on the child's phone, explicit child-side approval and a visible stop control. No remote command is sent from this build.",
                12, MUTED, false), margin(0, 0, 0, 12));
        addButton(remote, "VIEW SAFE PAIRING REQUIREMENTS", false, v -> showRemoteRequirements());
        pageContainer.addView(remote, margin(0, 0, 0, 17));

        addSection("IMPORTANT ANDROID LIMITS", "What this version can and cannot do");
        LinearLayout limits = card(false);
        addCheckRow(limits, "Reads this phone's app usage only after Usage Access is granted");
        addCheckRow(limits, "Shows a soft daily screen-time reminder threshold");
        addCheckRow(limits, "Offers an on-screen focus/break timer");
        addCheckRow(limits, "Does not remotely lock apps, change settings or control another phone");
        addCheckRow(limits, "Does not capture messages, screen, camera or microphone");
        pageContainer.addView(limits);
        addFooter();
    }

    private void buildRoutinePage() {
        addEyebrow("FAMILY / DAILY ROUTINES");
        addPageTitle("Daily routine", "Build a healthy rhythm together. Checklists and answers stay on this phone.");

        LinearLayout checkin = card(true);
        checkin.addView(text("HOW IS TODAY GOING?", 10, PURPLE_DARK, true));
        String savedCheckin = getPreferences(MODE_PRIVATE).getString(todayKey("checkin"), "Not completed");
        checkin.addView(text(savedCheckin, 18, TEXT, true), margin(0, 6, 0, 3));
        checkin.addView(text("A simple family check-in — not a diagnosis or a public status.", 11, MUTED, false));
        LinearLayout moodButtons = row();
        String[] moods = {"Great", "Okay", "Need support"};
        for (String mood : moods) {
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(42), 1f);
            p.setMargins(dp(2), dp(10), dp(2), 0);
            moodButtons.addView(button(mood, mood.equals(savedCheckin), v -> {
                getPreferences(MODE_PRIVATE).edit().putString(todayKey("checkin"), mood).apply();
                appendLog("Daily family check-in saved: " + mood);
                renderPage();
            }), p);
        }
        checkin.addView(moodButtons);
        pageContainer.addView(checkin, margin(0, 0, 0, 16));

        addSection("TODAY'S CHECKLIST", "Mark each activity when it is complete");
        LinearLayout taskCard = card(false);
        TextView progress = text("", 12, PURPLE_DARK, true);
        taskCard.addView(progress, margin(0, 0, 0, 8));
        String[][] defaults = {
                {"morning", "Morning preparation"},
                {"school", "School / learning time"},
                {"homework", "Homework or reading"},
                {"movement", "Move, stretch or play outside"},
                {"family", "Spend time with family"},
                {"winddown", "Wind-down before bedtime"}
        };
        ArrayList<CheckBox> boxes = new ArrayList<>();
        String day = todayStamp();
        for (String[] task : defaults) addRoutineCheck(taskCard, boxes, progress, "routine_" + day + "_" + task[0], task[1]);
        JSONArray custom = getCustomTasks();
        for (int i = 0; i < custom.length(); i++) {
            String task = custom.optString(i, "");
            if (!task.trim().isEmpty()) addRoutineCheck(taskCard, boxes, progress, "routine_" + day + "_custom_" + i, task);
        }
        updateRoutineProgress(progress, boxes);
        addButton(taskCard, "ADD CUSTOM TASK", false, v -> addCustomTask());
        addButton(taskCard, "RESET TODAY'S CHECKLIST", false, v -> resetRoutineToday());
        pageContainer.addView(taskCard, margin(0, 0, 0, 16));

        addSection("SCHEDULE PLANNER", "Set family routine times for reference");
        LinearLayout plan = card(false);
        addInfoRow(plan, "Study start", savedTime("study_minute", 16 * 60));
        addInfoRow(plan, "Bedtime", savedTime("bedtime_minute", 21 * 60));
        addButton(plan, "EDIT STUDY START", false, v -> chooseRoutineTime("study_minute", "Choose study start"));
        addButton(plan, "EDIT BEDTIME", false, v -> chooseRoutineTime("bedtime_minute", "Choose bedtime"));
        addButton(plan, "OPEN DIGITAL WELLBEING", true, v -> openDigitalWellbeing());
        pageContainer.addView(plan, margin(0, 0, 0, 16));

        LinearLayout safety = card(false);
        safety.addView(text("FAMILY SAFETY SHORTCUTS", 10, PURPLE_DARK, true));
        addActionRow(safety, "☎", "Emergency contact", "Open the phone dialer with the saved number", () -> dialEmergencyContact());
        addDivider(safety);
        addActionRow(safety, "⏱", "Focus timer", "Start a 25-minute study session", () -> startFocus(25));
        addDivider(safety);
        addActionRow(safety, "☼", "Flashlight / SOS", "Open safe flashlight controls", () -> navigate(TAB_FLASH));
        pageContainer.addView(safety);
        addFooter();
    }

    private void addRoutineCheck(LinearLayout parent, ArrayList<CheckBox> boxes, TextView progress, String key, String label) {
        CheckBox box = new CheckBox(this);
        box.setText(label);
        box.setTextSize(13);
        box.setTextColor(TEXT);
        box.setPadding(dp(2), dp(5), dp(2), dp(5));
        box.setButtonTintList(ColorStateList.valueOf(PURPLE));
        box.setChecked(getPreferences(MODE_PRIVATE).getBoolean(key, false));
        box.setOnCheckedChangeListener((button, checked) -> {
            getPreferences(MODE_PRIVATE).edit().putBoolean(key, checked).apply();
            appendLog((checked ? "Completed routine: " : "Unchecked routine: ") + label);
            updateRoutineProgress(progress, boxes);
        });
        boxes.add(box);
        parent.addView(box, new LinearLayout.LayoutParams(-1, -2));
    }

    private void updateRoutineProgress(TextView progress, ArrayList<CheckBox> boxes) {
        int done = 0;
        for (CheckBox box : boxes) if (box.isChecked()) done++;
        progress.setText(done + " / " + boxes.size() + " completed today");
    }

    private void addCustomTask() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("Example: Pack school bag");
        new AlertDialog.Builder(this).setTitle("Add a family task").setView(input)
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("ADD", (d, w) -> {
                    String value = input.getText().toString().trim();
                    if (value.length() < 2) { toast("Task name is too short"); return; }
                    JSONArray tasks = getCustomTasks();
                    if (tasks.length() >= 12) { toast("Maximum 12 custom tasks"); return; }
                    tasks.put(value);
                    getPreferences(MODE_PRIVATE).edit().putString("custom_tasks", tasks.toString()).apply();
                    appendLog("Custom routine task added");
                    renderPage();
                }).show();
    }

    private JSONArray getCustomTasks() {
        try { return new JSONArray(getPreferences(MODE_PRIVATE).getString("custom_tasks", "[]")); }
        catch (JSONException e) { return new JSONArray(); }
    }

    private void resetRoutineToday() {
        new AlertDialog.Builder(this).setTitle("Reset today's checklist?")
                .setMessage("Only today's checked states will be cleared. Your task list and schedules remain.")
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("RESET", (d, w) -> {
                    android.content.SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                    String day = todayStamp();
                    for (String id : new String[]{"morning", "school", "homework", "movement", "family", "winddown"}) editor.remove("routine_" + day + "_" + id);
                    JSONArray custom = getCustomTasks();
                    for (int i = 0; i < custom.length(); i++) editor.remove("routine_" + day + "_custom_" + i);
                    editor.apply();
                    appendLog("Today's family routine checklist reset");
                    renderPage();
                }).show();
    }

    private void chooseRoutineTime(String preferenceKey, String title) {
        int current = getPreferences(MODE_PRIVATE).getInt(preferenceKey, preferenceKey.startsWith("bedtime") ? 21 * 60 : 16 * 60);
        new TimePickerDialog(this, (view, hour, minute) -> {
            getPreferences(MODE_PRIVATE).edit().putInt(preferenceKey, hour * 60 + minute).apply();
            appendLog(title + " set to " + String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
            renderPage();
        }, current / 60, current % 60, true).show();
    }

    private String savedTime(String key, int fallback) {
        int minute = getPreferences(MODE_PRIVATE).getInt(key, fallback);
        return String.format(Locale.getDefault(), "%02d:%02d", (minute / 60) % 24, minute % 60);
    }

    private String todayStamp() { return new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date()); }
    private String todayKey(String suffix) { return "today_" + todayStamp() + "_" + suffix; }

    private void buildFlashPage() {
        addEyebrow("TOOLS / FLASHLIGHT");
        addPageTitle("Flashlight & SOS", "Use the flash on this phone only. Android will ask for camera permission before the torch can be used.");

        LinearLayout light = card(true);
        TextView big = text("☼", 54, PURPLE, true);
        big.setGravity(Gravity.CENTER);
        light.addView(big, new LinearLayout.LayoutParams(-1, dp(90)));
        flashStatusView = text(flashOn ? "FLASHLIGHT ON" : "FLASHLIGHT OFF", 14, TEXT, true);
        flashStatusView.setGravity(Gravity.CENTER);
        light.addView(flashStatusView, margin(0, 2, 0, 15));
        addButton(light, "TURN FLASHLIGHT ON", true, v -> requestOrTurnTorch(true));
        addButton(light, "TURN FLASHLIGHT OFF", false, v -> turnFlashOff());
        pageContainer.addView(light, margin(0, 0, 0, 17));

        addSection("SOS SIGNAL", "A slow, short pattern with automatic stop");
        LinearLayout sos = card(false);
        sos.addView(text("SAFE SOS BLINK", 12, PURPLE_DARK, true));
        sos.addView(text("The pattern runs for at most 10 seconds at about one blink per second, then turns the torch off automatically. Do not point the light at eyes or use it near people who may be photosensitive.",
                12, MUTED, false), margin(0, 7, 0, 12));
        addButton(sos, "START 10-SECOND SOS", true, v -> startFlashPattern());
        addButton(sos, "STOP FLASH NOW", false, v -> turnFlashOff());
        pageContainer.addView(sos, margin(0, 0, 0, 17));

        LinearLayout warning = card(false);
        warning.addView(text("SAFETY", 11, RED, true));
        warning.addView(text("A rapid 'turbo strobe' mode is intentionally not included. Bright repetitive flashing can trigger discomfort or seizures in photosensitive people. This app uses only a slow, time-limited signal.",
                12, MUTED, false), margin(0, 6, 0, 0));
        pageContainer.addView(warning);
        addFooter();
    }

    private void buildUsagePage() {
        addEyebrow("INSIGHTS / APP USAGE");
        addPageTitle("Screen-time report", "Today's foreground app usage on this phone. Android Usage Access must be enabled in system settings.");
        if (!hasUsageAccess()) {
            LinearLayout permission = card(true);
            permission.addView(pill("PERMISSION NEEDED", ORANGE));
            permission.addView(text("Allow Usage Access", 20, TEXT, true), margin(0, 10, 0, 5));
            permission.addView(text("Android protects app-usage history. Grant access only if you want this on-device report; you can revoke it at any time.",
                    12, MUTED, false), margin(0, 0, 0, 12));
            addButton(permission, "OPEN USAGE ACCESS SETTINGS", true, v -> openUsageAccessSettings());
            pageContainer.addView(permission, margin(0, 0, 0, 16));
        } else {
            long total = todayUsageMillis();
            LinearLayout summary = card(true);
            summary.addView(text("TOTAL TODAY", 10, PURPLE_DARK, true));
            usageSummaryView = text(formatUsage(total), 30, TEXT, true);
            summary.addView(usageSummaryView, margin(0, 5, 0, 2));
            boolean exceeded = total >= dailyLimitHours * 60L * 60L * 1000L;
            summary.addView(pill(exceeded ? "DAILY SOFT GOAL REACHED" : (dailyLimitHours + "H DAILY SOFT GOAL"), exceeded ? ORANGE : GREEN), margin(0, 7, 0, 0));
            pageContainer.addView(summary, margin(0, 0, 0, 12));
            LinearLayout weekly = card(false);
            weekly.addView(text("LAST 7 DAYS", 10, PURPLE_DARK, true));
            weekly.addView(text(formatUsage(weeklyUsageMillis()), 22, TEXT, true), margin(0, 5, 0, 2));
            weekly.addView(text("Combined foreground time reported by Android Usage Access", 10, MUTED, false));
            pageContainer.addView(weekly, margin(0, 0, 0, 16));

            addSection("MOST USED APPS", "Tap an app to set a soft reminder goal or open Android settings");
            LinearLayout apps = card(false);
            List<UsageRow> rows = getUsageRows();
            if (rows.isEmpty()) {
                apps.addView(text("No usage data returned yet", 15, TEXT, true));
                apps.addView(text("Open a few apps and refresh this page. Usage Access may take a little time to populate.", 12, MUTED, false), margin(0, 5, 0, 0));
            } else {
                int shown = Math.min(rows.size(), 12);
                for (int i = 0; i < shown; i++) {
                    UsageRow item = rows.get(i);
                    LinearLayout line = row();
                    TextView rank = text(String.format(Locale.US, "%02d", i + 1), 11, PURPLE, true);
                    line.addView(rank, new LinearLayout.LayoutParams(dp(28), -2));
                    LinearLayout name = column();
                    name.addView(text(item.label, 13, TEXT, true));
                    int appGoal = getPreferences(MODE_PRIVATE).getInt("app_goal_" + item.packageName, 0);
                    String helper = item.packageName;
                    if (appGoal > 0) helper += " • soft goal " + appGoal + " min" + (item.millis >= appGoal * 60000L ? " • reached" : "");
                    name.addView(text(helper, 9, appGoal > 0 && item.millis >= appGoal * 60000L ? ORANGE : MUTED, false));
                    line.addView(name, new LinearLayout.LayoutParams(0, -2, 1f));
                    line.addView(text(formatUsage(item.millis), 12, PURPLE_DARK, true));
                    line.setOnClickListener(v -> showAppUsageOptions(item));
                    apps.addView(line, margin(0, 0, 0, 11));
                    if (i < shown - 1) addDivider(apps);
                }
            }
            pageContainer.addView(apps, margin(0, 0, 0, 15));
            addButton(pageContainer, "REFRESH USAGE REPORT", true, v -> renderPage());
        }
        LinearLayout note = card(false);
        note.addView(text("PRIVACY", 11, PURPLE_DARK, true));
        note.addView(text("Usage history is read on this phone and is not uploaded or shared by HugzzBotzX. Android settings controls this permission.",
                12, MUTED, false), margin(0, 6, 0, 0));
        pageContainer.addView(note);
        addFooter();
    }

    private void buildMorePage() {
        addEyebrow("MORE / APP & HISTORY");
        addPageTitle("More settings", "App information, privacy and the local activity record.");
        LinearLayout app = card(false);
        LinearLayout title = row();
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.hugzzbotzx_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        title.addView(logo, new LinearLayout.LayoutParams(dp(58), dp(58)));
        LinearLayout brand = column();
        brand.setPadding(dp(11), 0, 0, 0);
        brand.addView(text("HugzzBotzX Family", 17, TEXT, true));
        brand.addView(text("Version " + appVersion(), 11, MUTED, false));
        title.addView(brand, new LinearLayout.LayoutParams(0, -2, 1f));
        app.addView(title);
        addInfoRow(app, "Android", Build.VERSION.RELEASE + " / API " + Build.VERSION.SDK_INT);
        addInfoRow(app, "Device", deviceName());
        addInfoRow(app, "Security patch", Build.VERSION.SDK_INT >= 23 ? Build.VERSION.SECURITY_PATCH : "Unavailable");
        addInfoRow(app, "Profile", getPreferences(MODE_PRIVATE).getString("child_nickname", "My child"));
        addInfoRow(app, "Parent PIN", hasParentPin() ? "Enabled" : "Not configured");
        pageContainer.addView(app, margin(0, 0, 0, 16));

        LinearLayout permissions = card(false);
        permissions.addView(text("PERMISSION CENTER", 10, PURPLE_DARK, true));
        addInfoRow(permissions, "Usage Access", hasUsageAccess() ? "Enabled" : "Not enabled");
        addInfoRow(permissions, "Camera for flashlight", Build.VERSION.SDK_INT < 23 || checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ? "Granted" : "Not granted");
        addInfoRow(permissions, "Network state", "Read-only status");
        addButton(permissions, "OPEN ANDROID APP SETTINGS", false, v -> openAppSettings());
        addButton(permissions, "MANAGE USAGE ACCESS", true, v -> openUsageAccessSettings());
        pageContainer.addView(permissions, margin(0, 0, 0, 16));

        addSection("ACTIVITY LOG", activityLog.size() + " latest local events");
        LinearLayout history = card(false);
        if (activityLog.isEmpty()) {
            history.addView(text("No events recorded yet", 14, TEXT, true));
        } else {
            int count = Math.min(activityLog.size(), 25);
            for (int i = 0; i < count; i++) {
                history.addView(text("• " + activityLog.get(i), 11, i == 0 ? TEXT : MUTED, i == 0), margin(0, 0, 0, 8));
            }
        }
        pageContainer.addView(history, margin(0, 0, 0, 12));
        addButton(pageContainer, "EXPORT DEVICE REPORT", true, v -> shareReport());
        addButton(pageContainer, "CLEAR LOCAL ACTIVITY LOG", false, v -> confirmClearHistory());
        addButton(pageContainer, "OPEN APP PERMISSIONS", false, v -> openAppSettings());
        addButton(pageContainer, "OPEN USAGE ACCESS SETTINGS", false, v -> openUsageAccessSettings());
        addButton(pageContainer, "EDIT FAMILY PROFILE", false, v -> navigate(TAB_PARENT));
        addButton(pageContainer, "EXPORT FAMILY ROUTINE & DEVICE REPORT", true, v -> shareReport());

        LinearLayout privacy = card(false);
        privacy.addView(text("PRIVACY PROMISE", 11, PURPLE_DARK, true));
        privacy.addView(text("No stealth mode • no screen capture • no keylogging • no hidden camera or microphone • no remote access service • no data upload in this version. PIN and family routine data stay in local app storage.",
                12, MUTED, false), margin(0, 6, 0, 0));
        pageContainer.addView(privacy, margin(0, 12, 0, 0));
        addFooter();
    }

    private void editChildNickname() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(getPreferences(MODE_PRIVATE).getString("child_nickname", "My child"));
        input.setHint("Profile nickname");
        new AlertDialog.Builder(this).setTitle("Child profile nickname").setView(input)
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("SAVE", (d, w) -> {
                    String value = input.getText().toString().trim();
                    if (value.isEmpty()) value = "My child";
                    if (value.length() > 28) value = value.substring(0, 28);
                    getPreferences(MODE_PRIVATE).edit().putString("child_nickname", value).apply();
                    appendLog("Local family profile nickname updated");
                    renderPage();
                }).show();
    }

    private void editEmergencyContact() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setText(getPreferences(MODE_PRIVATE).getString("emergency_phone", ""));
        input.setHint("Phone number");
        new AlertDialog.Builder(this).setTitle("Emergency contact").setMessage("Save a trusted contact number on this phone. Calls are opened in the dialer for you to review; this app does not place calls automatically.")
                .setView(input).setNegativeButton("CANCEL", null)
                .setPositiveButton("SAVE", (d, w) -> {
                    String value = input.getText().toString().trim();
                    if (value.length() > 24) { toast("Phone number too long"); return; }
                    getPreferences(MODE_PRIVATE).edit().putString("emergency_phone", value).apply();
                    appendLog("Emergency contact updated");
                    renderPage();
                }).show();
    }

    private void dialEmergencyContact() {
        String phone = getPreferences(MODE_PRIVATE).getString("emergency_phone", "");
        if (phone.trim().isEmpty()) { editEmergencyContact(); return; }
        try { startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))); }
        catch (Exception e) { toast("Phone dialer is unavailable"); }
    }

    private boolean hasParentPin() {
        return !getPreferences(MODE_PRIVATE).getString("parent_pin_hash", "").isEmpty();
    }

    private void showParentPinSetupDialog() {
        LinearLayout fields = column();
        fields.setPadding(dp(8), dp(4), dp(8), dp(4));
        EditText first = new EditText(this);
        first.setSingleLine(true);
        first.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        first.setHint("New PIN (4–8 digits)");
        EditText second = new EditText(this);
        second.setSingleLine(true);
        second.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        second.setHint("Confirm PIN");
        fields.addView(first);
        fields.addView(second);
        new AlertDialog.Builder(this).setTitle("Set parent PIN")
                .setMessage("This PIN protects entry to the Family dashboard on this device. It is stored as a salted hash, not as readable text.")
                .setView(fields).setNegativeButton("CANCEL", null)
                .setPositiveButton("SAVE PIN", (d, w) -> {
                    String pin = first.getText().toString();
                    String confirm = second.getText().toString();
                    if (pin.length() < 4 || pin.length() > 8 || !pin.matches("[0-9]+")) { toast("PIN must contain 4–8 digits"); return; }
                    if (!pin.equals(confirm)) { toast("PIN confirmation does not match"); return; }
                    try {
                        byte[] salt = new byte[16];
                        new SecureRandom().nextBytes(salt);
                        String salt64 = Base64.encodeToString(salt, Base64.NO_WRAP);
                        String hash64 = Base64.encodeToString(hashParentPin(salt, pin), Base64.NO_WRAP);
                        getPreferences(MODE_PRIVATE).edit().putString("parent_pin_salt", salt64).putString("parent_pin_hash", hash64).apply();
                        parentUnlocked = true;
                        appendLog("Parent dashboard PIN configured");
                        toast("Parent PIN saved on this device");
                        renderPage();
                    } catch (Exception e) { toast("Could not secure the PIN on this device"); }
                }).show();
    }

    private byte[] hashParentPin(byte[] salt, String pin) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(salt);
        digest.update(pin.getBytes("UTF-8"));
        return digest.digest();
    }

    private void showParentUnlockDialog() {
        if (pinDialogShowing) return;
        pinDialogShowing = true;
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setHint("Parent PIN");
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Parent dashboard locked")
                .setMessage("Enter the parent PIN configured on this phone.")
                .setView(input).setNegativeButton("CANCEL", (d, w) -> { pinDialogShowing = false; })
                .setPositiveButton("UNLOCK", null).create();
        dialog.setOnDismissListener(d -> pinDialogShowing = false);
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            try {
                String pin = input.getText().toString();
                String salt64 = getPreferences(MODE_PRIVATE).getString("parent_pin_salt", "");
                byte[] salt = Base64.decode(salt64, Base64.DEFAULT);
                byte[] expected = Base64.decode(getPreferences(MODE_PRIVATE).getString("parent_pin_hash", ""), Base64.DEFAULT);
                byte[] actual = hashParentPin(salt, pin);
                if (MessageDigest.isEqual(expected, actual)) {
                    parentUnlocked = true;
                    currentTab = TAB_PARENT;
                    dialog.dismiss();
                    renderPage();
                    appendLog("Parent dashboard unlocked");
                } else {
                    input.setError("Incorrect PIN");
                }
            } catch (Exception e) { input.setError("Could not verify PIN"); }
        }));
        dialog.show();
    }

    private void showAppUsageOptions(UsageRow item) {
        int goal = getPreferences(MODE_PRIVATE).getInt("app_goal_" + item.packageName, 0);
        String status = goal > 0 ? "Current soft daily goal: " + goal + " min" : "No app-specific soft goal set";
        String[] actions = {"Set / change soft daily goal", "Open Android app settings", "Remove soft goal"};
        new AlertDialog.Builder(this).setTitle(item.label).setMessage(status).setItems(actions, (d, which) -> {
            if (which == 0) {
                EditText input = new EditText(this);
                input.setSingleLine(true);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                input.setHint("Minutes per day (e.g. 60)");
                if (goal > 0) input.setText(String.valueOf(goal));
                new AlertDialog.Builder(this).setTitle("Soft reminder goal").setMessage("This records a target for your review. Android does not block or close the app.")
                        .setView(input).setNegativeButton("CANCEL", null).setPositiveButton("SAVE", (dd, ww) -> {
                            try {
                                int minutes = Integer.parseInt(input.getText().toString().trim());
                                if (minutes < 5 || minutes > 1440) { toast("Choose 5–1440 minutes"); return; }
                                getPreferences(MODE_PRIVATE).edit().putInt("app_goal_" + item.packageName, minutes).apply();
                                appendLog("Soft app usage goal set for " + item.label + ": " + minutes + " minutes");
                                renderPage();
                            } catch (Exception ex) { toast("Enter a valid number of minutes"); }
                        }).show();
            } else if (which == 1) openPackageSettings(item.packageName);
            else {
                getPreferences(MODE_PRIVATE).edit().remove("app_goal_" + item.packageName).apply();
                appendLog("Soft app usage goal removed for " + item.label);
                renderPage();
            }
        }).show();
    }

    private void startFocus(int minutes) {
        if (focusTimer != null) focusTimer.cancel();
        focusRemainingMs = minutes * 60L * 1000L;
        if (focusTimerStatus != null) focusTimerStatus.setText("Remaining: " + formatClock(focusRemainingMs));
        appendLog("Focus timer started: " + minutes + " minutes");
        focusTimer = new CountDownTimer(focusRemainingMs, 1000L) {
            @Override public void onTick(long millisUntilFinished) {
                focusRemainingMs = millisUntilFinished;
                if (focusTimerStatus != null) focusTimerStatus.setText("Remaining: " + formatClock(focusRemainingMs));
            }
            @Override public void onFinish() {
                focusRemainingMs = 0L;
                if (focusTimerStatus != null) focusTimerStatus.setText("Focus complete • time for a break");
                appendLog("Focus timer finished");
                new AlertDialog.Builder(MainActivity.this).setTitle("Focus session complete")
                        .setMessage("Nice work. Take a short break before your next session.")
                        .setPositiveButton("OK", null).show();
            }
        }.start();
        toast(minutes + "-minute focus timer started");
    }

    private void stopFocus() {
        if (focusTimer != null) focusTimer.cancel();
        focusTimer = null;
        focusRemainingMs = 0L;
        if (focusTimerStatus != null) focusTimerStatus.setText("Focus timer stopped");
        appendLog("Focus timer stopped by user");
        toast("Focus timer dihentikan");
    }

    private String formatClock(long millis) {
        long seconds = Math.max(0L, millis / 1000L);
        return String.format(Locale.getDefault(), "%02d:%02d", seconds / 60L, seconds % 60L);
    }

    private void openPackageSettings(String packageName) {
        try {
            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + packageName));
            startActivity(i);
        } catch (Exception e) { toast("App details tidak tersedia."); }
    }

    private void showRemoteRequirements() {
        new AlertDialog.Builder(this)
                .setTitle("Before connecting a second device")
                .setMessage("A working remote parental-control system needs: (1) HugzzBotzX installed on both devices, (2) parent account sign-in, (3) child-side pairing approval, (4) encrypted communication, (5) a visible status/stop control on the child device, and (6) a secure backend. This release has no backend, so remote flash, remote app blocking and remote device control are not active.\n\nFor real app blocking or device-wide restrictions, Android requires supported Family Link / supervised-device or Android Enterprise enrollment. Ordinary apps cannot silently gain those powers.")
                .setPositiveButton("UNDERSTOOD", null).show();
    }

    private void requestOrTurnTorch(boolean enabled) {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
            return;
        }
        if (enabled) setTorch(true); else setTorch(false);
    }

    private void startFlashPattern() {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
            toast("Berikan izin, lalu tekan SOS sekali lagi.");
            return;
        }
        if (flashCameraId == null) flashCameraId = findFlashCamera();
        if (flashCameraId == null) { toast("Flash LED tidak tersedia pada perangkat ini."); return; }
        stopFlashPattern();
        flashPatternRunning = true;
        flashPulses = 0;
        flashOn = false;
        appendLog("Safe SOS flash pattern started (10 seconds max)");
        handler.post(flashPattern);
        toast("SOS berjalan maksimal 10 detik. Tekan STOP untuk menghentikan.");
    }

    private String findFlashCamera() {
        if (cameraManager == null) return null;
        try {
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
                Boolean hasFlash = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer facing = c.get(CameraCharacteristics.LENS_FACING);
                if (Boolean.TRUE.equals(hasFlash) && (facing == null || facing == CameraCharacteristics.LENS_FACING_BACK)) return id;
            }
        } catch (CameraAccessException | SecurityException ignored) { }
        return null;
    }

    private void setTorch(boolean enabled) {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            toast("Izin kamera diperlukan untuk senter.");
            return;
        }
        try {
            if (flashCameraId == null) flashCameraId = findFlashCamera();
            if (flashCameraId == null || cameraManager == null) { toast("Flash LED tidak ditemukan."); return; }
            cameraManager.setTorchMode(flashCameraId, enabled);
            flashOn = enabled;
            if (flashStatusView != null) flashStatusView.setText(enabled ? "FLASHLIGHT ON" : "FLASHLIGHT OFF");
            if (enabled) appendLog("Flashlight turned on");
        } catch (CameraAccessException | IllegalArgumentException | SecurityException e) {
            flashOn = false;
            toast("Tidak dapat mengakses flash: " + e.getMessage());
        }
    }

    private void stopFlashPattern() {
        flashPatternRunning = false;
        handler.removeCallbacks(flashPattern);
        if (flashOn) {
            try {
                if (cameraManager != null && flashCameraId != null) cameraManager.setTorchMode(flashCameraId, false);
            } catch (Exception ignored) { }
        }
        flashOn = false;
        if (flashStatusView != null) flashStatusView.setText("FLASHLIGHT OFF");
    }

    private void turnFlashOff() {
        stopFlashPattern();
        appendLog("Flashlight turned off");
        toast("Flash dimatikan");
    }

    private boolean hasUsageAccess() {
        try {
            AppOpsManager appOps = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), getPackageName());
            return mode == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) { return false; }
    }

    private void openUsageAccessSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            appendLog("Opened Android Usage Access settings");
        } catch (Exception e) { toast("Pengaturan Usage Access tidak tersedia di perangkat ini."); }
    }

    private void openSystemParentalControls() {
        try {
            startActivity(new Intent("android.settings.PARENTAL_CONTROLS_SETTINGS"));
            appendLog("Opened Android parental controls settings");
        } catch (Exception e) {
            openDigitalWellbeing();
            toast("System parental controls are not available on this device; opened Digital Wellbeing instead.");
        }
    }

    private void openDigitalWellbeing() {
        try {
            Intent i = new Intent("android.settings.DIGITAL_WELLBEING_SETTINGS");
            startActivity(i);
        } catch (Exception e) {
            openUsageAccessSettings();
            toast("Membuka pengaturan penggunaan Android yang tersedia.");
        }
    }

    private void openAppSettings() {
        try {
            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
            startActivity(i);
        } catch (Exception e) { toast("Tidak dapat membuka pengaturan aplikasi."); }
    }

    private long todayUsageMillis() {
        if (!hasUsageAccess()) return 0L;
        UsageStatsManager manager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        if (manager == null) return 0L;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
        long total = 0L;
        Map<String, UsageStats> stats = manager.queryAndAggregateUsageStats(cal.getTimeInMillis(), System.currentTimeMillis());
        if (stats != null) for (UsageStats item : stats.values()) total += item.getTotalTimeInForeground();
        return total;
    }

    private long weeklyUsageMillis() {
        if (!hasUsageAccess()) return 0L;
        UsageStatsManager manager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        if (manager == null) return 0L;
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DAY_OF_YEAR, -6);
        start.set(Calendar.HOUR_OF_DAY, 0); start.set(Calendar.MINUTE, 0); start.set(Calendar.SECOND, 0); start.set(Calendar.MILLISECOND, 0);
        long total = 0L;
        Map<String, UsageStats> stats = manager.queryAndAggregateUsageStats(start.getTimeInMillis(), System.currentTimeMillis());
        if (stats != null) for (UsageStats item : stats.values()) total += item.getTotalTimeInForeground();
        return total;
    }

    private List<UsageRow> getUsageRows() {
        ArrayList<UsageRow> rows = new ArrayList<>();
        if (!hasUsageAccess()) return rows;
        UsageStatsManager manager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        if (manager == null) return rows;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
        Map<String, UsageStats> stats = manager.queryAndAggregateUsageStats(cal.getTimeInMillis(), System.currentTimeMillis());
        if (stats == null) return rows;
        PackageManager pm = getPackageManager();
        for (UsageStats item : stats.values()) {
            if (item.getTotalTimeInForeground() < 60_000L) continue;
            try {
                ApplicationInfo info = pm.getApplicationInfo(item.getPackageName(), 0);
                String label = pm.getApplicationLabel(info).toString();
                if (getPackageName().equals(item.getPackageName())) continue;
                rows.add(new UsageRow(label, item.getPackageName(), item.getTotalTimeInForeground()));
            } catch (PackageManager.NameNotFoundException ignored) { }
        }
        Collections.sort(rows, (a, b) -> Long.compare(b.millis, a.millis));
        return rows;
    }

    private static class UsageRow {
        final String label, packageName;
        final long millis;
        UsageRow(String label, String packageName, long millis) { this.label = label; this.packageName = packageName; this.millis = millis; }
    }

    private void addActionRow(LinearLayout parent, String icon, String title, String subtitle, Runnable action) {
        LinearLayout line = row();
        line.setGravity(Gravity.CENTER_VERTICAL);
        TextView glyph = text(icon, 21, PURPLE, true);
        glyph.setGravity(Gravity.CENTER);
        glyph.setBackground(rounded(SURFACE_TINT, 13, BORDER, 1));
        line.addView(glyph, new LinearLayout.LayoutParams(dp(44), dp(44)));
        LinearLayout labels = column();
        labels.setPadding(dp(11), 0, dp(7), 0);
        labels.addView(text(title, 13, TEXT, true));
        labels.addView(text(subtitle, 10, MUTED, false), margin(0, 3, 0, 0));
        line.addView(labels, new LinearLayout.LayoutParams(0, -2, 1f));
        line.addView(text("›", 24, MUTED, false));
        line.setPadding(0, dp(5), 0, dp(5));
        line.setOnClickListener(v -> action.run());
        parent.addView(line, new LinearLayout.LayoutParams(-1, -2));
    }

    private void addHeaderSpacer() { pageContainer.addView(new View(this), margin(0, 5, 0, 5)); }

    private void addFooter() {
        TextView footer = text("HUGZZBOTZX FAMILY V4  •  OWNER-FIRST PRIVACY", 9, MUTED, true);
        footer.setGravity(Gravity.CENTER);
        pageContainer.addView(footer, margin(0, 22, 0, 5));
    }

    private void addEyebrow(String value) { pageContainer.addView(text(value, 10, PURPLE_DARK, true), margin(0, 0, 0, 5)); }

    private void addPageTitle(String title, String subtitle) {
        pageContainer.addView(text(title, 26, TEXT, true), margin(0, 0, 0, 5));
        pageContainer.addView(text(subtitle, 12, MUTED, false), margin(0, 0, 0, 17));
    }

    private void addSection(String title, String subtitle) {
        pageContainer.addView(text(title, 13, TEXT, true), margin(0, 0, 0, 3));
        pageContainer.addView(text(subtitle, 10, MUTED, false), margin(0, 0, 0, 10));
    }

    private LinearLayout metric(String label, String value, String helper) {
        LinearLayout c = card(false);
        c.setPadding(dp(11), dp(13), dp(9), dp(12));
        c.addView(text(label, 9, MUTED, true));
        c.addView(text(value, 19, TEXT, true), margin(0, 7, 0, 2));
        c.addView(text(helper, 9, MUTED, false));
        return c;
    }

    private LinearLayout.LayoutParams metricWeight() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1f);
        p.setMargins(0, 0, dp(7), 0);
        return p;
    }

    private TextView pill(String label, int tint) {
        TextView t = text(label, 9, tint, true);
        t.setPadding(dp(10), dp(6), dp(10), dp(6));
        t.setBackground(rounded(Color.argb(24, Color.red(tint), Color.green(tint), Color.blue(tint)), 50, Color.argb(55, Color.red(tint), Color.green(tint), Color.blue(tint)), 1));
        return t;
    }

    private LinearLayout card(boolean gradient) {
        LinearLayout c = column();
        c.setPadding(dp(15), dp(15), dp(15), dp(15));
        if (gradient) {
            GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    new int[]{Color.rgb(255, 255, 255), Color.rgb(239, 232, 255), Color.rgb(255, 239, 247)});
            gd.setCornerRadius(dp(22));
            gd.setStroke(dp(1), BORDER);
            c.setBackground(gd);
        } else {
            c.setBackground(rounded(SURFACE, 20, BORDER, 1));
        }
        c.setElevation(dp(2));
        return c;
    }

    private LinearLayout row() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); return l; }
    private LinearLayout column() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        t.setGravity(Gravity.CENTER_VERTICAL); t.setIncludeFontPadding(true);
        return t;
    }

    private Button button(String label, boolean primary, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(label); b.setTextSize(10); b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setTextColor(primary ? Color.WHITE : PURPLE_DARK);
        b.setPadding(dp(9), 0, dp(9), 0);
        b.setBackground(rounded(primary ? PURPLE : SURFACE_TINT, 14, primary ? PURPLE : BORDER, 1));
        b.setOnClickListener(listener);
        return b;
    }

    private void addButton(LinearLayout parent, String label, boolean primary, View.OnClickListener listener) {
        parent.addView(button(label, primary, listener), margin(0, 5, 0, 5));
    }

    private void addCheckRow(LinearLayout parent, String label) {
        LinearLayout line = row();
        line.addView(text("✓", 14, GREEN, true), new LinearLayout.LayoutParams(dp(24), -2));
        line.addView(text(label, 11, MUTED, false), new LinearLayout.LayoutParams(0, -2, 1f));
        parent.addView(line, margin(0, 4, 0, 6));
    }

    private void addInfoRow(LinearLayout parent, String label, String value) {
        LinearLayout line = row();
        line.setGravity(Gravity.CENTER_VERTICAL);
        line.addView(text(label, 11, MUTED, false), new LinearLayout.LayoutParams(0, -2, 1f));
        TextView v = text(value, 11, TEXT, true); v.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        line.addView(v, new LinearLayout.LayoutParams(0, -2, 1.3f));
        parent.addView(line, margin(0, 6, 0, 6));
        addDivider(parent);
    }

    private void addDivider(LinearLayout parent) {
        View d = new View(this); d.setBackgroundColor(BORDER);
        parent.addView(d, new LinearLayout.LayoutParams(-1, dp(1)));
    }

    private GradientDrawable rounded(int color, int radius, int strokeColor, int strokeWidth) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius));
        if (strokeWidth > 0) d.setStroke(dp(strokeWidth), strokeColor);
        return d;
    }

    private LinearLayout.LayoutParams margin(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(dp(left), dp(top), dp(right), dp(bottom)); return p;
    }

    private void confirmClearHistory() {
        new AlertDialog.Builder(this).setTitle("Clear local activity log?")
                .setMessage("This removes activity events stored on this phone.")
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("CLEAR", (d, w) -> {
                    activityLog.clear(); saveState(); renderPage(); toast("Local history cleared");
                }).show();
    }

    private void shareReport() {
        StringBuilder report = new StringBuilder();
        report.append("HUGZZBOTZX FAMILY DEVICE REPORT\n");
        report.append("Generated: ").append(DateFormat.getDateTimeInstance().format(new Date())).append("\n\n");
        report.append("Device: ").append(deviceName()).append("\n");
        report.append("Android: ").append(Build.VERSION.RELEASE).append(" / API ").append(Build.VERSION.SDK_INT).append("\n");
        report.append("Battery: ").append(batteryStatus()).append(" / ").append(batteryPercent()).append("%\n");
        report.append("Storage: ").append(storageSummary()).append("\n");
        report.append("Memory: ").append(memorySummary()).append("\n");
        report.append("Network: ").append(networkLabel()).append("\n");
        report.append("Usage access enabled: ").append(hasUsageAccess()).append("\n");
        report.append("Today's app usage: ").append(hasUsageAccess() ? formatUsage(todayUsageMillis()) : "Not permitted").append("\n");
        report.append("Daily reminder goal: ").append(dailyLimitHours).append(" hours (soft reminder only)\n");
        report.append("Family profile: ").append(getPreferences(MODE_PRIVATE).getString("child_nickname", "My child")).append("\n");
        report.append("Study start: ").append(savedTime("study_minute", 16 * 60)).append("\n");
        report.append("Bedtime plan: ").append(savedTime("bedtime_minute", 21 * 60)).append("\n");
        report.append("Today's check-in: ").append(getPreferences(MODE_PRIVATE).getString(todayKey("checkin"), "Not completed")).append("\n");
        report.append("Last 7 days usage: ").append(hasUsageAccess() ? formatUsage(weeklyUsageMillis()) : "Not permitted").append("\n\n");
        report.append("PRIVACY: Usage information is queried locally and is not transmitted by this version. No remote device command, screen capture, microphone, keylogging or hidden monitoring is implemented.\n\n");
        report.append("RECENT APP EVENTS\n");
        if (activityLog.isEmpty()) report.append("No events\n");
        else for (int i = 0; i < Math.min(activityLog.size(), 25); i++) report.append("- ").append(activityLog.get(i)).append("\n");
        Intent send = new Intent(Intent.ACTION_SEND); send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "HugzzBotzX Family device report"); send.putExtra(Intent.EXTRA_TEXT, report.toString());
        appendLog("Local device report shared through Android share sheet");
        startActivity(Intent.createChooser(send, "Share device report"));
    }

    private void loadState() {
        dailyLimitHours = getPreferences(MODE_PRIVATE).getInt("daily_limit_hours", 3);
        String raw = getPreferences(MODE_PRIVATE).getString("activity_log", "[]");
        try { JSONArray a = new JSONArray(raw); for (int i = 0; i < a.length(); i++) activityLog.add(a.optString(i)); }
        catch (JSONException ignored) { activityLog.clear(); }
    }

    private void saveState() {
        JSONArray a = new JSONArray(); for (String s : activityLog) a.put(s);
        getPreferences(MODE_PRIVATE).edit().putString("activity_log", a.toString()).apply();
    }

    private void appendLog(String event) {
        String stamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        activityLog.add(0, stamp + " • " + event);
        while (activityLog.size() > 80) activityLog.remove(activityLog.size() - 1);
        saveState();
    }

    private int batteryPercent() {
        Intent i = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (i == null) return -1;
        int level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1), scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return level < 0 || scale <= 0 ? -1 : Math.round(level * 100f / scale);
    }

    private String batteryStatus() {
        Intent i = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (i == null) return "Unavailable";
        int s = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        if (s == BatteryManager.BATTERY_STATUS_CHARGING) return "Charging";
        if (s == BatteryManager.BATTERY_STATUS_FULL) return "Full";
        if (s == BatteryManager.BATTERY_STATUS_DISCHARGING) return "Discharging";
        return "Standby";
    }

    private String deviceName() { return Build.MANUFACTURER + " " + Build.MODEL; }

    private String storageSummary() {
        try { StatFs s = new StatFs(Environment.getDataDirectory().getPath()); return formatBytes(s.getAvailableBytes()) + " free / " + formatBytes(s.getTotalBytes()); }
        catch (Exception e) { return "Unavailable"; }
    }

    private String storageFreeShort() {
        try { StatFs s = new StatFs(Environment.getDataDirectory().getPath()); return formatBytesShort(s.getAvailableBytes()); }
        catch (Exception e) { return "—"; }
    }

    private String memorySummary() {
        try { ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE); ActivityManager.MemoryInfo m = new ActivityManager.MemoryInfo(); am.getMemoryInfo(m); return formatBytes(m.availMem) + " free / " + formatBytes(m.totalMem); }
        catch (Exception e) { return "Unavailable"; }
    }

    private String memoryFreeShort() {
        try { ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE); ActivityManager.MemoryInfo m = new ActivityManager.MemoryInfo(); am.getMemoryInfo(m); return formatBytesShort(m.availMem); }
        catch (Exception e) { return "—"; }
    }

    private String networkLabel() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (cm == null) return "Unavailable";
            Network n = cm.getActiveNetwork(); if (n == null) return "Offline";
            NetworkCapabilities c = cm.getNetworkCapabilities(n); if (c == null) return "Offline";
            if (c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return "Wi-Fi";
            if (c.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return "Mobile data";
            if (c.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) return "Ethernet";
            return "Connected";
        } catch (Exception e) { return "Unavailable"; }
    }

    private String formatUsage(long ms) {
        long mins = Math.max(0, ms / 60000L); long h = mins / 60L; long m = mins % 60L;
        return h > 0 ? h + "h " + m + "m" : m + " min";
    }

    private String formatBytes(long bytes) {
        if (bytes < 0) return "Unavailable"; double v = bytes; String[] units = {"B", "KB", "MB", "GB", "TB"}; int i = 0;
        while (v >= 1024 && i < units.length - 1) { v /= 1024; i++; }
        return String.format(Locale.getDefault(), "%.1f %s", v, units[i]);
    }

    private String formatBytesShort(long bytes) {
        if (bytes < 0) return "—"; double v = bytes; String[] units = {"B", "K", "M", "G", "T"}; int i = 0;
        while (v >= 1024 && i < units.length - 1) { v /= 1024; i++; }
        return String.format(Locale.getDefault(), "%.1f%s", v, units[i]);
    }

    private String appVersion() {
        try { return getPackageManager().getPackageInfo(getPackageName(), 0).versionName; }
        catch (Exception e) { return "4.0.0"; }
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private void toast(String value) { Toast.makeText(this, value, Toast.LENGTH_SHORT).show(); }
}
