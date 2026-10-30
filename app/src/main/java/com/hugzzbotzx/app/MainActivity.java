package com.hugzzbotzx.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * HugzzBotzX - consent-based device support starter.
 * This build is deliberately local-only: no network, screen capture,
 * microphone, camera, Accessibility Service, or remote input functionality.
 */
public class MainActivity extends Activity {
    private static final int BG = Color.rgb(8, 8, 11);
    private static final int PANEL = Color.rgb(21, 21, 27);
    private static final int TEXT = Color.rgb(244, 244, 248);
    private static final int MUTED = Color.rgb(160, 160, 174);
    private static final int LINE = Color.rgb(48, 48, 59);
    private static final int ACCENT = Color.rgb(224, 224, 234);
    private static final int GREEN = Color.rgb(130, 220, 155);

    private LinearLayout root;
    private TextView sessionValue;
    private TextView pairingValue;
    private TextView pairingExpiry;
    private TextView sessionBanner;
    private TextView batteryValue;
    private boolean sessionActive = false;
    private String pairingCode = "Belum dibuat";
    private long pairingExpiresAt = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        buildScreen();
        refreshDeviceInfo();
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(18), dp(20), dp(30));
        scroll.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout brandRow = new LinearLayout(this);
        brandRow.setGravity(Gravity.CENTER_VERTICAL);
        brandRow.setOrientation(LinearLayout.HORIZONTAL);
        TextView brandMark = text("HX", 18, TEXT, true);
        brandMark.setGravity(Gravity.CENTER);
        GradientDrawable markBg = rounded(Color.rgb(35, 35, 43), 14, LINE, 1);
        brandMark.setBackground(markBg);
        brandMark.setWidth(dp(48));
        brandMark.setHeight(dp(48));
        brandRow.addView(brandMark);

        LinearLayout brandText = new LinearLayout(this);
        brandText.setOrientation(LinearLayout.VERTICAL);
        brandText.setPadding(dp(12), 0, 0, 0);
        brandText.addView(text("HUGZZBOTZX", 17, TEXT, true));
        brandText.addView(text("CONSENT DEVICE SUPPORT", 10, MUTED, true));
        brandRow.addView(brandText, new LinearLayout.LayoutParams(0, -2, 1));
        TextView version = text("V1.0", 10, MUTED, true);
        version.setBackground(rounded(PANEL, 20, LINE, 1));
        version.setPadding(dp(10), dp(7), dp(10), dp(7));
        brandRow.addView(version);
        root.addView(brandRow, margin(0, 0, 0, 22));

        TextView heading = text("Support, with\npermission.", 31, TEXT, true);
        heading.setLineSpacing(dp(2), 1f);
        root.addView(heading, margin(0, 0, 0, 8));
        root.addView(text("Kelola sesi bantuan secara transparan. Pemilik perangkat tetap memegang kendali.",
                14, MUTED, false), margin(0, 0, 0, 18));

        sessionBanner = text("●  TIDAK ADA SESI AKTIF", 11, GREEN, true);
        sessionBanner.setPadding(dp(13), dp(11), dp(13), dp(11));
        sessionBanner.setBackground(rounded(Color.rgb(20, 34, 25), 14,
                Color.rgb(48, 75, 56), 1));
        root.addView(sessionBanner, margin(0, 0, 0, 18));

        root.addView(sectionLabel("PERANGKAT INI"), margin(0, 0, 0, 9));
        LinearLayout deviceCard = card();
        deviceCard.addView(text(Build.MANUFACTURER + " " + Build.MODEL, 17, TEXT, true));
        deviceCard.addView(text("Android " + Build.VERSION.RELEASE + "  •  API "
                + Build.VERSION.SDK_INT, 12, MUTED, false), margin(0, 5, 0, 13));
        LinearLayout batteryRow = new LinearLayout(this);
        batteryRow.setGravity(Gravity.CENTER_VERTICAL);
        batteryRow.addView(text("Baterai", 13, MUTED, false),
                new LinearLayout.LayoutParams(0, -2, 1));
        batteryValue = text("Membaca…", 13, TEXT, true);
        batteryRow.addView(batteryValue);
        deviceCard.addView(batteryRow);
        root.addView(deviceCard, margin(0, 0, 0, 20));

        root.addView(sectionLabel("PAIRING DEMO"), margin(0, 0, 0, 9));
        LinearLayout pairingCard = card();
        pairingCard.addView(text("Kode undangan lokal", 15, TEXT, true));
        pairingValue = text(pairingCode, 25, TEXT, true);
        pairingValue.setLetterSpacing(0.12f);
        pairingCard.addView(pairingValue, margin(0, 12, 0, 4));
        pairingExpiry = text("Kode hanya contoh di perangkat ini; belum terhubung ke jaringan.",
                11, MUTED, false);
        pairingCard.addView(pairingExpiry, margin(0, 0, 0, 14));
        Button makeCode = button("BUAT KODE DEMO", true);
        makeCode.setOnClickListener(v -> createPairingCode());
        pairingCard.addView(makeCode, fullWidth());
        root.addView(pairingCard, margin(0, 0, 0, 20));

        root.addView(sectionLabel("KONTROL SESI"), margin(0, 0, 0, 9));
        LinearLayout sessionCard = card();
        sessionCard.addView(text("Sesi bantuan", 15, TEXT, true));
        sessionCard.addView(text("Mulai hanya setelah pemilik perangkat membaca dan menyetujui pemberitahuan. Ini adalah simulasi lokal, bukan remote control.",
                12, MUTED, false), margin(0, 7, 0, 14));
        sessionValue = text("Status: tidak aktif", 13, MUTED, true);
        sessionCard.addView(sessionValue, margin(0, 0, 0, 13));
        Button startSession = button("MULAI SESI DENGAN PERSETUJUAN", true);
        startSession.setOnClickListener(v -> showConsentDialog());
        sessionCard.addView(startSession, fullWidth());
        Button stopSession = button("AKHIRI SESI", false);
        stopSession.setOnClickListener(v -> endSession());
        LinearLayout.LayoutParams stopParams = fullWidth();
        stopParams.topMargin = dp(9);
        sessionCard.addView(stopSession, stopParams);
        root.addView(sessionCard, margin(0, 0, 0, 20));

        root.addView(sectionLabel("BATASAN KEAMANAN"), margin(0, 0, 0, 9));
        LinearLayout safetyCard = card();
        safetyCard.addView(text("✓  Tidak meminta izin sensitif", 13, TEXT, true));
        safetyCard.addView(text("✓  Tidak merekam layar, kamera, atau mikrofon", 13, TEXT, true),
                margin(0, 8, 0, 0));
        safetyCard.addView(text("✓  Tidak membaca pesan, file, atau ketikan", 13, TEXT, true),
                margin(0, 8, 0, 0));
        safetyCard.addView(text("✓  Tidak berjalan diam-diam di latar belakang", 13, TEXT, true),
                margin(0, 8, 0, 0));
        root.addView(safetyCard, margin(0, 0, 0, 18));

        TextView footer = text("HUGZZBOTZX  •  PRIVATE BY DESIGN\nBuild awal / local demo", 10,
                MUTED, true);
        footer.setGravity(Gravity.CENTER);
        footer.setLineSpacing(dp(4), 1f);
        root.addView(footer, margin(0, 4, 0, 8));
        setContentView(scroll);
    }

    private void createPairingCode() {
        int value = new SecureRandom().nextInt(900000) + 100000;
        pairingCode = String.format(Locale.US, "%06d", value);
        pairingExpiresAt = System.currentTimeMillis() + 5 * 60 * 1000L;
        pairingValue.setText(pairingCode);
        String expiry = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
                .format(new Date(pairingExpiresAt));
        pairingExpiry.setText("Demo lokal • label kedaluwarsa " + expiry
                + " • tidak dapat dipakai untuk koneksi jarak jauh.");
    }

    private void showConsentDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Persetujuan sesi bantuan")
                .setMessage("Sesi ini hanya simulasi yang terlihat di aplikasi ini. Tidak ada layar, kamera, mikrofon, file, atau input yang dibagikan. Dengan memilih SETUJU, kamu mengaktifkan indikator sesi demo. Kamu dapat mengakhirinya kapan saja.")
                .setNegativeButton("BATAL", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("SETUJU & MULAI", (dialog, which) -> startSession())
                .show();
    }

    private void startSession() {
        sessionActive = true;
        sessionValue.setText("Status: sesi demo aktif");
        sessionValue.setTextColor(GREEN);
        sessionBanner.setText("●  SESI DEMO AKTIF — PEMILIK MEMEGANG KENDALI");
        sessionBanner.setTextColor(GREEN);
        sessionBanner.setBackground(rounded(Color.rgb(20, 34, 25), 14,
                Color.rgb(48, 75, 56), 1));
    }

    private void endSession() {
        sessionActive = false;
        sessionValue.setText("Status: tidak aktif");
        sessionValue.setTextColor(MUTED);
        sessionBanner.setText("●  TIDAK ADA SESI AKTIF");
        sessionBanner.setTextColor(GREEN);
        sessionBanner.setBackground(rounded(Color.rgb(20, 34, 25), 14,
                Color.rgb(48, 75, 56), 1));
    }

    private void refreshDeviceInfo() {
        try {
            Intent battery = registerReceiver(null,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (battery != null) {
                int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                if (level >= 0 && scale > 0) {
                    int percent = Math.round(level * 100f / scale);
                    batteryValue.setText(percent + "%");
                    return;
                }
            }
            batteryValue.setText("Tidak tersedia");
        } catch (Exception ignored) {
            batteryValue.setText("Tidak tersedia");
        }
    }

    private LinearLayout card() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(16), dp(16), dp(16));
        box.setBackground(rounded(PANEL, 20, LINE, 1));
        return box;
    }

    private TextView sectionLabel(String value) {
        TextView view = text(value, 10, MUTED, true);
        view.setLetterSpacing(0.16f);
        return view;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(color);
        view.setTextSize(sp);
        view.setIncludeFontPadding(true);
        if (bold) view.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        else view.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        return view;
    }

    private Button button(String label, boolean primary) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(11);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setAllCaps(false);
        b.setTextColor(primary ? Color.rgb(12, 12, 16) : TEXT);
        b.setPadding(dp(12), dp(11), dp(12), dp(11));
        b.setMinHeight(dp(50));
        b.setBackground(rounded(primary ? ACCENT : Color.rgb(36, 36, 45), 14,
                primary ? ACCENT : LINE, 1));
        return b;
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

    private LinearLayout.LayoutParams fullWidth() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
