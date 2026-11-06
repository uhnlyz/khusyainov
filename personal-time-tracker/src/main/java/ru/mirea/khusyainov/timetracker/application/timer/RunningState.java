package ru.mirea.khusyainov.timetracker.application.timer;

import ru.mirea.khusyainov.timetracker.domain.model.TimerStatus;

final class RunningState extends AbstractTimerState {
    @Override
    public TimerStatus status() {
        return TimerStatus.RUNNING;
    }

    @Override
    public void pause(TimerService timer, String reason) {
        timer.pauseInternal(reason);
    }

    @Override
    public void stop(TimerService timer) {
        timer.stopInternal();
    }
}
