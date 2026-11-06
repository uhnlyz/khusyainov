package ru.mirea.khusyainov.timetracker.infrastructure.persistence;

import ru.mirea.khusyainov.timetracker.domain.model.Task;
import ru.mirea.khusyainov.timetracker.domain.repository.TaskRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class SqliteTaskRepository implements TaskRepository {
    private final SqliteDatabase database;

    public SqliteTaskRepository(SqliteDatabase database) {
        this.database = database;
    }

    @Override
    public void save(Task task) {
        String sql = "INSERT INTO tasks(id, title, category_id, planned_seconds, archived, created_at) VALUES(?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(id) DO UPDATE SET title=excluded.title, category_id=excluded.category_id, " +
                "planned_seconds=excluded.planned_seconds, archived=excluded.archived";
        try (Connection connection = database.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, task.id().toString());
            statement.setString(2, task.title());
            statement.setString(3, task.categoryId().toString());
            statement.setLong(4, task.plannedSeconds());
            statement.setInt(5, task.archived() ? 1 : 0);
            statement.setString(6, task.createdAt().toString());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Не удалось сохранить задачу", exception);
        }
    }

    @Override
    public Optional<Task> findById(UUID id) {
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM tasks WHERE id=?")) {
            statement.setString(1, id.toString());
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Не удалось найти задачу", exception);
        }
    }

    @Override
    public List<Task> findAll() {
        List<Task> tasks = new ArrayList<>();
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM tasks ORDER BY created_at DESC");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) tasks.add(map(result));
            return tasks;
        } catch (SQLException exception) {
            throw new IllegalStateException("Не удалось получить список задач", exception);
        }
    }

    private Task map(ResultSet result) throws SQLException {
        return new Task(UUID.fromString(result.getString("id")), result.getString("title"),
                UUID.fromString(result.getString("category_id")), result.getLong("planned_seconds"),
                result.getInt("archived") != 0, java.time.Instant.parse(result.getString("created_at")));
    }
}
