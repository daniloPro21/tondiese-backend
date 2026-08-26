package com.tondise.ecommerce.dao.utils;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PdfReportGenerator {

    public byte[] generate(String title, List<String> headers, List<List<String>> rows) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (Document document = new Document(new PdfDocument(new PdfWriter(out)), PageSize.A4)) {
            document.add(new Paragraph(title).setBold().setFontSize(16));
            document.add(new Paragraph("Généré le " + DateTimeFormatter.ISO_DATE.format(java.time.LocalDate.now()))
                    .setFontSize(9).setFontColor(ColorConstants.GRAY));

            Table table = new Table(UnitValue.createPercentArray(headers.size())).useAllAvailableWidth();
            headers.forEach(header -> table.addHeaderCell(
                    new Cell().add(new Paragraph(header).setBold())
                            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                            .setTextAlignment(TextAlignment.CENTER)));

            rows.forEach(row -> row.forEach(value -> table.addCell(new Cell().add(new Paragraph(value)))));

            document.add(table);
        } catch (Exception e) {
            throw new IllegalStateException("Échec de la génération du PDF", e);
        }

        return out.toByteArray();
    }
}
