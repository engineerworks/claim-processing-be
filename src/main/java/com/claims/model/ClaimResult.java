package com.claims.model;

import java.math.BigDecimal;

public class ClaimResult {

    private String policyId;
    private String policyHolderId;
    private String planId;

    private BigDecimal billedAmount;
    private BigDecimal policyHolderPays;
    private BigDecimal planPays;

    private String ruleUsed;

    private BigDecimal individualAccumulatedDeductible;
    private BigDecimal familyAccumulatedDeductible;

    private String errorCode;
    private String errorMessage;
    private String processingMessage;

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

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public BigDecimal getBilledAmount() {
        return billedAmount;
    }

    public void setBilledAmount(BigDecimal billedAmount) {
        this.billedAmount = billedAmount;
    }

    public BigDecimal getPolicyHolderPays() {
        return policyHolderPays;
    }

    public void setPolicyHolderPays(BigDecimal policyHolderPays) {
        this.policyHolderPays = policyHolderPays;
    }

    public BigDecimal getPlanPays() {
        return planPays;
    }

    public void setPlanPays(BigDecimal planPays) {
        this.planPays = planPays;
    }

    public String getRuleUsed() {
        return ruleUsed;
    }

    public void setRuleUsed(String ruleUsed) {
        this.ruleUsed = ruleUsed;
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

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getProcessingMessage() {
        return processingMessage;
    }

    public void setProcessingMessage(String processingMessage) {
        this.processingMessage = processingMessage;
    }
}
