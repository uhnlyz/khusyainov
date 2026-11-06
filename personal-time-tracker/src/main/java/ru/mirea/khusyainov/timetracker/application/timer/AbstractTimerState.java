package ru.mirea.khusyainov.timetracker.application.timer;

import ru.mirea.khusyainov.timetracker.domain.model.Task;

abstract class AbstractTimerState implements TimerState {
    protected IllegalStateException invalid(String action) {
        return new IllegalStateException("Операция «" + action + "» недоступна в текущем состоянии таймера");
    }

    @Override public void start(TimerService timer, Task task) { throw invalid("Старт"); }
    @Override public void pause(TimerService timer, String reason) { throw invalid("Пауза"); }
    @Override public void resume(TimerService timer) { throw invalid("Продолжить"); }
    @Override public void stop(TimerService timer) { throw invalid("Стоп"); }
}
