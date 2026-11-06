package ru.mirea.khusyainov.timetracker.application.timer;

import ru.mirea.khusyainov.timetracker.domain.model.TimerStatus;

final class PausedState extends AbstractTimerState {
    @Override
    public TimerStatus status() {
        return TimerStatus.PAUSED;
    }

    @Override
    public void resume(TimerService timer) {
        timer.resumeInternal();
    }

    @Override
    public void stop(TimerService timer) {
        timer.stopInternal();
    }
}
