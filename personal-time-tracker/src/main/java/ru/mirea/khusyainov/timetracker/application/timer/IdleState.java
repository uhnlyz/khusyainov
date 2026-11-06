package ru.mirea.khusyainov.timetracker.application.timer;

import ru.mirea.khusyainov.timetracker.domain.model.Task;

final class IdleState extends AbstractTimerState {
    @Override
    public void start(TimerService timer, Task task) {
        timer.startInternal(task);
    }
}
