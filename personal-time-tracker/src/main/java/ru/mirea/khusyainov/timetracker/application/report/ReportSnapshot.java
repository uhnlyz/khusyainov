package ru.mirea.khusyainov.timetracker.application.report;

import java.time.Instant;
import java.util.Objects;

public record ReportSnapshot(
        String title,
        Instant generatedAt,
        long plannedSeconds,
        long actualSeconds,
        int pauseCount,
        long pausedSeconds) {
    public ReportSnapshot {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Название отчёта обязательно");
        Objects.requireNonNull(generatedAt, "generatedAt");
        if (plannedSeconds < 0 || actualSeconds < 0 || pauseCount < 0 || pausedSeconds < 0) {
            throw new IllegalArgumentException("Показатели отчёта не могут быть отрицательными");
        }
    }
}
