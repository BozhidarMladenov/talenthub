package com.softuni.talenthub.controller;

import com.lowagie.text.DocumentException;
import com.softuni.talenthub.service.export.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("/export")
@RequiredArgsConstructor
public class BrowserExportController {

    private final ExportService exportService;

    @GetMapping("/jobs/pdf")
    @ResponseBody
    @PreAuthorize("hasAuthority('PERMISSION_EXPORT_DATA')")
    public ResponseEntity<byte[]> exportJobsPdf() throws DocumentException {
        log.info("Browser PDF export requested");
        byte[] pdf = exportService.exportJobsToPdf();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("talenthub-jobs.pdf")
                                .build().toString())
                .body(pdf);
    }

    @GetMapping("/jobs/excel")
    @ResponseBody
    @PreAuthorize("hasAuthority('PERMISSION_EXPORT_DATA')")
    public ResponseEntity<byte[]> exportJobsExcel() throws IOException {
        log.info("Browser Excel export requested");
        byte[] excel = exportService.exportJobsToExcel();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("talenthub-jobs.xlsx")
                                .build().toString())
                .body(excel);
    }
}
