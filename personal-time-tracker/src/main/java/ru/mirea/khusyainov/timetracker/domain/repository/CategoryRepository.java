package ru.mirea.khusyainov.timetracker.domain.repository;

import ru.mirea.khusyainov.timetracker.domain.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    void save(Category category);
    Optional<Category> findByName(String name);
    Optional<Category> findById(UUID id);
    List<Category> findAll();
}
