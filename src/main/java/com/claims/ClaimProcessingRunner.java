package com.claims;

import com.claims.model.Claim;
import com.claims.model.ClaimResult;
import com.claims.model.CoverageRule;
import com.claims.model.Plan;
import com.claims.model.PolicyHolder;
import com.claims.repository.CoverageRuleRepository;
import com.claims.repository.PlanRepository;
import com.claims.repository.PolicyHolderRepository;
import com.claims.service.ClaimProcessingService;

import java.math.BigDecimal;

public class ClaimProcessingRunner {

    public static void main(String[] args) {

        // -------------------------------------------------
        // 1. Create repositories
        // -------------------------------------------------

        PolicyHolderRepository policyHolderRepository =
                new PolicyHolderRepository();

        PlanRepository planRepository =
                new PlanRepository();

        CoverageRuleRepository coverageRuleRepository =
                new CoverageRuleRepository();


        // -------------------------------------------------
        // 2. Create the submitted claim
        // -------------------------------------------------

        Claim claim = new Claim();

        claim.setPolicyId("100001");
        claim.setPolicyHolderId("1000011");

        claim.setMainCategory(
                "Inpatient Hospital Care");

        claim.setSubCategory(
                "ROOM AND BOARD");

        claim.setBilledAmount(
                new BigDecimal("1000"));


        // -------------------------------------------------
        // 3. Find policyholder
        // -------------------------------------------------

        PolicyHolder policyHolder =
                policyHolderRepository.findByPolicyHolderId(
                        claim.getPolicyHolderId());

        if (policyHolder == null) {

            System.out.println(
                    "E0001 - Policy holder does not exist");

            return;
        }


        // -------------------------------------------------
        // 4. Find policyholder's plan
        // -------------------------------------------------

        Plan plan =
                planRepository.findByPlanId(
                        policyHolder.getPlanId());

        if (plan == null) {

            System.out.println(
                    "Plan does not exist");

            return;
        }


        // -------------------------------------------------
        // 5. Find coverage rule
        //
        // Plan ID comes from PolicyHolder.
        // Category/Subcategory come from Claim.
        // -------------------------------------------------

        CoverageRule coverageRule =
                coverageRuleRepository.findCoverageRule(
                        plan.getPlanId(),
                        claim.getMainCategory(),
                        claim.getSubCategory());

        if (coverageRule == null) {

            System.out.println(
                    "Coverage rule does not exist");

            return;
        }


        // -------------------------------------------------
        // 6. Process claim
        // -------------------------------------------------

        ClaimProcessingService service =
                new ClaimProcessingService();

        ClaimResult result =
                service.processClaim(
                        claim,
                        policyHolder,
                        plan,
                        coverageRule);


        // -------------------------------------------------
        // 7. Display what the system discovered
        // -------------------------------------------------

        System.out.println();

        System.out.println(
                "Policyholder found: "
                        + policyHolder.getMemberName());

        System.out.println(
                "Policyholder plan: "
                        + policyHolder.getPlanId());

        System.out.println(
                "Coverage rule: "
                        + coverageRule.getRuleType());

        System.out.println(
                "Coverage value: "
                        + coverageRule.getCoverageValue());


        // -------------------------------------------------
        // 8. Display claim result
        // -------------------------------------------------

        System.out.println();
        System.out.println("----- Claim Result -----");

        System.out.println(
                "Policyholder ID: "
                        + result.getPolicyHolderId());

        System.out.println(
                "Plan: "
                        + result.getPlanId());

        System.out.println(
                "Billed Amount: "
                        + result.getBilledAmount());

        System.out.println(
                "Policyholder Pays: "
                        + result.getPolicyHolderPays());

        System.out.println(
                "Plan Pays: "
                        + result.getPlanPays());

        System.out.println(
                "Updated Individual Deductible: "
                        + result.getIndividualAccumulatedDeductible());

        System.out.println(
                "Updated Family Deductible: "
                        + result.getFamilyAccumulatedDeductible());

        System.out.println(
                "Message: "
                        + result.getProcessingMessage());
    }
}
