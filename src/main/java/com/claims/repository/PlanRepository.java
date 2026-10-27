package com.claims.repository;

import com.claims.model.Plan;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Repository
public class PlanRepository {

    private static final String FILE_PATH =
            "data/SamplePlanAndTransactionData_1.xlsx";

    private final Map<String, Plan> plans =
            new HashMap<>();

    private final DataFormatter formatter =
            new DataFormatter();

    public PlanRepository() {
        loadPlans();
    }

    private void loadPlans() {

        try (InputStream inputStream =
                     getClass()
                             .getClassLoader()
                             .getResourceAsStream(FILE_PATH)) {

            if (inputStream == null) {

                throw new IllegalStateException(
                        "Excel file not found: "
                                + FILE_PATH);
            }

            try (Workbook workbook =
                         new XSSFWorkbook(inputStream)) {

                Sheet sheet =
                        workbook.getSheet(
                                "PlanDescriptions");

                if (sheet == null) {

                    throw new IllegalStateException(
                            "PlanDescriptions sheet not found");
                }

                for (int rowIndex = 0;
                     rowIndex <= sheet.getLastRowNum();
                     rowIndex++) {

                    Row row =
                            sheet.getRow(rowIndex);

                    if (row == null) {
                        continue;
                    }

                    String planId =
                            readString(row.getCell(0));

                    // Skip empty rows
                    if (planId.isBlank()) {
                        continue;
                    }

                    // Skip header
                    if ("PlanId".equalsIgnoreCase(
                            planId)) {
                        continue;
                    }

                    BigDecimal individualDeductible =
                            readMoney(
                                    row.getCell(4));

                    BigDecimal familyDeductible =
                            readMoney(
                                    row.getCell(5));

                    Plan plan =
                            new Plan();

                    plan.setPlanId(
                            planId);

                    plan.setIndividualDeductible(
                            individualDeductible);

                    plan.setFamilyDeductible(
                            familyDeductible);

                    plans.put(
                            planId,
                            plan);
                }
            }

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Unable to read PlanDescriptions",
                    exception);
        }

        System.out.println(
                "Plans loaded from Excel: "
                        + plans.size());

        System.out.println(
                "Plan IDs: "
                        + plans.keySet());
    }

    public Plan findByPlanId(
            String planId) {

        return plans.get(planId);
    }

    private String readString(
            Cell cell) {

        if (cell == null) {
            return "";
        }

        return formatter
                .formatCellValue(cell)
                .trim();
    }

    private BigDecimal readMoney(
            Cell cell) {

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
}
