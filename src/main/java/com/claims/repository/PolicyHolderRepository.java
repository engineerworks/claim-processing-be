package com.claims.repository;

import com.claims.model.PolicyHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class PolicyHolderRepository {

    private final Map<String, PolicyHolder> policyHolders =
            new HashMap<>();

    public PolicyHolderRepository() {
        loadPolicyHolders();
    }

    private void loadPolicyHolders() {

        PolicyHolder sam = new PolicyHolder();

        sam.setPolicyId("100001");
        sam.setPolicyHolderId("1000011");
        sam.setMemberName("Sam Collins");
        sam.setPlanId("P001");

        sam.setCoverageStartDate(
                LocalDate.of(2016, 1, 1));

        sam.setCoverageEndDate(null);

        sam.setIndividualAccumulatedDeductible(
                new BigDecimal("6000"));

        sam.setFamilyAccumulatedDeductible(
                new BigDecimal("6000"));

        policyHolders.put(
                sam.getPolicyHolderId(),
                sam);
    }

    public PolicyHolder findByPolicyHolderId(
            String policyHolderId) {

        return policyHolders.get(policyHolderId);
    }
}
