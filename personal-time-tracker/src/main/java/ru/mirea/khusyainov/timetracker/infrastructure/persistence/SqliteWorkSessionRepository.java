package ru.mirea.khusyainov.timetracker.infrastructure.persistence;

import ru.mirea.khusyainov.timetracker.domain.model.PauseRecord;
import ru.mirea.khusyainov.timetracker.domain.model.WorkSession;
import ru.mirea.khusyainov.timetracker.domain.repository.WorkSessionRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SqliteWorkSessionRepository implements WorkSessionRepository {
    private final SqliteDatabase database;

    public SqliteWorkSessionRepository(SqliteDatabase database) {
        this.database = database;
    }

    @Override
    public void save(WorkSession session) {
        String sessionSql = "INSERT INTO work_sessions(id, task_id, started_at, ended_at, active_seconds) VALUES(?, ?, ?, ?, ?)";
        String pauseSql = "INSERT INTO pause_records(id, session_id, started_at, ended_at, reason) VALUES(?, ?, ?, ?, ?)";
        try (Connection connection = database.openConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(sessionSql)) {
                    statement.setString(1, session.id().toString());
                    statement.setString(2, session.taskId().toString());
                    statement.setString(3, session.startedAt().toString());
                    statement.setString(4, session.endedAt().toString());
                    statement.setLong(5, session.activeSeconds());
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(pauseSql)) {
                    for (PauseRecord pause : session.pauses()) {
                        statement.setString(1, pause.id().toString());
                        statement.setString(2, session.id().toString());
                        statement.setString(3, pause.startedAt().toString());
                        statement.setString(4, pause.endedAt().toString());
                        statement.setString(5, pause.reason());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Не удалось сохранить сеанс и его паузы", exception);
        }
    }

    @Override
    public List<WorkSession> findAll() {
        List<WorkSession> sessions = new ArrayList<>();
        String sql = "SELECT id, task_id, started_at, ended_at, active_seconds FROM work_sessions ORDER BY started_at DESC";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                UUID sessionId = UUID.fromString(result.getString("id"));
                sessions.add(new WorkSession(sessionId, UUID.fromString(result.getString("task_id")),
                        java.time.Instant.parse(result.getString("started_at")),
                        java.time.Instant.parse(result.getString("ended_at")), result.getLong("active_seconds"),
                        findPauses(connection, sessionId)));
            }
            return sessions;
        } catch (SQLException exception) {
            throw new IllegalStateException("Не удалось прочитать историю сеансов", exception);
        }
    }

    private List<PauseRecord> findPauses(Connection connection, UUID sessionId) throws SQLException {
        List<PauseRecord> pauses = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, started_at, ended_at, reason FROM pause_records WHERE session_id=? ORDER BY started_at")) {
            statement.setString(1, sessionId.toString());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    pauses.add(new PauseRecord(UUID.fromString(result.getString("id")),
                            java.time.Instant.parse(result.getString("started_at")),
                            java.time.Instant.parse(result.getString("ended_at")), result.getString("reason")));
                }
            }
        }
        return pauses;
    }
}
