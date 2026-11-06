package ru.mirea.khusyainov.timetracker.application.report;

public final class CsvReportExporterFactory extends ReportExporterFactory {
    @Override
    protected ReportExporter createExporter() {
        return new CsvReportExporter();
    }
}
