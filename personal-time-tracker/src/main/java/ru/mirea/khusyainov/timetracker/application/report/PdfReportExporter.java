package ru.mirea.khusyainov.timetracker.application.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/** Экспортирует снимок отчёта в PDF с встроенным шрифтом, поддерживающим кириллицу. */
final class PdfReportExporter implements ReportExporter {
    @Override
    public void export(ReportSnapshot report, Path destination) throws IOException {
        Path parent = destination.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);

        try (OutputStream output = Files.newOutputStream(destination)) {
            Document document = new Document();
            PdfWriter.getInstance(document, output);
            document.open();
            Font font = createCyrillicFont();
            document.add(new Paragraph(report.title(), font));
            document.add(new Paragraph("Дата формирования: " + report.generatedAt(), font));
            document.add(new Paragraph("Плановое время, сек.: " + report.plannedSeconds(), font));
            document.add(new Paragraph("Фактическое время, сек.: " + report.actualSeconds(), font));
            document.add(new Paragraph("Количество пауз: " + report.pauseCount(), font));
            document.add(new Paragraph("Время пауз, сек.: " + report.pausedSeconds(), font));
            document.close();
        } catch (DocumentException exception) {
            throw new IOException("Не удалось сформировать PDF-отчёт", exception);
        }
    }

    private Font createCyrillicFont() throws IOException, DocumentException {
        // Шрифт поставляется вместе с приложением: экспорт не зависит от набора
        // системных шрифтов Windows, Linux или macOS.
        try (InputStream input = getClass().getResourceAsStream("/fonts/DejaVuSans.ttf")) {
            if (input == null) {
                throw new IOException("В JAR отсутствует встроенный шрифт DejaVuSans.ttf");
            }
            byte[] fontBytes = input.readAllBytes();
            BaseFont baseFont = BaseFont.createFont(
                    "DejaVuSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    true, fontBytes, null);
            return new Font(baseFont, 11);
        }
    }
}
