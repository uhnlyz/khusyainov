package ru.mirea.khusyainov.timetracker.infrastructure.persistence;

import ru.mirea.khusyainov.timetracker.domain.model.Category;
import ru.mirea.khusyainov.timetracker.domain.repository.CategoryRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class SqliteCategoryRepository implements CategoryRepository {
    private final SqliteDatabase database;

    public SqliteCategoryRepository(SqliteDatabase database) {
        this.database = database;
    }

    @Override
    public void save(Category category) {
        String sql = "INSERT INTO categories(id, name, created_at) VALUES(?, ?, ?) " +
                "ON CONFLICT(id) DO UPDATE SET name=excluded.name, created_at=excluded.created_at";
        try (Connection connection = database.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, category.id().toString());
            statement.setString(2, category.name());
            statement.setString(3, category.createdAt().toString());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Не удалось сохранить категорию", exception);
        }
    }

    @Override
    public Optional<Category> findByName(String name) {
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id, name, created_at FROM categories WHERE lower(name)=lower(?)")) {
            statement.setString(1, name.trim());
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Не удалось найти категорию", exception);
        }
    }

    @Override
    public Optional<Category> findById(UUID id) {
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id, name, created_at FROM categories WHERE id=?")) {
            statement.setString(1, id.toString());
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Не удалось получить категорию", exception);
        }
    }

    @Override
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id, name, created_at FROM categories ORDER BY name");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) categories.add(map(result));
            return categories;
        } catch (SQLException exception) {
            throw new IllegalStateException("Не удалось получить список категорий", exception);
        }
    }

    private Category map(ResultSet result) throws SQLException {
        return new Category(UUID.fromString(result.getString("id")), result.getString("name"),
                java.time.Instant.parse(result.getString("created_at")));
    }
}
