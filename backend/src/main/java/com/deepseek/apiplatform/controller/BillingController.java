package com.deepseek.apiplatform.controller;

import com.deepseek.apiplatform.dto.BillingRecordResponse;
import com.deepseek.apiplatform.dto.RechargeRequest;
import com.deepseek.apiplatform.dto.UsageStatsResponse;
import com.deepseek.apiplatform.security.UserPrincipal;
import com.deepseek.apiplatform.service.BillingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/billing")
public class BillingController {
    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }
    
    @GetMapping("/usage")
    public ResponseEntity<UsageStatsResponse> getUsageStats(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(billingService.getUsageStats(principal.getId()));
    }
    
    @GetMapping("/records")
    public ResponseEntity<List<BillingRecordResponse>> getBillingRecords(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(billingService.getBillingRecords(principal.getId()));
    }
    
    @PostMapping("/recharge")
    public ResponseEntity<BillingRecordResponse> recharge(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RechargeRequest request) {
        return ResponseEntity.ok(billingService.recharge(principal.getId(), request));
    }
}
