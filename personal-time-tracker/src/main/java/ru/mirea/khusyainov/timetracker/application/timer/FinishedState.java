package ru.mirea.khusyainov.timetracker.application.timer;

import ru.mirea.khusyainov.timetracker.domain.model.Task;
import ru.mirea.khusyainov.timetracker.domain.model.TimerStatus;

final class FinishedState extends AbstractTimerState {
    @Override
    public TimerStatus status() {
        return TimerStatus.FINISHED;
    }

    @Override
    public void start(TimerService timer, Task task) {
        timer.startInternal(task);
    }
}
