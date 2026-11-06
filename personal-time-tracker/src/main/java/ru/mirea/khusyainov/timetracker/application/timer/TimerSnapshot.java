package ru.mirea.khusyainov.timetracker.application.timer;

import ru.mirea.khusyainov.timetracker.domain.model.TimerStatus;

public record TimerSnapshot(TimerStatus status, String taskTitle, long elapsedSeconds, long plannedSeconds) {
    public static TimerSnapshot idle() {
        return new TimerSnapshot(TimerStatus.IDLE, "Задача не выбрана", 0, 0);
    }
}
