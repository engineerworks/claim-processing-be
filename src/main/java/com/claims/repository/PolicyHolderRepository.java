package com.claims.repository;

import com.claims.model.PolicyHolder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Repository
public class PolicyHolderRepository {

    private static final String FILE_PATH =
            "data/SamplePlanAndTransactionData_1.xlsx";

    private final Map<String, PolicyHolder> policyHolders =
            new HashMap<>();

    private final DataFormatter formatter =
            new DataFormatter();

    public PolicyHolderRepository() {
        loadPolicyHolders();
    }

    private void loadPolicyHolders() {

        try (InputStream inputStream =
                     getClass()
                             .getClassLoader()
                             .getResourceAsStream(FILE_PATH)) {

            if (inputStream == null) {
                throw new IllegalStateException(
                        "Excel file not found: " + FILE_PATH);
            }

            try (Workbook workbook = new XSSFWorkbook(inputStream)) {

                Sheet sheet = workbook.getSheet("PolicyData");

                if (sheet == null) {throw new IllegalStateException("PolicyData sheet not found");
                }

                /*
                 * Row 0 contains headings.
                 * Start reading from row 1.
                 */
                for (int rowIndex = 0;
                     rowIndex <= sheet.getLastRowNum();
                     rowIndex++) {

                    Row row = sheet.getRow(rowIndex);

                    if (row == null) {
                        continue;
                    }

                    String policyId =
                            readString(row.getCell(0));

                    String policyHolderId =
                            readString(row.getCell(1));

                    /*
                     * Skip empty rows.
                     */
                    if (policyHolderId.isBlank()) {
                        continue;
                    }

                    /*
                     * Skip header row.
                     */
                    if ("PolicyId".equalsIgnoreCase(policyId)
                            || "Policy holder Id".equalsIgnoreCase(
                            policyHolderId)) {
                        continue;
                    }


                    String firstName =
                            readString(row.getCell(2));

                    String lastName =
                            readString(row.getCell(3));

                    String planId =
                            readString(row.getCell(4));

                    LocalDate coverageStartDate =
                            readDate(row.getCell(5));

                    LocalDate coverageEndDate =
                            readDate(row.getCell(6));

                    BigDecimal individualDeductible =
                            readMoney(row.getCell(7));

                    BigDecimal familyDeductible =
                            readMoney(row.getCell(8));

                    PolicyHolder policyHolder =
                            new PolicyHolder();

                    policyHolder.setPolicyId(
                            policyId);

                    policyHolder.setPolicyHolderId(
                            policyHolderId);

                    policyHolder.setMemberName(
                            (firstName + " " + lastName).trim());

                    policyHolder.setPlanId(
                            planId);

                    policyHolder.setCoverageStartDate(
                            coverageStartDate);

                    policyHolder.setCoverageEndDate(
                            coverageEndDate);

                    policyHolder.setIndividualAccumulatedDeductible(
                            individualDeductible);

                    policyHolder.setFamilyAccumulatedDeductible(
                            familyDeductible);

                    policyHolders.put(
                            policyHolderId,
                            policyHolder);
                }
            }

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Unable to read PolicyData",
                    exception);
        }

        System.out.println(
                "Policyholders loaded from Excel: "
                        + policyHolders.size());

        System.out.println(
                "Policyholder IDs: "
                        + policyHolders.keySet());
    }


    public PolicyHolder findByPolicyHolderId(
            String policyHolderId) {

        return policyHolders.get(
                policyHolderId);
    }


    private String readString(Cell cell) {

        if (cell == null) {
            return "";
        }

        return formatter
                .formatCellValue(cell)
                .trim();
    }


    private BigDecimal readMoney(Cell cell) {

        String value =
                readString(cell);

        if (value.isBlank()) {
            return BigDecimal.ZERO;
        }

        value = value
                .replace("$", "")
                .replace(",", "")
                .trim();

        return new BigDecimal(value);
    }

    private LocalDate readDate(Cell cell) {

        if (cell == null) {
            return null;
        }

        // Blank cell means no date.
        if (cell.getCellType()
                == org.apache.poi.ss.usermodel.CellType.BLANK) {

            return null;
        }

        // Excel may store a date internally as a numeric value.
        if (cell.getCellType()
                == org.apache.poi.ss.usermodel.CellType.NUMERIC) {

            if (DateUtil.isCellDateFormatted(cell)) {

                return cell
                        .getDateCellValue()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }

            /*
             * Some Excel files may contain an Excel serial
             * date number without date formatting.
             */
            return cell
                    .getLocalDateTimeCellValue()
                    .toLocalDate();
        }

        // Excel may also store the date as text.
        if (cell.getCellType()
                == org.apache.poi.ss.usermodel.CellType.STRING) {

            String value =
                    cell.getStringCellValue().trim();

            if (value.isBlank()) {
                return null;
            }

            /*
             * Try common date formats used in spreadsheets.
             */
            java.time.format.DateTimeFormatter[] formatters = {
                    java.time.format.DateTimeFormatter.ofPattern(
                            "M/d/yyyy"),
                    java.time.format.DateTimeFormatter.ofPattern(
                            "MM/dd/yyyy"),
                    java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
            };

            for (java.time.format.DateTimeFormatter formatter
                    : formatters) {

                try {

                    return LocalDate.parse(
                            value,
                            formatter);

                } catch (
                        java.time.format.DateTimeParseException ignored) {

                    // Try the next format.
                }
            }

            throw new IllegalStateException(
                    "Unable to parse date: " + value);
        }

        throw new IllegalStateException(
                "Unsupported Excel date cell type: "
                        + cell.getCellType());
    }
}
