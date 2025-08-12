package com.example.srs.Controller;

import com.example.srs.Service.StudentReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/admin")
public class StudentReportController {

    private final StudentReportService reportService;

    public StudentReportController(StudentReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/report/students/pdf")
    public void downloadPdf(HttpServletResponse response,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) Long departmentId,
                            @RequestParam(required = false) String status,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size)
            throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=students.pdf");
        reportService.exportToPdf(response, search, departmentId, status, PageRequest.of(page, size));
    }

    @GetMapping("/report/students/excel")
    public void downloadExcel(HttpServletResponse response,
                              @RequestParam(required = false) String search,
                              @RequestParam(required = false) Long departmentId,
                              @RequestParam(required = false) String status,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=students.xlsx");
        reportService.exportToExcel(response,search, departmentId, status, PageRequest.of(page, size));
    }

    @GetMapping("/report/students/csv")
    public void downloadCsv(HttpServletResponse response,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) Long departmentId,
                            @RequestParam(required = false) String status,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=students.csv");
        reportService.exportToCsv(response,search, departmentId, status, PageRequest.of(page, size));
    }
}

