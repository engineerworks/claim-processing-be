package com.claims.service;

import com.claims.model.Claim;
import com.claims.model.ClaimResult;
import com.claims.model.CoverageRule;
import com.claims.model.Plan;
import com.claims.model.PolicyHolder;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ClaimProcessingService {

    public ClaimResult processClaim(
            Claim claim,
            PolicyHolder policyHolder,
            Plan plan,
            CoverageRule coverageRule) {

        ClaimResult result = new ClaimResult();

        result.setPolicyId(claim.getPolicyId());
        result.setPolicyHolderId(claim.getPolicyHolderId());
        result.setPlanId(plan.getPlanId());
        result.setBilledAmount(claim.getBilledAmount());
        result.setRuleUsed(coverageRule.getRuleType());

        boolean individualDeductibleMet =
                policyHolder
                        .getIndividualAccumulatedDeductible()
                        .compareTo(plan.getIndividualDeductible()) >= 0;

        boolean familyDeductibleMet =
                policyHolder
                        .getFamilyAccumulatedDeductible()
                        .compareTo(plan.getFamilyDeductible()) >= 0;

        boolean deductibleMet =
                individualDeductibleMet || familyDeductibleMet;

        BigDecimal policyHolderPays;
        BigDecimal planPays;

        if ("NO_CHARGE".equals(coverageRule.getRuleType())) {

            policyHolderPays = BigDecimal.ZERO;
            planPays = claim.getBilledAmount();

        } else if ("FLAT_DOLLAR".equals(coverageRule.getRuleType())) {

            planPays = coverageRule
                    .getCoverageValue()
                    .min(claim.getBilledAmount());

            policyHolderPays =
                    claim.getBilledAmount().subtract(planPays);

        } else if ("PERCENTAGE_AFTER_DEDUCTIBLE"
                .equals(coverageRule.getRuleType())) {

            if (deductibleMet) {

                BigDecimal percentage =
                        coverageRule
                                .getCoverageValue()
                                .divide(
                                        new BigDecimal("100"),
                                        4,
                                        RoundingMode.HALF_UP);

                planPays =
                        claim.getBilledAmount()
                                .multiply(percentage)
                                .setScale(2, RoundingMode.HALF_UP);

                policyHolderPays =
                        claim.getBilledAmount()
                                .subtract(planPays)
                                .setScale(2, RoundingMode.HALF_UP);

            } else {

                policyHolderPays =
                        claim.getBilledAmount();

                planPays =
                        BigDecimal.ZERO;
            }

        } else {

            throw new IllegalArgumentException(
                    "Unknown coverage rule type: "
                            + coverageRule.getRuleType());
        }

        BigDecimal updatedIndividualDeductible =
                policyHolder
                        .getIndividualAccumulatedDeductible()
                        .add(policyHolderPays);

        BigDecimal updatedFamilyDeductible =
                policyHolder
                        .getFamilyAccumulatedDeductible()
                        .add(policyHolderPays);

        result.setPolicyHolderPays(policyHolderPays);
        result.setPlanPays(planPays);

        result.setIndividualAccumulatedDeductible(
                updatedIndividualDeductible);

        result.setFamilyAccumulatedDeductible(
                updatedFamilyDeductible);

        result.setProcessingMessage(
                "Claim processed successfully");

        return result;
    }
}
