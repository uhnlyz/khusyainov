package ru.mirea.khusyainov.timetracker.application.timer;

final class RunningState extends AbstractTimerState {
    @Override
    public void pause(TimerService timer, String reason) {
        timer.pauseInternal(reason);
    }

    @Override
    public void stop(TimerService timer) {
        timer.stopInternal();
    }
}
