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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/jobs/pdf")
    @PreAuthorize("hasAuthority('PERMISSION_EXPORT_DATA')")
    public ResponseEntity<byte[]> exportJobsPdf() throws DocumentException {
        log.info("PDF export requested");
        byte[] pdf = exportService.exportJobsToPdf();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("talenthub-jobs.pdf").build().toString())
                .body(pdf);
    }

    @GetMapping("/jobs/excel")
    @PreAuthorize("hasAuthority('PERMISSION_EXPORT_DATA')")
    public ResponseEntity<byte[]> exportJobsExcel() throws IOException {
        log.info("Excel export requested");
        byte[] excel = exportService.exportJobsToExcel();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("talenthub-jobs.xlsx").build().toString())
                .body(excel);
    }
}
