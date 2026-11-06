package ru.mirea.khusyainov.timetracker.domain.repository;

import ru.mirea.khusyainov.timetracker.domain.model.WorkSession;

import java.util.List;

public interface WorkSessionRepository {
    void save(WorkSession session);
    List<WorkSession> findAll();
}
