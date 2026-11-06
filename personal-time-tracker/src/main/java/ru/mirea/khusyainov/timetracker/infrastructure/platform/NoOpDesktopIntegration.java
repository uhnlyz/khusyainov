package ru.mirea.khusyainov.timetracker.infrastructure.platform;

import java.awt.Window;

/** Безопасный резервный вариант, если конкретная ОС или графическая среда не поддерживает SystemTray. */
public final class NoOpDesktopIntegration implements DesktopIntegration {
    @Override public boolean isAvailable() { return false; }
    @Override public void attach(Window window) { }
    @Override public void showNotification(String title, String message) { }
    @Override public void close() { }
}
