package com.example.srs.Components;


import com.example.srs.Model.DepartmentEntity;
import com.example.srs.Model.StudentEntity;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelHelper {

    public static List<StudentEntity> convertExcelToStudents(InputStream is, DepartmentEntity department) {
        List<StudentEntity> students = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // Skip header row
                if (rowNumber++ == 0) {
                    continue;
                }

                StudentEntity student = new StudentEntity();
                DataFormatter formatter = new DataFormatter();

                student.setFirstName(formatter.formatCellValue(currentRow.getCell(0)));

                String ageStr = formatter.formatCellValue(currentRow.getCell(1));
                student.setAge(ageStr.isEmpty() ? 0 : Integer.parseInt(ageStr));

                student.setGender(formatter.formatCellValue(currentRow.getCell(2)));
                student.setEmail(formatter.formatCellValue(currentRow.getCell(3)));
                student.setPhoneNumber(formatter.formatCellValue(currentRow.getCell(4)));

                student.setProfileImagePath(null);
                student.setMarksheetImagePath10th(null);
                student.setDepartment(department);

                students.add(student);
            }

            return students;

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage());
        }
    }
}
