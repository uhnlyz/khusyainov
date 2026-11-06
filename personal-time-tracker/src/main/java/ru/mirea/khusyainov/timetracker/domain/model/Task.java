package ru.mirea.khusyainov.timetracker.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Task(
        UUID id,
        String title,
        UUID categoryId,
        long plannedSeconds,
        boolean archived,
        Instant createdAt) {

    public Task {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(categoryId, "categoryId");
        Objects.requireNonNull(createdAt, "createdAt");
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Название задачи не может быть пустым");
        }
        if (plannedSeconds <= 0) {
            throw new IllegalArgumentException("Плановая длительность должна быть больше нуля");
        }
        title = title.trim();
    }

    @Override
    public String toString() {
        return title;
    }
}
