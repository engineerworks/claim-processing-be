package com.claims.repository;

import com.claims.model.Plan;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
@Repository
public class PlanRepository {

    private final Map<String, Plan> plans =
            new HashMap<>();

    public PlanRepository() {
        loadPlans();
    }

    private void loadPlans() {

        Plan p001 = new Plan();

        p001.setPlanId("P001");
        p001.setIndividualDeductible(
                new BigDecimal("6000"));
        p001.setFamilyDeductible(
                new BigDecimal("12000"));

        plans.put(
                p001.getPlanId(),
                p001);


        Plan p002 = new Plan();

        p002.setPlanId("P002");
        p002.setIndividualDeductible(
                new BigDecimal("5000"));
        p002.setFamilyDeductible(
                new BigDecimal("10000"));

        plans.put(
                p002.getPlanId(),
                p002);


        Plan p003 = new Plan();

        p003.setPlanId("P003");
        p003.setIndividualDeductible(
                new BigDecimal("4000"));
        p003.setFamilyDeductible(
                new BigDecimal("8000"));

        plans.put(
                p003.getPlanId(),
                p003);
    }

    public Plan findByPlanId(String planId) {
        return plans.get(planId);
    }
}
