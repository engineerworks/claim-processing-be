package com.claims.repository;

import com.claims.model.PolicyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
@Repository
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

        PolicyHolder p003Member =
                new PolicyHolder();

        p003Member.setPolicyId("100003");

        p003Member.setPolicyHolderId("1000031");

        p003Member.setMemberName(
                "P003 Test Member");

        p003Member.setPlanId("P003");

        p003Member.setCoverageStartDate(
                LocalDate.of(2016, 1, 1));

        p003Member.setCoverageEndDate(null);

        p003Member.setIndividualAccumulatedDeductible(
                new BigDecimal("2000"));

        p003Member.setFamilyAccumulatedDeductible(
                new BigDecimal("4000"));

        policyHolders.put(
                p003Member.getPolicyHolderId(),
                p003Member);
    }

    public PolicyHolder findByPolicyHolderId(
            String policyHolderId) {

        return policyHolders.get(policyHolderId);
    }
}
