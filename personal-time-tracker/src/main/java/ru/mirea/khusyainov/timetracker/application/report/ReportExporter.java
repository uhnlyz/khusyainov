package ru.mirea.khusyainov.timetracker.application.report;

import java.io.IOException;
import java.nio.file.Path;

public interface ReportExporter {
    void export(ReportSnapshot report, Path destination) throws IOException;
}
