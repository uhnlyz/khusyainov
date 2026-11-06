package ru.mirea.khusyainov.timetracker.presentation;

import ru.mirea.khusyainov.timetracker.application.task.TaskService;
import ru.mirea.khusyainov.timetracker.application.timer.TimerObserver;
import ru.mirea.khusyainov.timetracker.application.timer.TimerService;
import ru.mirea.khusyainov.timetracker.application.timer.TimerSnapshot;
import ru.mirea.khusyainov.timetracker.domain.model.Task;
import ru.mirea.khusyainov.timetracker.domain.model.TimerStatus;
import ru.mirea.khusyainov.timetracker.infrastructure.platform.DesktopIntegration;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Duration;
import java.util.List;

public final class MainWindow extends JFrame implements TimerObserver {
    private final TaskService taskService;
    private final TimerService timerService;
    private final DesktopIntegration desktopIntegration;
    private final JComboBox<Task> taskCombo = new JComboBox<>();
    private final JLabel timerLabel = new JLabel("00:00:00", SwingConstants.CENTER);
    private final JLabel statusLabel = new JLabel("Нет активного сеанса", SwingConstants.CENTER);
    private final MiniTimerWindow miniWindow = new MiniTimerWindow();

    public MainWindow(TaskService taskService, TimerService timerService, DesktopIntegration desktopIntegration) {
        super("Персональный тайм-трекер");
        this.taskService = taskService;
        this.timerService = timerService;
        this.desktopIntegration = desktopIntegration;
        buildInterface();
        refreshTasks();
        timerService.addObserver(this);
        timerService.addObserver(miniWindow);
        desktopIntegration.attach(this);
        setLocationRelativeTo(null);
    }

    private void buildInterface() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(560, 300);
        setLayout(new BorderLayout(10, 10));

        JPanel taskPanel = new JPanel(new BorderLayout(8, 8));
        taskPanel.setBorder(BorderFactory.createTitledBorder("Задача"));
        taskPanel.add(taskCombo, BorderLayout.CENTER);
        JButton addTaskButton = new JButton("Создать задачу");
        addTaskButton.addActionListener(event -> createTask());
        taskPanel.add(addTaskButton, BorderLayout.EAST);

        JPanel timerPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        timerLabel.setFont(timerLabel.getFont().deriveFont(34f));
        statusLabel.setFont(statusLabel.getFont().deriveFont(13f));
        timerPanel.add(timerLabel);
        timerPanel.add(statusLabel);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        JButton start = new JButton("Старт");
        JButton pause = new JButton("Пауза");
        JButton resume = new JButton("Продолжить");
        JButton stop = new JButton("Стоп");
        JButton mini = new JButton("Мини-панель");
        start.addActionListener(event -> runSafely(this::startTimer));
        pause.addActionListener(event -> runSafely(this::pauseTimer));
        resume.addActionListener(event -> runSafely(timerService::resume));
        stop.addActionListener(event -> runSafely(timerService::stop));
        mini.addActionListener(event -> miniWindow.setVisible(!miniWindow.isVisible()));
        controls.add(start);
        controls.add(pause);
        controls.add(resume);
        controls.add(stop);
        controls.add(mini);

        add(taskPanel, BorderLayout.NORTH);
        add(timerPanel, BorderLayout.CENTER);
        add(controls, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent event) {
                if (desktopIntegration.isAvailable()) {
                    setVisible(false);
                    desktopIntegration.showNotification("Тайм-трекер", "Приложение продолжает работать в фоне.");
                } else {
                    shutdown();
                }
            }
        });
    }

    private void createTask() {
        String title = JOptionPane.showInputDialog(this, "Название задачи:", "Создание задачи", JOptionPane.QUESTION_MESSAGE);
        if (title == null) return;
        String category = JOptionPane.showInputDialog(this, "Категория:", "Учёба");
        if (category == null) return;
        String minutesText = JOptionPane.showInputDialog(this, "Плановая длительность, минут:", "60");
        if (minutesText == null) return;
        runSafely(() -> {
            long minutes = Long.parseLong(minutesText.trim());
            taskService.createTask(title, category, Duration.ofMinutes(minutes));
            refreshTasks();
        });
    }

    private void startTimer() {
        Task selected = (Task) taskCombo.getSelectedItem();
        timerService.start(selected);
        desktopIntegration.showNotification("Таймер запущен", selected.title());
    }

    private void pauseTimer() {
        String reason = JOptionPane.showInputDialog(this, "Укажите причину прерывания:", "Пауза", JOptionPane.QUESTION_MESSAGE);
        if (reason != null) timerService.pause(reason);
    }

    private void refreshTasks() {
        List<Task> tasks = taskService.listTasks();
        taskCombo.removeAllItems();
        tasks.forEach(taskCombo::addItem);
    }

    private void runSafely(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Не удалось выполнить операцию", JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public void onTimerUpdated(TimerSnapshot snapshot) {
        SwingUtilities.invokeLater(() -> {
            timerLabel.setText(format(snapshot.elapsedSeconds()));
            String status = switch (snapshot.status()) {
                case IDLE -> "Нет активного сеанса";
                case RUNNING -> "Выполняется: " + snapshot.taskTitle();
                case PAUSED -> "Пауза: " + snapshot.taskTitle();
                case FINISHED -> "Завершено: " + snapshot.taskTitle();
            };
            statusLabel.setText(status);
            if (snapshot.status() == TimerStatus.RUNNING && snapshot.plannedSeconds() > 0
                    && snapshot.elapsedSeconds() >= snapshot.plannedSeconds()) {
                statusLabel.setText(status + " — плановое время превышено");
            }
        });
    }

    private String format(long seconds) {
        return "%02d:%02d:%02d".formatted(seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }

    private void shutdown() {
        timerService.close();
        desktopIntegration.close();
        miniWindow.dispose();
        dispose();
    }
}
