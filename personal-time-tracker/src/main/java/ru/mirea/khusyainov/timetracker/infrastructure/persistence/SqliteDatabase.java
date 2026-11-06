package ru.mirea.khusyainov.timetracker.infrastructure.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class SqliteDatabase {
    private final String jdbcUrl;

    public SqliteDatabase(Path databaseFile) {
        try {
            Path absolute = databaseFile.toAbsolutePath();
            Path parent = absolute.getParent();
            if (parent != null) Files.createDirectories(parent);
            this.jdbcUrl = "jdbc:sqlite:" + absolute;
            initialize();
        } catch (IOException | SQLException exception) {
            throw new IllegalStateException("Не удалось подготовить локальное хранилище", exception);
        }
    }

    public Connection openConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 5000");
        }
        return connection;
    }

    private void initialize() throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS categories (
                        id TEXT PRIMARY KEY,
                        name TEXT NOT NULL UNIQUE,
                        created_at TEXT NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS tasks (
                        id TEXT PRIMARY KEY,
                        title TEXT NOT NULL,
                        category_id TEXT NOT NULL REFERENCES categories(id),
                        planned_seconds INTEGER NOT NULL CHECK(planned_seconds > 0),
                        archived INTEGER NOT NULL DEFAULT 0,
                        created_at TEXT NOT NULL
                    )
                    """);
            statement.execute("CREATE INDEX IF NOT EXISTS idx_tasks_category ON tasks(category_id)");
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS work_sessions (
                        id TEXT PRIMARY KEY,
                        task_id TEXT NOT NULL REFERENCES tasks(id),
                        started_at TEXT NOT NULL,
                        ended_at TEXT NOT NULL,
                        active_seconds INTEGER NOT NULL CHECK(active_seconds >= 0)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS pause_records (
                        id TEXT PRIMARY KEY,
                        session_id TEXT NOT NULL REFERENCES work_sessions(id) ON DELETE CASCADE,
                        started_at TEXT NOT NULL,
                        ended_at TEXT NOT NULL,
                        reason TEXT NOT NULL
                    )
                    """);
            statement.execute("CREATE INDEX IF NOT EXISTS idx_sessions_task ON work_sessions(task_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_sessions_started ON work_sessions(started_at)");
        }
    }
}
