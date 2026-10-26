package com.claims.model;

import java.math.BigDecimal;

public class CoverageRule {

    private String planId;
    private String mainCategory;
    private String subCategory;

    private String ruleType;
    private BigDecimal coverageValue;

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(String mainCategory) {
        this.mainCategory = mainCategory;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public BigDecimal getCoverageValue() {
        return coverageValue;
    }

    public void setCoverageValue(BigDecimal coverageValue) {
        this.coverageValue = coverageValue;
    }
}
