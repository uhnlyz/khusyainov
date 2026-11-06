package ru.mirea.khusyainov.timetracker.application.report;

import java.io.IOException;
import java.nio.file.Path;

/** Creator из паттерна Factory Method. Конкретная фабрика определяет тип экспортёра. */
public abstract class ReportExporterFactory {
    protected abstract ReportExporter createExporter();

    public final void export(ReportSnapshot report, Path destination) throws IOException {
        createExporter().export(report, destination);
    }
}
