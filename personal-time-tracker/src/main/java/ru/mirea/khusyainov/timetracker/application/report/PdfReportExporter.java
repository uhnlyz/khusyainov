package ru.mirea.khusyainov.timetracker.application.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

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
        String[] candidates = {
                "C:/Windows/Fonts/arial.ttf",
                "/System/Library/Fonts/Supplemental/Arial.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/liberation2/LiberationSans-Regular.ttf"
        };
        for (String candidate : candidates) {
            if (Files.isRegularFile(Path.of(candidate))) {
                BaseFont baseFont = BaseFont.createFont(candidate, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                return new Font(baseFont, 11);
            }
        }
        BaseFont fallback = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        return new Font(fallback, 11);
    }
}
