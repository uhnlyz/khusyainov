package ru.mirea.khusyainov.timetracker.application.analytics;

public record AnalyticsInput(
        String taskTitle,
        long plannedSeconds,
        long actualSeconds,
        int pauseCount,
        long pausedSeconds) {
    public AnalyticsInput {
        if (plannedSeconds < 0 || actualSeconds < 0 || pauseCount < 0 || pausedSeconds < 0) {
            throw new IllegalArgumentException("Показатели аналитики не могут быть отрицательными");
        }
    }
}
