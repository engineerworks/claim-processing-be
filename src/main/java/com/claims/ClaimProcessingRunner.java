package com.claims;

import com.claims.model.Claim;
import com.claims.model.ClaimResult;
import com.claims.repository.CoverageRuleRepository;
import com.claims.repository.PlanRepository;
import com.claims.repository.PolicyHolderRepository;
import com.claims.service.ClaimProcessingService;

import java.math.BigDecimal;

public class ClaimProcessingRunner {

    public static void main(String[] args) {

        PolicyHolderRepository policyHolderRepository =
                new PolicyHolderRepository();

        PlanRepository planRepository =
                new PlanRepository();

        CoverageRuleRepository coverageRuleRepository =
                new CoverageRuleRepository();

        ClaimProcessingService service =
                new ClaimProcessingService(
                        policyHolderRepository,
                        planRepository,
                        coverageRuleRepository);


        // This is the submitted claim
        Claim claim = new Claim();

        claim.setPolicyId("100001");
        claim.setPolicyHolderId("1000011");

        claim.setMainCategory(
                "Inpatient Hospital Care");

        claim.setSubCategory(
                "ROOM AND BOARD");

        claim.setBilledAmount(
                new BigDecimal("1000"));


        // Caller only submits Claim
        ClaimResult result =
                service.processClaim(claim);


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
                "Error Code: "
                        + result.getErrorCode());

        System.out.println(
                "Error Message: "
                        + result.getErrorMessage());

        System.out.println(
                "Message: "
                        + result.getProcessingMessage());
    }
}
