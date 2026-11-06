package ru.mirea.khusyainov.timetracker.application.task;

import ru.mirea.khusyainov.timetracker.domain.model.Category;
import ru.mirea.khusyainov.timetracker.domain.model.Task;
import ru.mirea.khusyainov.timetracker.domain.repository.CategoryRepository;
import ru.mirea.khusyainov.timetracker.domain.repository.TaskRepository;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

public final class TaskService {
    private final CategoryRepository categoryRepository;
    private final TaskRepository taskRepository;
    private final Clock clock;

    public TaskService(CategoryRepository categoryRepository, TaskRepository taskRepository, Clock clock) {
        this.categoryRepository = categoryRepository;
        this.taskRepository = taskRepository;
        this.clock = clock;
    }

    public Task createTask(String title, String categoryName, Duration plannedDuration) {
        if (categoryName == null || categoryName.isBlank()) {
            throw new IllegalArgumentException("Укажите категорию задачи");
        }
        if (plannedDuration == null || plannedDuration.isZero() || plannedDuration.isNegative()) {
            throw new IllegalArgumentException("Плановое время должно быть больше нуля");
        }

        String normalizedCategory = categoryName.trim();
        Category category = categoryRepository.findByName(normalizedCategory)
                .orElseGet(() -> {
                    Category created = new Category(UUID.randomUUID(), normalizedCategory, clock.instant());
                    categoryRepository.save(created);
                    return created;
                });

        Task task = new Task(
                UUID.randomUUID(), title, category.id(), plannedDuration.toSeconds(), false, clock.instant());
        taskRepository.save(task);
        return task;
    }

    public List<Task> listTasks() {
        return taskRepository.findAll().stream().filter(task -> !task.archived()).toList();
    }
}
