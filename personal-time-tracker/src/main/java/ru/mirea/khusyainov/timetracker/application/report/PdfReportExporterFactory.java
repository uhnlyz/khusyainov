package ru.mirea.khusyainov.timetracker.application.report;

public final class PdfReportExporterFactory extends ReportExporterFactory {
    @Override
    protected ReportExporter createExporter() {
        return new PdfReportExporter();
    }
}
