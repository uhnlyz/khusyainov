package ru.mirea.khusyainov.timetracker.application.timer;

@FunctionalInterface
public interface TimerObserver {
    void onTimerUpdated(TimerSnapshot snapshot);
}
