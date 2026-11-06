package ru.mirea.khusyainov.timetracker.domain.repository;

import ru.mirea.khusyainov.timetracker.domain.model.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository {
    void save(Task task);
    Optional<Task> findById(UUID id);
    List<Task> findAll();
}
