package ru.mirea.khusyainov.timetracker.infrastructure.platform;

import java.awt.Window;

public interface DesktopIntegration extends AutoCloseable {
    boolean isAvailable();
    void attach(Window window);
    void showNotification(String title, String message);
    @Override void close();
}
