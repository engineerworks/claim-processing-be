package com.claims.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PolicyHolder {

    private String policyId;
    private String policyHolderId;
    private String memberName;
    private String planId;

    private LocalDate coverageStartDate;
    private LocalDate coverageEndDate;

    private BigDecimal individualAccumulatedDeductible;
    private BigDecimal familyAccumulatedDeductible;

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getPolicyHolderId() {
        return policyHolderId;
    }

    public void setPolicyHolderId(String policyHolderId) {
        this.policyHolderId = policyHolderId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public LocalDate getCoverageStartDate() {
        return coverageStartDate;
    }

    public void setCoverageStartDate(LocalDate coverageStartDate) {
        this.coverageStartDate = coverageStartDate;
    }

    public LocalDate getCoverageEndDate() {
        return coverageEndDate;
    }

    public void setCoverageEndDate(LocalDate coverageEndDate) {
        this.coverageEndDate = coverageEndDate;
    }

    public BigDecimal getIndividualAccumulatedDeductible() {
        return individualAccumulatedDeductible;
    }

    public void setIndividualAccumulatedDeductible(
            BigDecimal individualAccumulatedDeductible) {
        this.individualAccumulatedDeductible =
                individualAccumulatedDeductible;
    }

    public BigDecimal getFamilyAccumulatedDeductible() {
        return familyAccumulatedDeductible;
    }

    public void setFamilyAccumulatedDeductible(
            BigDecimal familyAccumulatedDeductible) {
        this.familyAccumulatedDeductible =
                familyAccumulatedDeductible;
    }
}
