package ru.mirea.khusyainov.timetracker.presentation;

import ru.mirea.khusyainov.timetracker.application.timer.TimerObserver;
import ru.mirea.khusyainov.timetracker.application.timer.TimerSnapshot;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

public final class MiniTimerWindow extends JFrame implements TimerObserver {
    private final JLabel titleLabel = new JLabel("Таймер не запущен", SwingConstants.CENTER);
    private final JLabel timeLabel = new JLabel("00:00:00", SwingConstants.CENTER);

    public MiniTimerWindow() {
        super("Тайм-трекер — мини-панель");
        setLayout(new BorderLayout(4, 4));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 12f));
        timeLabel.setFont(timeLabel.getFont().deriveFont(Font.BOLD, 20f));
        add(titleLabel, BorderLayout.NORTH);
        add(timeLabel, BorderLayout.CENTER);
        setAlwaysOnTop(true);
        setResizable(false);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(new Dimension(250, 90));
        Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setLocation(bounds.x + bounds.width - getWidth() - 16,
                bounds.y + bounds.height - getHeight() - 16);
    }

    @Override
    public void onTimerUpdated(TimerSnapshot snapshot) {
        SwingUtilities.invokeLater(() -> {
            titleLabel.setText(snapshot.taskTitle());
            timeLabel.setText(format(snapshot.elapsedSeconds()));
        });
    }

    private String format(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remaining = seconds % 60;
        return "%02d:%02d:%02d".formatted(hours, minutes, remaining);
    }
}
