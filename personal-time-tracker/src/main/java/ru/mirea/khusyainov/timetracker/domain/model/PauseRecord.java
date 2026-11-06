package ru.mirea.khusyainov.timetracker.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record PauseRecord(UUID id, Instant startedAt, Instant endedAt, String reason) {
    public PauseRecord {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(startedAt, "startedAt");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Причина паузы обязательна");
        }
        reason = reason.trim();
        if (endedAt != null && endedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("Окончание паузы не может быть раньше её начала");
        }
    }
}
