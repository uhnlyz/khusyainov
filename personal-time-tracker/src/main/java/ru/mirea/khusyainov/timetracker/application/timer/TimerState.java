package ru.mirea.khusyainov.timetracker.application.timer;

import ru.mirea.khusyainov.timetracker.domain.model.Task;

public interface TimerState {
    void start(TimerService timer, Task task);
    void pause(TimerService timer, String reason);
    void resume(TimerService timer);
    void stop(TimerService timer);
}
