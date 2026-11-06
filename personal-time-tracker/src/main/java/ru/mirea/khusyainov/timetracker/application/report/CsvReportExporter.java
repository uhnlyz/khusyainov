package ru.mirea.khusyainov.timetracker.application.report;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class CsvReportExporter implements ReportExporter {
    @Override
    public void export(ReportSnapshot report, Path destination) throws IOException {
        List<String> rows = List.of(
                "Показатель;Значение",
                "Название;" + escape(report.title()),
                "Дата формирования;" + report.generatedAt(),
                "Плановое время, сек.;" + report.plannedSeconds(),
                "Фактическое время, сек.;" + report.actualSeconds(),
                "Количество пауз;" + report.pauseCount(),
                "Время пауз, сек.;" + report.pausedSeconds());
        Path parent = destination.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        Files.writeString(destination, "\uFEFF" + String.join(System.lineSeparator(), rows), StandardCharsets.UTF_8);
    }

    private String escape(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
