package com.jaydeep.jobhunter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.ZoneId;

public class DailyQuota {
    private static final int LIMIT = 40;
    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

    private final Path path;
    private LocalDate date;
    private int count;

    public DailyQuota(String file) {
        path = Path.of(file);
        load();
    }

    public int remaining() {
        resetIfNewDay();
        return Math.max(0, LIMIT - count);
    }

    public void add(int amount) throws IOException {
        if (amount <= 0) return;
        resetIfNewDay();
        count += amount;
        save();
    }

    private void resetIfNewDay() {
        LocalDate today = LocalDate.now(ZONE);
        if (!today.equals(date)) {
            date = today;
            count = 0;
            try {
                save();
            } catch (IOException e) {
                throw new RuntimeException("Could not persist daily quota", e);
            }
        }
    }

    private void load() {
        try {
            if (Files.exists(path)) {
                String value = Files.readString(path).trim();
                String[] parts = value.split("\\n", -1);
                if (parts.length >= 2) {
                    date = LocalDate.parse(parts[0].trim());
                    count = Integer.parseInt(parts[1].trim());
                }
            }
        } catch (Exception e) {
            date = null;
            count = 0;
        }
        resetIfNewDay();
    }

    private void save() throws IOException {
        Files.writeString(path, date + "\n" + count + "\n",
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }
}
