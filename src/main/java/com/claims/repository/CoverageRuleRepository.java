package com.claims.repository;

import com.claims.model.CoverageRule;
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
public class CoverageRuleRepository {

    private static final String FILE_PATH =
            "data/SamplePlanAndTransactionData_1.xlsx";

    private final Map<String, CoverageRule> coverageRules =
            new HashMap<>();

    private final DataFormatter formatter =
            new DataFormatter();

    public CoverageRuleRepository() {
        loadCoverageRules();
    }

    private void loadCoverageRules() {

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
                        workbook.getSheet("PlanCoverage");

                if (sheet == null) {

                    throw new IllegalStateException(
                            "PlanCoverage sheet not found");
                }

                String currentMainCategory = "";

                for (int rowIndex = 0;
                     rowIndex <= sheet.getLastRowNum();
                     rowIndex++) {

                    Row row =
                            sheet.getRow(rowIndex);

                    if (row == null) {
                        continue;
                    }

                    String mainCategory =
                            readString(row.getCell(0));

                    String subCategory =
                            readString(row.getCell(1));

                    /*
                     * Skip the heading row.
                     */
                    if ("Main Category"
                            .equalsIgnoreCase(mainCategory)) {

                        continue;
                    }

                    /*
                     * Some rows don't repeat the main
                     * category.
                     *
                     * Example:
                     *
                     * Outpatient Services
                     *     PRIMARY CARE OFFICE VISIT
                     *     SPECIALTY CARE OFFICE VISIT
                     *     X-RAYS
                     *
                     * Therefore remember the last
                     * non-empty main category.
                     */
                    if (!mainCategory.isBlank()) {

                        currentMainCategory =
                                mainCategory;
                    }

                    /*
                     * Ignore rows without a subcategory.
                     *
                     * This also skips the explanatory
                     * text appearing below the coverage
                     * table.
                     */
                    if (currentMainCategory.isBlank()
                            || subCategory.isBlank()) {

                        continue;
                    }

                    String p001Rule =
                            readString(row.getCell(2));

                    String p002Rule =
                            readString(row.getCell(3));

                    String p003Rule =
                            readString(row.getCell(4));

                    addRulesForSubCategories(
                            "P001",
                            currentMainCategory,
                            subCategory,
                            p001Rule);

                    addRulesForSubCategories(
                            "P002",
                            currentMainCategory,
                            subCategory,
                            p002Rule);

                    addRulesForSubCategories(
                            "P003",
                            currentMainCategory,
                            subCategory,
                            p003Rule);
                }
            }

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Unable to read PlanCoverage",
                    exception);
        }

        System.out.println(
                "Coverage rules loaded from Excel: "
                        + coverageRules.size());
    }


    private void addRulesForSubCategories(
            String planId,
            String mainCategory,
            String subCategoryCell,
            String ruleText) {

        if (ruleText == null
                || ruleText.isBlank()) {

            return;
        }

        /*
         * Some Excel rows contain multiple subcategories.
         *
         * Example:
         *
         * ROOM AND BOARD, SURGERY, ANESTHESIA...
         *
         * The assignment says the same rule applies
         * individually to each of those services.
         */
        String[] subCategories =
                subCategoryCell.split(",");

        for (String subCategory : subCategories) {

            String cleanedSubCategory =
                    subCategory.trim();

            if (cleanedSubCategory.isBlank()) {
                continue;
            }

            CoverageRule coverageRule =
                    createCoverageRule(
                            planId,
                            mainCategory,
                            cleanedSubCategory,
                            ruleText);

            addCoverageRule(
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

        coverageRule.setPlanId(
                planId);

        coverageRule.setMainCategory(
                mainCategory);

        coverageRule.setSubCategory(
                subCategory);

        String normalizedRule =
                ruleText
                        .trim()
                        .toUpperCase();

        /*
         * RULE 1:
         * NO CHARGE
         */
        if ("NO CHARGE".equals(
                normalizedRule)) {

            coverageRule.setRuleType(
                    "NO_CHARGE");

            coverageRule.setCoverageValue(
                    null);

            return coverageRule;
        }

        /*
         * RULE 2:
         * Percentage after deductible.
         *
         * Example:
         * 40% AFTER DEDUCTIBLE
         */
        if (normalizedRule.contains("%")) {

            int percentageIndex =
                    normalizedRule.indexOf("%");

            String percentageValue =
                    normalizedRule
                            .substring(
                                    0,
                                    percentageIndex)
                            .trim();

            coverageRule.setRuleType(
                    "PERCENTAGE_AFTER_DEDUCTIBLE");

            coverageRule.setCoverageValue(
                    new BigDecimal(
                            percentageValue));

            return coverageRule;
        }

        /*
         * RULE 3:
         * Flat-dollar amount.
         *
         * Actual workbook values for P002/P003
         * urgent care are 100 and 120.
         */
        try {

            String flatDollarValue =
                    normalizedRule
                            .replace("$", "")
                            .replace("FLAT", "")
                            .trim();

            BigDecimal amount =
                    new BigDecimal(
                            flatDollarValue);

            coverageRule.setRuleType(
                    "FLAT_DOLLAR");

            coverageRule.setCoverageValue(
                    amount);

            return coverageRule;

        } catch (NumberFormatException exception) {

            throw new IllegalStateException(
                    "Unsupported coverage rule: "
                            + ruleText
                            + " for "
                            + planId
                            + " / "
                            + mainCategory
                            + " / "
                            + subCategory,
                    exception);
        }
    }


    private void addCoverageRule(
            CoverageRule coverageRule) {

        String key =
                buildKey(
                        coverageRule.getPlanId(),
                        coverageRule.getMainCategory(),
                        coverageRule.getSubCategory());

        coverageRules.put(
                key,
                coverageRule);
    }


    public CoverageRule findCoverageRule(
            String planId,
            String mainCategory,
            String subCategory) {

        String key =
                buildKey(
                        planId,
                        mainCategory,
                        subCategory);

        return coverageRules.get(
                key);
    }


    private String buildKey(
            String planId,
            String mainCategory,
            String subCategory) {

        return normalize(planId)
                + "|"
                + normalize(mainCategory)
                + "|"
                + normalize(subCategory);
    }


    private String normalize(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replaceAll("\\s+", " ")
                .toUpperCase();
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
}
