package com.claims.controller;

import com.claims.model.Claim;
import com.claims.model.ClaimResult;
import com.claims.service.ClaimProcessingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimProcessingService claimProcessingService;

    public ClaimController(
            ClaimProcessingService claimProcessingService) {

        this.claimProcessingService =
                claimProcessingService;
    }

    @PostMapping("/process")
    public ClaimResult processClaim(
            @RequestBody Claim claim) {

        return claimProcessingService.processClaim(claim);
    }
}
