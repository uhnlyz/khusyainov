package ru.mirea.khusyainov.timetracker.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Category(UUID id, String name, Instant createdAt) {
    public Category {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(createdAt, "createdAt");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название категории не может быть пустым");
        }
        name = name.trim();
    }
}
