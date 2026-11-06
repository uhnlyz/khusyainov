package ru.mirea.khusyainov.timetracker.infrastructure.platform;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.SystemTray;

public final class DesktopIntegrationFactory {
    private DesktopIntegrationFactory() { }

    public static DesktopIntegration create() {
        try {
            if (!GraphicsEnvironment.isHeadless() && SystemTray.isSupported()) {
                return new AwtDesktopIntegrationAdapter();
            }
        } catch (HeadlessException ignored) {
            // В headless-среде приложение должно работать без системного трея.
        }
        return new NoOpDesktopIntegration();
    }
}
