package com.example.srs.Service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

public interface StudentReportService {
    void exportToPdf(HttpServletResponse response, String search, Long departmentId, String status, Pageable pageable) throws Exception;
    void exportToExcel(HttpServletResponse response, String search, Long departmentId, String status, Pageable pageable) throws IOException;
    void exportToCsv(HttpServletResponse response, String search, Long departmentId, String status, Pageable pageable) throws IOException;
}
