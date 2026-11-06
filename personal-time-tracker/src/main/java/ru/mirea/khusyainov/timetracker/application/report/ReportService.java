package ru.mirea.khusyainov.timetracker.application.report;

import java.io.IOException;
import java.nio.file.Path;

public final class ReportService {
    public void export(ReportSnapshot report, ExportFormat format, Path destination) throws IOException {
        ReportExporterFactory factory = switch (format) {
            case CSV -> new CsvReportExporterFactory();
            case PDF -> new PdfReportExporterFactory();
        };
        factory.export(report, destination);
    }
}
