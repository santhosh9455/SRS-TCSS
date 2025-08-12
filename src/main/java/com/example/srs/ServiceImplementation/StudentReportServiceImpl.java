package com.example.srs.ServiceImplementation;

import com.example.srs.DTO.studentReportDto;
import com.example.srs.Enum.StatusEnum;
import com.example.srs.Model.DepartmentEntity;
import com.example.srs.Model.StudentEntity;
import com.example.srs.Model.SubjectEntity;
import com.example.srs.Repository.DepartmentRepo;
import com.example.srs.Repository.StudentRepo;
import com.example.srs.Service.StudentReportService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentReportServiceImpl implements StudentReportService {

    @Autowired
    private StudentRepo studentRepo;

    @Autowired
    private DepartmentRepo dept;

    @Override
    public void exportToPdf(HttpServletResponse response, String search, Long departmentId, String status, Pageable pageable) throws Exception {

        // 1. Convert status string to enum
        StatusEnum statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = StatusEnum.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status value: " + status);
            }
        }

        // 2. Fetch filtered students
        Page<StudentEntity> students = studentRepo.findFiltered(search, departmentId, statusEnum, pageable);

        // 3. Build filter text to display
        StringBuilder filterText = new StringBuilder();
        if (search != null && !search.isBlank()) {
            filterText.append("Search: ").append(search).append(" | ");
        }
        if (departmentId != null) {
            Optional<DepartmentEntity> dep = dept.findById(departmentId);
            dep.ifPresent(department -> filterText.append("Department: ").append(department.getDepartmentName()).append(" | "));
        }
        if (statusEnum != null) {
            filterText.append("Status: ").append(statusEnum.name());
        }

        // 4. Set response headers
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=students_report.pdf");

        // 5. Setup PDF writer
        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4.rotate());
        document.setMargins(20, 20, 20, 20);

        // 6. Add title
        document.add(new Paragraph("Student Report")
                .setBold()
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(8));

        // 7. Add filters as subtitle if available
        if (filterText.length() > 0) {
            document.add(new Paragraph("Filters: " + filterText.toString())
                    .setFontSize(11)
                    .setItalic()
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(10));
        }

        // 8. Create table with column widths
        Table table = new Table(UnitValue.createPercentArray(new float[]{
                1.2f, 2.5f, 3.5f, 2.5f, 2.5f, 1.2f, 2.5f, 2.5f, 2.5f
        })).useAllAvailableWidth();

        // 9. Define header columns
        String[] headers = {
                "ID", "Name", "Email", "Department", "Phone",
                "Age", "Course Status", "Student Status", "Username"
        };

        // 10. Add header cells
        for (String header : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(header))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setPadding(5));
        }

        // 11. Fill rows from student list
        for (StudentEntity s : students) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(s.getId()))));
            table.addCell(new Cell().add(new Paragraph(s.getName())));
            table.addCell(new Cell().add(new Paragraph(s.getEmail())));
            table.addCell(new Cell().add(new Paragraph(s.getDepartment().getDepartmentName())));
            table.addCell(new Cell().add(new Paragraph(s.getPhoneNumber())));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(s.getAge()))));
            table.addCell(new Cell().add(new Paragraph(s.getCourseStatus().toString())));
            table.addCell(new Cell().add(new Paragraph(s.getStatus().toString())));
            table.addCell(new Cell().add(new Paragraph(s.getUser() != null ? s.getUser().getUsername() : "N/A")));
        }

        // 12. Add table to PDF
        document.add(table);

        // 13. Close document
        document.close();
    }

    @Override
    public void exportToExcel(HttpServletResponse response, String search, Long departmentId, String status, Pageable pageable) throws IOException {
        StatusEnum statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = StatusEnum.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status value: " + status);
            }
        }

        Page<StudentEntity> students = studentRepo.findFiltered(search, departmentId, statusEnum, pageable);

        List<studentReportDto> dtos = students.stream().map(student -> {
            studentReportDto dto = new studentReportDto();
            dto.setName(student.getName());
            dto.setEmail(student.getEmail());
            dto.setDepartmentName(student.getDepartment().getDepartmentName());
            dto.setAge(student.getAge());
            dto.setGender(student.getGender());
            dto.setPhoneNumber(student.getPhoneNumber());
            dto.setCourseId(student.getCourse() != null ? student.getCourse().getId() : null);
            dto.setUsername(student.getUser() != null ? student.getUser().getUsername() : "N/A");
            dto.setCourseName((student.getCourse() != null ? student.getCourse().getCourseName() : "N/A"));
            dto.setSubjectNames(student.getSubjects() != null
                    ? student.getSubjects().stream()
                    .map(SubjectEntity::getSubjectName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", "))
                    : "N/A");
            dto.setStatus(student.getStatus().toString());
            dto.setCourseStatus(student.getCourseStatus().toString());
            return dto;
        }).toList();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Students");

        // Header Row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Name");
        header.createCell(1).setCellValue("Email");
        header.createCell(2).setCellValue("Department");
        header.createCell(3).setCellValue("Phone");
        header.createCell(4).setCellValue("Age");
        header.createCell(5).setCellValue("Gender");
        header.createCell(6).setCellValue("Username");
        header.createCell(7).setCellValue("Course Name");
        header.createCell(8).setCellValue("Subject Names");
        header.createCell(9).setCellValue("Student Status");
        header.createCell(10).setCellValue("Course Status");

        int rowIndex = 1;
        for (studentReportDto dto : dtos) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(dto.getName());
            row.createCell(1).setCellValue(dto.getEmail());
            row.createCell(2).setCellValue(dto.getDepartmentName());
            row.createCell(3).setCellValue(dto.getPhoneNumber());
            row.createCell(4).setCellValue(dto.getAge() != null ? dto.getAge() : 0);
            row.createCell(5).setCellValue(dto.getGender() != null ? dto.getGender() : "N/A");
            row.createCell(6).setCellValue(dto.getUsername());
            row.createCell(7).setCellValue(dto.getCourseName());
            row.createCell(8).setCellValue(dto.getSubjectNames());
            row.createCell(9).setCellValue(dto.getStatus());
            row.createCell(10).setCellValue(dto.getCourseStatus());
        }

        // Auto-size columns for better appearance
        for (int i = 0; i <= 10; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=students_Report.xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @Override
    public void exportToCsv(HttpServletResponse response, String search, Long departmentId, String status, Pageable pageable) throws IOException {
        StatusEnum statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = StatusEnum.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status value: " + status);
            }
        }

        Page<StudentEntity> students = studentRepo.findFiltered(search, departmentId, statusEnum, pageable);

        List<studentReportDto> dtos = students.stream().map(student -> {
            studentReportDto dto = new studentReportDto();
            dto.setName(student.getName());
            dto.setEmail(student.getEmail());
            dto.setDepartmentName(student.getDepartment().getDepartmentName());
            dto.setAge(student.getAge());
            dto.setGender(student.getGender());
            dto.setPhoneNumber(student.getPhoneNumber());
            dto.setCourseId(student.getCourse() != null ? student.getCourse().getId() : null);
            dto.setUsername(student.getUser() != null ? student.getUser().getUsername() : "N/A");
            dto.setCourseName((student.getCourse() != null ? student.getCourse().getCourseName() : "N/A"));

            // This is the fix — flattening the list into a string
            dto.setSubjectNames(student.getSubjects() != null
                    ? student.getSubjects().stream()
                    .map(SubjectEntity::getSubjectName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", "))
                    : "N/A");

            return dto;
        }).toList();

        try {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=students_Report.csv");

            StatefulBeanToCsv<studentReportDto> writer = new StatefulBeanToCsvBuilder<studentReportDto>(response.getWriter())
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build();

            writer.write(dtos);
        } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            e.printStackTrace();
            throw new RuntimeException("Error writing CSV: " + e.getMessage());
        }
    }


}

