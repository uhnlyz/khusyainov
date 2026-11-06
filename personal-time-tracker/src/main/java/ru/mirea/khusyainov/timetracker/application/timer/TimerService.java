package ru.mirea.khusyainov.timetracker.application.timer;

import ru.mirea.khusyainov.timetracker.domain.model.PauseRecord;
import ru.mirea.khusyainov.timetracker.domain.model.Task;
import ru.mirea.khusyainov.timetracker.domain.model.TimerStatus;
import ru.mirea.khusyainov.timetracker.domain.model.WorkSession;
import ru.mirea.khusyainov.timetracker.domain.repository.WorkSessionRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Контекст паттерна State. Разрешённые операции делегируются текущему объекту-состоянию.
 * Observer используется для публикации снимка таймера подписанным представлениям.
 */
public final class TimerService implements AutoCloseable {
    private final WorkSessionRepository sessionRepository;
    private final Clock clock;
    private final List<TimerObserver> observers = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService ticker;

    private TimerState state = new IdleState();
    private Task currentTask;
    private Instant sessionStartedAt;
    private Instant activeIntervalStartedAt;
    private Instant pauseStartedAt;
    private String currentPauseReason;
    private long accumulatedActiveSeconds;
    private final List<PauseRecord> pauses = new ArrayList<>();

    public TimerService(WorkSessionRepository sessionRepository, Clock clock) {
        this.sessionRepository = sessionRepository;
        this.clock = clock;
        this.ticker = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "time-tracker-ticker");
            thread.setDaemon(true);
            return thread;
        });
        ticker.scheduleAtFixedRate(this::publishSnapshot, 0, 1, TimeUnit.SECONDS);
    }

    public synchronized void start(Task task) {
        if (task == null) throw new IllegalArgumentException("Сначала выберите задачу");
        state.start(this, task);
    }

    public synchronized void pause(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Для паузы необходимо указать причину");
        }
        state.pause(this, reason.trim());
    }

    public synchronized void resume() {
        state.resume(this);
    }

    public synchronized void stop() {
        state.stop(this);
    }

    public void addObserver(TimerObserver observer) {
        observers.add(observer);
        observer.onTimerUpdated(snapshot());
    }

    public void removeObserver(TimerObserver observer) {
        observers.remove(observer);
    }

    public synchronized TimerSnapshot snapshot() {
        if (currentTask == null) return TimerSnapshot.idle();
        long elapsed = accumulatedActiveSeconds;
        if (state.status() == TimerStatus.RUNNING && activeIntervalStartedAt != null) {
            elapsed += Math.max(0, Duration.between(activeIntervalStartedAt, clock.instant()).toSeconds());
        }
        return new TimerSnapshot(state.status(), currentTask.title(), elapsed, currentTask.plannedSeconds());
    }

    synchronized void startInternal(Task task) {
        currentTask = task;
        sessionStartedAt = clock.instant();
        activeIntervalStartedAt = sessionStartedAt;
        pauseStartedAt = null;
        currentPauseReason = null;
        accumulatedActiveSeconds = 0;
        pauses.clear();
        state = new RunningState();
        publishSnapshot();
    }

    synchronized void pauseInternal(String reason) {
        Instant now = clock.instant();
        accumulatedActiveSeconds += Math.max(0, Duration.between(activeIntervalStartedAt, now).toSeconds());
        activeIntervalStartedAt = null;
        pauseStartedAt = now;
        currentPauseReason = reason;
        state = new PausedState();
        publishSnapshot();
    }

    synchronized void resumeInternal() {
        closeCurrentPause(clock.instant());
        activeIntervalStartedAt = clock.instant();
        state = new RunningState();
        publishSnapshot();
    }

    synchronized void stopInternal() {
        Instant now = clock.instant();
        if (state.status() == TimerStatus.RUNNING && activeIntervalStartedAt != null) {
            accumulatedActiveSeconds += Math.max(0, Duration.between(activeIntervalStartedAt, now).toSeconds());
        } else if (state instanceof PausedState) {
            closeCurrentPause(now);
        }
        WorkSession session = new WorkSession(
                UUID.randomUUID(), currentTask.id(), sessionStartedAt, now,
                accumulatedActiveSeconds, List.copyOf(pauses));
        sessionRepository.save(session);
        activeIntervalStartedAt = null;
        state = new FinishedState();
        publishSnapshot();
    }

    private void closeCurrentPause(Instant endedAt) {
        if (pauseStartedAt != null && currentPauseReason != null) {
            pauses.add(new PauseRecord(UUID.randomUUID(), pauseStartedAt, endedAt, currentPauseReason));
        }
        pauseStartedAt = null;
        currentPauseReason = null;
    }

    private void publishSnapshot() {
        TimerSnapshot current = snapshot();
        for (TimerObserver observer : observers) {
            try {
                observer.onTimerUpdated(current);
            } catch (RuntimeException ignored) {
                // Ошибка одного представления не должна останавливать таймер и другие подписки.
            }
        }
    }

    @Override
    public void close() {
        ticker.shutdownNow();
    }
}
