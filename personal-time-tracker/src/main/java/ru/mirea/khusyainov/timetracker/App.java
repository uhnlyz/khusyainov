package ru.mirea.khusyainov.timetracker;

import ru.mirea.khusyainov.timetracker.application.analytics.AnalyticsInput;
import ru.mirea.khusyainov.timetracker.application.analytics.FrequentPauseRecommendationStrategy;
import ru.mirea.khusyainov.timetracker.application.analytics.OvertimeRecommendationStrategy;
import ru.mirea.khusyainov.timetracker.application.analytics.RecommendationService;
import ru.mirea.khusyainov.timetracker.application.report.ExportFormat;
import ru.mirea.khusyainov.timetracker.application.report.ReportService;
import ru.mirea.khusyainov.timetracker.application.report.ReportSnapshot;
import ru.mirea.khusyainov.timetracker.application.task.TaskService;
import ru.mirea.khusyainov.timetracker.application.timer.TimerService;
import ru.mirea.khusyainov.timetracker.domain.model.Task;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class App {
    private App() { }

    public static void main(String[] args) {
        if (args.length == 1 && "--self-check".equals(args[0])) {
            runSelfCheck();
            return;
        }

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

    /**
     * Проверяет собранный исполняемый JAR без открытия графического окна.
     * Режим используется CI для проверки запуска, SQLite, таймера, стратегий и экспорта.
     */
    private static void runSelfCheck() {
        Path temporaryDirectory = null;
        try {
            temporaryDirectory = Files.createTempDirectory("personal-time-tracker-self-check-");
            Clock clock = Clock.systemUTC();
            SqliteDatabase database = new SqliteDatabase(temporaryDirectory.resolve("self-check.db"));

            CategoryRepository categories = new SqliteCategoryRepository(database);
            TaskRepository tasks = new SqliteTaskRepository(database);
            WorkSessionRepository sessions = new SqliteWorkSessionRepository(database);
            TaskService taskService = new TaskService(categories, tasks, clock);
            Task task = taskService.createTask("Проверка сборки", "Техническая проверка", Duration.ofMinutes(1));

            TimerService timerService = new TimerService(sessions, clock);
            try {
                timerService.start(task);
                timerService.pause("Проверка сохранения причины прерывания");
                timerService.resume();
                timerService.stop();
            } finally {
                timerService.close();
            }

            var savedSessions = sessions.findAll();
            if (savedSessions.size() != 1 || savedSessions.get(0).pauses().size() != 1) {
                throw new IllegalStateException("Не удалось проверить сохранение сеанса и причины паузы");
            }

            RecommendationService recommendations = new RecommendationService(List.of(
                    new OvertimeRecommendationStrategy(0.2),
                    new FrequentPauseRecommendationStrategy(3)));
            if (recommendations.generate(new AnalyticsInput("Проверка сборки", 60, 90, 3, 15)).size() != 2) {
                throw new IllegalStateException("Не удалось проверить стратегии рекомендаций");
            }

            ReportSnapshot snapshot = new ReportSnapshot("Проверочный отчёт", clock.instant(), 60, 90, 3, 15);
            ReportService reportService = new ReportService();
            Path csv = temporaryDirectory.resolve("self-check.csv");
            Path pdf = temporaryDirectory.resolve("self-check.pdf");
            reportService.export(snapshot, ExportFormat.CSV, csv);
            reportService.export(snapshot, ExportFormat.PDF, pdf);
            if (Files.size(csv) == 0 || Files.size(pdf) == 0) {
                throw new IllegalStateException("Экспорт отчёта создал пустой файл");
            }

            DesktopIntegration desktopIntegration = DesktopIntegrationFactory.create();
            desktopIntegration.close();

            System.out.println("SELF-CHECK SUCCESS: SQLite, Timer/State, Strategy, Adapter и PDF/CSV Factory Method.");
        } catch (Exception exception) {
            throw new IllegalStateException("Самопроверка исполняемого JAR не пройдена", exception);
        } finally {
            if (temporaryDirectory != null) {
                deleteRecursively(temporaryDirectory);
            }
        }
    }

    private static void deleteRecursively(Path directory) {
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // Временные файлы диагностики не должны влиять на результат проверки.
                }
            });
        } catch (IOException ignored) {
            // Каталог создаётся только для самопроверки и удаляется при возможности.
        }
    }
}
