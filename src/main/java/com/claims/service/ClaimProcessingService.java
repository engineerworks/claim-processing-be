package com.claims.service;

import com.claims.model.Claim;
import com.claims.model.ClaimResult;
import com.claims.model.CoverageRule;
import com.claims.model.Plan;
import com.claims.model.PolicyHolder;
import com.claims.repository.CoverageRuleRepository;
import com.claims.repository.PlanRepository;
import com.claims.repository.PolicyHolderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ClaimProcessingService {

    private static final String NO_CHARGE =
            "NO_CHARGE";

    private static final String FLAT_DOLLAR =
            "FLAT_DOLLAR";

    private static final String PERCENTAGE_AFTER_DEDUCTIBLE =
            "PERCENTAGE_AFTER_DEDUCTIBLE";

    private final PolicyHolderRepository policyHolderRepository;
    private final PlanRepository planRepository;
    private final CoverageRuleRepository coverageRuleRepository;

    public ClaimProcessingService(
            PolicyHolderRepository policyHolderRepository,
            PlanRepository planRepository,
            CoverageRuleRepository coverageRuleRepository) {

        this.policyHolderRepository = policyHolderRepository;
        this.planRepository = planRepository;
        this.coverageRuleRepository = coverageRuleRepository;
    }

    public ClaimResult processClaim(Claim claim) {

        ClaimResult result =
                createInitialResult(claim);

        // ---------------------------------------------
        // 1. Validate basic claim information
        // ---------------------------------------------

        if (claim == null) {

            return createErrorResult(
                    result,
                    "E0002",
                    "Claim is required");
        }

        if (claim.getPolicyHolderId() == null
                || claim.getPolicyHolderId().isBlank()) {

            return createErrorResult(
                    result,
                    "E0001",
                    "Policy holder does not exist");
        }

        if (claim.getBilledAmount() == null
                || claim.getBilledAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {

            return createErrorResult(
                    result,
                    "E0002",
                    "Billed amount must be greater than zero");
        }


        // ---------------------------------------------
        // 2. Find policyholder
        // ---------------------------------------------

        PolicyHolder policyHolder =
                policyHolderRepository.findByPolicyHolderId(
                        claim.getPolicyHolderId());

        if (policyHolder == null) {

            return createErrorResult(
                    result,
                    "E0001",
                    "Policy holder does not exist");
        }


        // ---------------------------------------------
        // 3. Validate Policy ID
        // ---------------------------------------------

        if (claim.getPolicyId() == null
                || !policyHolder.getPolicyId()
                .equals(claim.getPolicyId())) {

            return createErrorResult(
                    result,
                    "E0003",
                    "Policy does not match policy holder");
        }


        // ---------------------------------------------
        // 4. Validate Date of Service
        // ---------------------------------------------

        if (claim.getDateOfService() == null) {

            return createErrorResult(
                    result,
                    "E0007",
                    "Date of service is required");
        }


        // Claim cannot happen before coverage starts.

        if (policyHolder.getCoverageStartDate() != null
                && claim.getDateOfService()
                .isBefore(
                        policyHolder.getCoverageStartDate())) {

            return createErrorResult(
                    result,
                    "E0008",
                    "Coverage was not active on date of service");
        }


        // Claim cannot happen after coverage ends.
        //
        // If coverageEndDate is null, there is no
        // specified coverage end date.

        if (policyHolder.getCoverageEndDate() != null
                && claim.getDateOfService()
                .isAfter(
                        policyHolder.getCoverageEndDate())) {

            return createErrorResult(
                    result,
                    "E0008",
                    "Coverage was not active on date of service");
        }


        // ---------------------------------------------
        // 5. Find Plan
        // ---------------------------------------------

        Plan plan =
                planRepository.findByPlanId(
                        policyHolder.getPlanId());

        if (plan == null) {

            return createErrorResult(
                    result,
                    "E0004",
                    "Plan does not exist");
        }

        result.setPlanId(
                plan.getPlanId());


        // ---------------------------------------------
        // 6. Find Coverage Rule
        // ---------------------------------------------

        CoverageRule coverageRule =
                coverageRuleRepository.findCoverageRule(
                        plan.getPlanId(),
                        claim.getMainCategory(),
                        claim.getSubCategory());

        if (coverageRule == null) {

            return createErrorResult(
                    result,
                    "E0005",
                    "Coverage rule does not exist");
        }

        result.setRuleUsed(
                coverageRule.getRuleType());


        // ---------------------------------------------
        // 7. Calculate Payment
        // ---------------------------------------------

        PaymentCalculation paymentCalculation =
                calculatePayment(
                        claim,
                        policyHolder,
                        plan,
                        coverageRule);


        if (paymentCalculation.errorMessage != null) {

            return createErrorResult(
                    result,
                    "E0006",
                    paymentCalculation.errorMessage);
        }


        // ---------------------------------------------
        // 8. Update accumulated deductibles
        // ---------------------------------------------

        BigDecimal updatedIndividualDeductible =
                policyHolder
                        .getIndividualAccumulatedDeductible()
                        .add(
                                paymentCalculation
                                        .policyHolderPays);

        BigDecimal updatedFamilyDeductible =
                policyHolder
                        .getFamilyAccumulatedDeductible()
                        .add(
                                paymentCalculation
                                        .policyHolderPays);


        // Keep updated state in memory.

        policyHolder.setIndividualAccumulatedDeductible(
                updatedIndividualDeductible);

        policyHolder.setFamilyAccumulatedDeductible(
                updatedFamilyDeductible);


        // ---------------------------------------------
        // 9. Create successful result
        // ---------------------------------------------

        result.setPolicyHolderPays(
                paymentCalculation.policyHolderPays);

        result.setPlanPays(
                paymentCalculation.planPays);

        result.setIndividualAccumulatedDeductible(
                updatedIndividualDeductible);

        result.setFamilyAccumulatedDeductible(
                updatedFamilyDeductible);

        result.setProcessingMessage(
                paymentCalculation.processingMessage);

        return result;
    }


    private PaymentCalculation calculatePayment(
            Claim claim,
            PolicyHolder policyHolder,
            Plan plan,
            CoverageRule coverageRule) {

        String ruleType =
                coverageRule.getRuleType();


        // =============================================
        // RULE 1: NO CHARGE
        // =============================================

        if (NO_CHARGE.equals(ruleType)) {

            BigDecimal policyHolderPays =
                    BigDecimal.ZERO
                            .setScale(2);

            BigDecimal planPays =
                    claim.getBilledAmount()
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP);

            return new PaymentCalculation(
                    policyHolderPays,
                    planPays,
                    "Claim processed using no-charge coverage",
                    null);
        }


        // =============================================
        // RULE 2: FLAT DOLLAR
        // =============================================

        if (FLAT_DOLLAR.equals(ruleType)) {

            if (coverageRule.getCoverageValue() == null) {

                return PaymentCalculation.error(
                        "Flat-dollar coverage value is missing");
            }

            /*
             * The plan cannot pay more than
             * the actual billed amount.
             */
            BigDecimal planPays =
                    coverageRule
                            .getCoverageValue()
                            .min(
                                    claim.getBilledAmount())
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP);


            BigDecimal policyHolderPays =
                    claim.getBilledAmount()
                            .subtract(planPays)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP);


            return new PaymentCalculation(
                    policyHolderPays,
                    planPays,
                    "Claim processed using flat-dollar coverage",
                    null);
        }


        // =============================================
        // RULE 3:
        // PERCENTAGE AFTER DEDUCTIBLE
        // =============================================

        if (PERCENTAGE_AFTER_DEDUCTIBLE.equals(
                ruleType)) {

            if (coverageRule.getCoverageValue() == null) {

                return PaymentCalculation.error(
                        "Percentage coverage value is missing");
            }


            boolean individualDeductibleMet =
                    policyHolder
                            .getIndividualAccumulatedDeductible()
                            .compareTo(
                                    plan.getIndividualDeductible())
                            >= 0;


            boolean familyDeductibleMet =
                    policyHolder
                            .getFamilyAccumulatedDeductible()
                            .compareTo(
                                    plan.getFamilyDeductible())
                            >= 0;


            boolean deductibleMet =
                    individualDeductibleMet
                            || familyDeductibleMet;


            /*
             * If neither deductible is met,
             * member pays the complete bill.
             */
            if (!deductibleMet) {

                BigDecimal policyHolderPays =
                        claim.getBilledAmount()
                                .setScale(
                                        2,
                                        RoundingMode.HALF_UP);

                BigDecimal planPays =
                        BigDecimal.ZERO
                                .setScale(2);


                return new PaymentCalculation(
                        policyHolderPays,
                        planPays,
                        "Deductible not met; policy holder pays full amount",
                        null);
            }


            /*
             * Convert percentage:
             *
             * 40
             *
             * into:
             *
             * 0.40
             */
            BigDecimal coveragePercentage =
                    coverageRule
                            .getCoverageValue()
                            .divide(
                                    new BigDecimal("100"),
                                    4,
                                    RoundingMode.HALF_UP);


            BigDecimal planPays =
                    claim.getBilledAmount()
                            .multiply(
                                    coveragePercentage)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP);


            BigDecimal policyHolderPays =
                    claim.getBilledAmount()
                            .subtract(
                                    planPays)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP);


            String deductibleMessage;

            if (individualDeductibleMet) {

                deductibleMessage =
                        "Individual deductible met";

            } else {

                deductibleMessage =
                        "Family deductible met";
            }


            return new PaymentCalculation(
                    policyHolderPays,
                    planPays,
                    deductibleMessage
                            + "; percentage coverage applied",
                    null);
        }


        return PaymentCalculation.error(
                "Unsupported coverage rule: "
                        + ruleType);
    }


    private ClaimResult createInitialResult(
            Claim claim) {

        ClaimResult result =
                new ClaimResult();

        if (claim != null) {

            result.setPolicyId(
                    claim.getPolicyId());

            result.setPolicyHolderId(
                    claim.getPolicyHolderId());

            result.setBilledAmount(
                    claim.getBilledAmount());
        }

        return result;
    }


    private ClaimResult createErrorResult(
            ClaimResult result,
            String errorCode,
            String errorMessage) {

        result.setErrorCode(
                errorCode);

        result.setErrorMessage(
                errorMessage);

        result.setProcessingMessage(
                "Claim processing failed");

        return result;
    }


    private static class PaymentCalculation {

        private final BigDecimal policyHolderPays;

        private final BigDecimal planPays;

        private final String processingMessage;

        private final String errorMessage;


        private PaymentCalculation(
                BigDecimal policyHolderPays,
                BigDecimal planPays,
                String processingMessage,
                String errorMessage) {

            this.policyHolderPays =
                    policyHolderPays;

            this.planPays =
                    planPays;

            this.processingMessage =
                    processingMessage;

            this.errorMessage =
                    errorMessage;
        }


        private static PaymentCalculation error(
                String errorMessage) {

            return new PaymentCalculation(
                    null,
                    null,
                    null,
                    errorMessage);
        }
    }
}
