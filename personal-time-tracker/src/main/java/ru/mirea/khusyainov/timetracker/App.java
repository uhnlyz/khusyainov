package ru.mirea.khusyainov.timetracker;

import ru.mirea.khusyainov.timetracker.application.task.TaskService;
import ru.mirea.khusyainov.timetracker.application.timer.TimerService;
import ru.mirea.khusyainov.timetracker.domain.repository.CategoryRepository;
import ru.mirea.khusyainov.timetracker.domain.repository.TaskRepository;
import ru.mirea.khusyainov.timetracker.domain.repository.WorkSessionRepository;
import ru.mirea.khusyainov.timetracker.infrastructure.persistence.SqliteCategoryRepository;
import ru.mirea.khusyainov.timetracker.infrastructure.persistence.SqliteDatabase;
import ru.mirea.khusyainov.timetracker.infrastructure.persistence.SqliteTaskRepository;
import ru.mirea.khusyainov.timetracker.infrastructure.persistence.SqliteWorkSessionRepository;
import ru.mirea.khusyainov.timetracker.infrastructure.platform.DesktopIntegration;
import ru.mirea.khusyainov.timetracker.infrastructure.platform.DesktopIntegrationFactory;
import ru.mirea.khusyainov.timetracker.presentation.MainWindow;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.nio.file.Path;
import java.time.Clock;

public final class App {
    private App() { }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                Path databasePath = Path.of(System.getProperty("user.home"), ".personal-time-tracker", "tracker.db");
                SqliteDatabase database = new SqliteDatabase(databasePath);

                CategoryRepository categories = new SqliteCategoryRepository(database);
                TaskRepository tasks = new SqliteTaskRepository(database);
                WorkSessionRepository sessions = new SqliteWorkSessionRepository(database);

                TaskService taskService = new TaskService(categories, tasks, Clock.systemDefaultZone());
                TimerService timerService = new TimerService(sessions, Clock.systemDefaultZone());
                DesktopIntegration desktopIntegration = DesktopIntegrationFactory.create();

                new MainWindow(taskService, timerService, desktopIntegration).setVisible(true);
            } catch (Exception exception) {
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Не удалось запустить приложение: " + exception.getMessage(),
                        "Ошибка запуска", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
