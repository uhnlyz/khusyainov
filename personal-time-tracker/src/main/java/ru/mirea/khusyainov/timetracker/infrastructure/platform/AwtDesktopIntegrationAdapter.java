package ru.mirea.khusyainov.timetracker.infrastructure.platform;

import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/** Адаптер стандартного AWT SystemTray к прикладному интерфейсу DesktopIntegration. */
public final class AwtDesktopIntegrationAdapter implements DesktopIntegration {
    private TrayIcon trayIcon;
    private Window mainWindow;

    @Override
    public boolean isAvailable() {
        return trayIcon != null;
    }

    @Override
    public void attach(Window window) {
        this.mainWindow = window;
        if (trayIcon != null || !SystemTray.isSupported()) return;

        BufferedImage iconImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = iconImage.createGraphics();
        try {
            graphics.setColor(new Color(35, 93, 145));
            graphics.fillOval(1, 1, 14, 14);
            graphics.setColor(Color.WHITE);
            graphics.drawLine(8, 3, 8, 8);
            graphics.drawLine(8, 8, 12, 10);
        } finally {
            graphics.dispose();
        }
        Image image = iconImage;
        trayIcon = new TrayIcon(image, "Персональный тайм-трекер");
        trayIcon.setImageAutoSize(true);
        PopupMenu menu = new PopupMenu();
        MenuItem open = new MenuItem("Открыть тайм-трекер");
        open.addActionListener(event -> mainWindow.setVisible(true));
        MenuItem exit = new MenuItem("Завершить работу");
        exit.addActionListener(event -> {
            close();
            mainWindow.dispose();
            System.exit(0);
        });
        menu.add(open);
        menu.addSeparator();
        menu.add(exit);
        trayIcon.setPopupMenu(menu);
        trayIcon.addActionListener(event -> mainWindow.setVisible(true));
        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (java.awt.AWTException exception) {
            trayIcon = null;
        }
    }

    @Override
    public void showNotification(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        }
    }

    @Override
    public void close() {
        if (trayIcon != null && SystemTray.isSupported()) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
        }
    }
}
