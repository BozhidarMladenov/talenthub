package com.softuni.talenthub.service.export;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.softuni.talenthub.model.entity.JobPost;
import com.softuni.talenthub.service.JobPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final JobPostService jobPostService;

    public byte[] exportJobsToPdf() throws DocumentException {
        log.info("Exporting open job posts to PDF");
        List<JobPost> jobs = jobPostService.findAllOpen();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            document.add(new Paragraph("TalentHub – Open Job Posts", titleFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 2f, 1.5f, 1.5f});

            addPdfHeaderCell(table, "Title", headerFont);
            addPdfHeaderCell(table, "Category", headerFont);
            addPdfHeaderCell(table, "Budget ($)", headerFont);
            addPdfHeaderCell(table, "Client", headerFont);

            for (JobPost job : jobs) {
                table.addCell(new PdfPCell(new Phrase(job.getTitle(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(job.getCategory().name(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(job.getBudget().toPlainString(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(job.getClient().getUsername(), cellFont)));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (IOException e) {
            throw new DocumentException("Failed to write PDF: " + e.getMessage());
        }
    }

    public byte[] exportJobsToExcel() throws IOException {
        log.info("Exporting open job posts to Excel");
        List<JobPost> jobs = jobPostService.findAllOpen();

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Open Jobs");

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] columns = {"Title", "Category", "Budget (USD)", "Client", "Status"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (JobPost job : jobs) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(job.getTitle());
                row.createCell(1).setCellValue(job.getCategory().name());
                row.createCell(2).setCellValue(job.getBudget().doubleValue());
                row.createCell(3).setCellValue(job.getClient().getUsername());
                row.createCell(4).setCellValue(job.getStatus().name());
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void addPdfHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(Color.DARK_GRAY);
        cell.setPadding(6);
        table.addCell(cell);
    }
}
