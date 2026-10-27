package com.claims.repository;

import com.claims.model.CoverageRule;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Repository
public class CoverageRuleRepository {

    private final Map<String, CoverageRule> coverageRules =
            new HashMap<>();

    public CoverageRuleRepository() {
        loadCoverageRules();
    }

    private void loadCoverageRules() {

        // =================================================
        // P001 - ROOM AND BOARD
        // 40% AFTER DEDUCTIBLE
        // =================================================

        CoverageRule p001RoomAndBoard =
                new CoverageRule();

        p001RoomAndBoard.setPlanId("P001");

        p001RoomAndBoard.setMainCategory(
                "Inpatient Hospital Care");

        p001RoomAndBoard.setSubCategory(
                "ROOM AND BOARD");

        p001RoomAndBoard.setRuleType(
                "PERCENTAGE_AFTER_DEDUCTIBLE");

        p001RoomAndBoard.setCoverageValue(
                new BigDecimal("40"));

        addCoverageRule(p001RoomAndBoard);


        // =================================================
        // P002 - ROOM AND BOARD
        // 50% AFTER DEDUCTIBLE
        // =================================================

        CoverageRule p002RoomAndBoard =
                new CoverageRule();

        p002RoomAndBoard.setPlanId("P002");

        p002RoomAndBoard.setMainCategory(
                "Inpatient Hospital Care");

        p002RoomAndBoard.setSubCategory(
                "ROOM AND BOARD");

        p002RoomAndBoard.setRuleType(
                "PERCENTAGE_AFTER_DEDUCTIBLE");

        p002RoomAndBoard.setCoverageValue(
                new BigDecimal("50"));

        addCoverageRule(p002RoomAndBoard);


        // =================================================
        // P003 - ROOM AND BOARD
        // 60% AFTER DEDUCTIBLE
        // =================================================

        CoverageRule p003RoomAndBoard =
                new CoverageRule();

        p003RoomAndBoard.setPlanId("P003");

        p003RoomAndBoard.setMainCategory(
                "Inpatient Hospital Care");

        p003RoomAndBoard.setSubCategory(
                "ROOM AND BOARD");

        p003RoomAndBoard.setRuleType(
                "PERCENTAGE_AFTER_DEDUCTIBLE");

        p003RoomAndBoard.setCoverageValue(
                new BigDecimal("60"));

        addCoverageRule(p003RoomAndBoard);


        // =================================================
        // P001 - PREVENTIVE CARE
        // NO CHARGE
        // =================================================

        CoverageRule p001PreventiveCare =
                new CoverageRule();

        p001PreventiveCare.setPlanId("P001");

        p001PreventiveCare.setMainCategory(
                "Preventive Care");

        p001PreventiveCare.setSubCategory(
                "ROUTINE PHYSICAL EXAM");

        p001PreventiveCare.setRuleType(
                "NO_CHARGE");

        p001PreventiveCare.setCoverageValue(null);

        addCoverageRule(p001PreventiveCare);


        // =================================================
        // P003 - URGENT CARE
        // FLAT $120
        // =================================================

        CoverageRule p003UrgentCare =
                new CoverageRule();

        p003UrgentCare.setPlanId("P003");

        p003UrgentCare.setMainCategory(
                "Urgent Care");

        p003UrgentCare.setSubCategory(
                "URGENT CARE VISIT");

        p003UrgentCare.setRuleType(
                "FLAT_DOLLAR");

        p003UrgentCare.setCoverageValue(
                new BigDecimal("120"));

        addCoverageRule(p003UrgentCare);
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

        return coverageRules.get(key);
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


    private String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toUpperCase();
    }
}
