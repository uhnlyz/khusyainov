package ru.mirea.khusyainov.timetracker.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record WorkSession(
        UUID id,
        UUID taskId,
        Instant startedAt,
        Instant endedAt,
        long activeSeconds,
        List<PauseRecord> pauses) {

    public WorkSession {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(taskId, "taskId");
        Objects.requireNonNull(startedAt, "startedAt");
        if (endedAt == null || endedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("Завершённый сеанс должен иметь корректное время окончания");
        }
        if (activeSeconds < 0) {
            throw new IllegalArgumentException("Активное время не может быть отрицательным");
        }
        pauses = List.copyOf(Objects.requireNonNull(pauses, "pauses"));
    }
}
