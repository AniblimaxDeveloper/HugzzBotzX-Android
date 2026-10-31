package com.hugzzbotzx.app;

import android.content.Context;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Small note vault stored exclusively in this application's private internal files directory. */
public final class LocalVaultStore {
    private static final int MAX_NOTE_BYTES = 64 * 1024;
    private final File directory;

    public LocalVaultStore(Context context) {
        directory = new File(context.getFilesDir(), "private_vault");
        // Create lazily when a vault action is used; app launch must not fail if storage is full.
    }

    public synchronized List<String> listNotes() {
        ArrayList<String> names = new ArrayList<>();
        if (!directory.exists() && !directory.mkdirs()) return names;
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".txt") && new File(dir, name).isFile());
        if (files != null) {
            for (File file : files) names.add(file.getName().substring(0, file.getName().length() - 4));
        }
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    public synchronized String readNote(String title) throws Exception {
        File file = resolve(title);
        if (!file.isFile()) throw new Exception("Note not found");
        if (file.length() > MAX_NOTE_BYTES) throw new Exception("Note exceeds the 64 KB limit");
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            char[] buffer = new char[4096];
            int count;
            while ((count = reader.read(buffer)) != -1) result.append(buffer, 0, count);
        }
        return result.toString();
    }

    public synchronized void saveNote(String title, String text) throws Exception {
        if (text == null) text = "";
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        if (data.length > MAX_NOTE_BYTES) throw new Exception("Note is larger than 64 KB");
        File target = resolve(title);
        File temp = new File(directory, target.getName() + ".tmp");
        try (FileOutputStream out = new FileOutputStream(temp)) {
            out.write(data);
            out.getFD().sync();
        }
        if (target.exists() && !target.delete()) {
            temp.delete();
            throw new Exception("Could not replace existing note");
        }
        if (!temp.renameTo(target)) {
            temp.delete();
            throw new Exception("Could not finish saving note");
        }
    }

    public synchronized boolean deleteNote(String title) {
        File file;
        try { file = resolve(title); }
        catch (Exception e) { return false; }
        return !file.exists() || file.delete();
    }

    private File resolve(String title) throws Exception {
        if (!directory.exists() && !directory.mkdirs()) throw new Exception("Could not create private vault directory");
        if (title == null) throw new Exception("Note title is required");
        String safe = title.trim().replaceAll("\\s+", " ");
        if (safe.length() < 1 || safe.length() > 40 || !safe.matches("[A-Za-z0-9_-][A-Za-z0-9 _-]{0,39}")) {
            throw new Exception("Title must be 1–40 letters, numbers, spaces, _ or -; path separators are not allowed");
        }
        File file = new File(directory, safe + ".txt");
        if (!file.getCanonicalFile().getParentFile().equals(directory.getCanonicalFile())) {
            throw new Exception("Invalid note path");
        }
        return file;
    }
}
