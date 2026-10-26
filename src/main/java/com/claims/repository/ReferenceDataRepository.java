package com.claims.repository;

import com.claims.model.CoverageRule;
import com.claims.model.Plan;
import com.claims.model.PolicyHolder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ReferenceDataRepository {

    private static final String WORKBOOK_PATH =
            "data/SamplePlanAndTransactionData_1.xlsx";

    private final Map<String, PolicyHolder> policyHoldersById =
            new HashMap<>();

    private final Map<String, Plan> plansById =
            new HashMap<>();

    private final Map<String, CoverageRule> coverageRulesByKey =
            new HashMap<>();

    private final DataFormatter dataFormatter =
            new DataFormatter();

    public ReferenceDataRepository() {
        loadReferenceData();
    }

    private void loadReferenceData() {

        try (InputStream inputStream =
                     getClass()
                             .getClassLoader()
                             .getResourceAsStream(WORKBOOK_PATH)) {

            if (inputStream == null) {
                throw new IllegalStateException(
                        "Excel workbook was not found at: "
                                + WORKBOOK_PATH);
            }

            try (Workbook workbook =
                         new XSSFWorkbook(inputStream)) {

                loadPlans(workbook);
                loadPolicyHolders(workbook);
                loadCoverageRules(workbook);
            }

            System.out.println(
                    "Reference data loaded successfully.");

            System.out.println(
                    "Policyholders loaded: "
                            + policyHoldersById.size());

            System.out.println(
                    "Plans loaded: "
                            + plansById.size());

            System.out.println(
                    "Coverage rules loaded: "
                            + coverageRulesByKey.size());

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to read the Excel workbook.",
                    exception);
        }
    }

    private void loadPlans(Workbook workbook) {

        Sheet sheet =
                workbook.getSheet("PlanDescriptions");

        validateSheet(sheet, "PlanDescriptions");

        for (int rowIndex = 1;
             rowIndex <= sheet.getLastRowNum();
             rowIndex++) {

            Row row = sheet.getRow(rowIndex);

            if (isRowEmpty(row)) {
                continue;
            }

            String planId =
                    readString(row.getCell(0));

            if (isBlank(planId)) {
                continue;
            }

            Plan plan = new Plan();

            plan.setPlanId(planId);

            plan.setIndividualDeductible(
                    readBigDecimal(row.getCell(4)));

            plan.setFamilyDeductible(
                    readBigDecimal(row.getCell(5)));

            plansById.put(
                    normalize(planId),
                    plan);
        }
    }

    private void loadPolicyHolders(Workbook workbook) {

        Sheet sheet =
                workbook.getSheet("PolicyData");

        validateSheet(sheet, "PolicyData");

        for (int rowIndex = 1;
             rowIndex <= sheet.getLastRowNum();
             rowIndex++) {

            Row row = sheet.getRow(rowIndex);

            if (isRowEmpty(row)) {
                continue;
            }

            String policyHolderId =
                    readString(row.getCell(1));

            if (isBlank(policyHolderId)) {
                continue;
            }

            String firstName =
                    readString(row.getCell(2));

            String lastName =
                    readString(row.getCell(3));

            PolicyHolder policyHolder =
                    new PolicyHolder();

            policyHolder.setPolicyId(
                    readString(row.getCell(0)));

            policyHolder.setPolicyHolderId(
                    policyHolderId);

            policyHolder.setMemberName(
                    buildMemberName(firstName, lastName));

            policyHolder.setPlanId(
                    readString(row.getCell(4)));

            policyHolder.setCoverageStartDate(
                    readDate(row.getCell(5)));

            policyHolder.setCoverageEndDate(
                    readDate(row.getCell(6)));

            policyHolder.setIndividualAccumulatedDeductible(
                    readBigDecimalOrZero(row.getCell(7)));

            policyHolder.setFamilyAccumulatedDeductible(
                    readBigDecimalOrZero(row.getCell(8)));

            policyHoldersById.put(
                    normalize(policyHolderId),
                    policyHolder);
        }
    }

    private void loadCoverageRules(Workbook workbook) {

        Sheet sheet =
                workbook.getSheet("PlanCoverage");

        validateSheet(sheet, "PlanCoverage");

        for (int rowIndex = 1;
             rowIndex <= sheet.getLastRowNum();
             rowIndex++) {

            Row row = sheet.getRow(rowIndex);

            if (isRowEmpty(row)) {
                continue;
            }

            String mainCategory =
                    readString(row.getCell(0));

            String subCategoryCell =
                    readString(row.getCell(1));

            if (isBlank(mainCategory)
                    || isBlank(subCategoryCell)) {
                continue;
            }

            /*
             * PlanCoverage stores the three plans in
             * separate columns:
             *
             * Column 2 = P001
             * Column 3 = P002
             * Column 4 = P003
             */
            createCoverageRules(
                    mainCategory,
                    subCategoryCell,
                    "P001",
                    readString(row.getCell(2)));

            createCoverageRules(
                    mainCategory,
                    subCategoryCell,
                    "P002",
                    readString(row.getCell(3)));

            createCoverageRules(
                    mainCategory,
                    subCategoryCell,
                    "P003",
                    readString(row.getCell(4)));
        }
    }

    private void createCoverageRules(
            String mainCategory,
            String subCategoryCell,
            String planId,
            String ruleText) {

        if (isBlank(ruleText)) {
            return;
        }

        /*
         * Inpatient Hospital Care has several
         * sub-services in one Excel cell.
         *
         * We create an individual lookup rule for each
         * sub-service, such as ROOM AND BOARD and SURGERY.
         */
        String[] subCategories =
                subCategoryCell.split(",");

        for (String subCategory : subCategories) {

            CoverageRule coverageRule =
                    createCoverageRule(
                            planId,
                            mainCategory,
                            subCategory.trim(),
                            ruleText);

            String key =
                    buildCoverageKey(
                            planId,
                            mainCategory,
                            subCategory);

            coverageRulesByKey.put(
                    key,
                    coverageRule);
        }
    }

    private CoverageRule createCoverageRule(
            String planId,
            String mainCategory,
            String subCategory,
            String ruleText) {

        CoverageRule coverageRule =
                new CoverageRule();

        coverageRule.setPlanId(planId);
        coverageRule.setMainCategory(mainCategory);
        coverageRule.setSubCategory(subCategory);

        String normalizedRule =
                ruleText.trim().toUpperCase();

        if (normalizedRule.equals("NO CHARGE")) {

            coverageRule.setRuleType(
                    "NO_CHARGE");

            coverageRule.setCoverageValue(null);

        } else if (normalizedRule.contains("%")) {

            coverageRule.setRuleType(
                    "PERCENTAGE_AFTER_DEDUCTIBLE");

            String percentageText =
                    normalizedRule.substring(
                            0,
                            normalizedRule.indexOf("%"));

            coverageRule.setCoverageValue(
                    new BigDecimal(
                            percentageText.trim()));

        } else {

            coverageRule.setRuleType(
                    "FLAT_DOLLAR");

            String amountText =
                    normalizedRule
                            .replace("$", "")
                            .replace("FLAT", "")
                            .trim();

            coverageRule.setCoverageValue(
                    new BigDecimal(amountText));
        }

        return coverageRule;
    }

    public Optional<PolicyHolder> findPolicyHolder(
            String policyHolderId) {

        return Optional.ofNullable(
                policyHoldersById.get(
                        normalize(policyHolderId)));
    }

    public Optional<Plan> findPlan(
            String planId) {

        return Optional.ofNullable(
                plansById.get(
                        normalize(planId)));
    }

    public Optional<CoverageRule> findCoverageRule(
            String planId,
            String mainCategory,
            String subCategory) {

        String key =
                buildCoverageKey(
                        planId,
                        mainCategory,
                        subCategory);

        return Optional.ofNullable(
                coverageRulesByKey.get(key));
    }

    private String buildCoverageKey(
            String planId,
            String mainCategory,
            String subCategory) {

        return normalize(planId)
                + "|"
                + normalize(mainCategory)
                + "|"
                + normalize(subCategory);
    }

    private String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replaceAll("\\s+", " ")
                .toUpperCase();
    }

    private String readString(Cell cell) {

        if (cell == null) {
            return "";
        }

        return dataFormatter
                .formatCellValue(cell)
                .trim();
    }

    private BigDecimal readBigDecimal(Cell cell) {

        String value =
                readString(cell);

        if (isBlank(value)) {
            throw new IllegalStateException(
                    "A required numeric value is missing.");
        }

        return new BigDecimal(
                value.replace(",", ""));
    }

    private BigDecimal readBigDecimalOrZero(
            Cell cell) {

        String value =
                readString(cell);

        if (isBlank(value)) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(
                value.replace(",", ""));
    }

    private LocalDate readDate(Cell cell) {

        if (cell == null
                || cell.getCellType()
                == CellType.BLANK) {

            return null;
        }

        if (cell.getCellType()
                == CellType.NUMERIC
                && DateUtil.isCellDateFormatted(cell)) {

            return cell
                    .getDateCellValue()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        String value =
                readString(cell);

        if (isBlank(value)) {
            return null;
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("MM/dd/yyyy");

        return LocalDate.parse(
                value,
                formatter);
    }

    private String buildMemberName(
            String firstName,
            String lastName) {

        return (firstName + " " + lastName).trim();
    }

    private boolean isRowEmpty(Row row) {

        if (row == null) {
            return true;
        }

        for (int cellIndex = row.getFirstCellNum();
             cellIndex < row.getLastCellNum();
             cellIndex++) {

            if (!isBlank(
                    readString(
                            row.getCell(cellIndex)))) {
                return false;
            }
        }

        return true;
    }

    private boolean isBlank(String value) {

        return value == null
                || value.trim().isEmpty();
    }

    private void validateSheet(
            Sheet sheet,
            String sheetName) {

        if (sheet == null) {
            throw new IllegalStateException(
                    "Required sheet was not found: "
                            + sheetName);
        }
    }
}
