package ru.mirea.khusyainov.timetracker.application.timer;

final class PausedState extends AbstractTimerState {
    @Override
    public void resume(TimerService timer) {
        timer.resumeInternal();
    }

    @Override
    public void stop(TimerService timer) {
        timer.stopInternal();
    }
}
