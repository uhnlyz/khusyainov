package ru.mirea.khusyainov.timetracker.application.timer;

import ru.mirea.khusyainov.timetracker.domain.model.Task;
import ru.mirea.khusyainov.timetracker.domain.model.TimerStatus;

final class IdleState extends AbstractTimerState {
    @Override
    public TimerStatus status() {
        return TimerStatus.IDLE;
    }

    @Override
    public void start(TimerService timer, Task task) {
        timer.startInternal(task);
    }
}
