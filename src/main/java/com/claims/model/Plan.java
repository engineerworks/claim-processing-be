package com.claims.model;

import java.math.BigDecimal;

public class Plan {

    private String planId;
    private BigDecimal individualDeductible;
    private BigDecimal familyDeductible;

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public BigDecimal getIndividualDeductible() {
        return individualDeductible;
    }

    public void setIndividualDeductible(BigDecimal individualDeductible) {
        this.individualDeductible = individualDeductible;
    }

    public BigDecimal getFamilyDeductible() {
        return familyDeductible;
    }

    public void setFamilyDeductible(BigDecimal familyDeductible) {
        this.familyDeductible = familyDeductible;
    }
}
