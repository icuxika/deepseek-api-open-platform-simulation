package com.deepseek.apiplatform.service;

import com.deepseek.apiplatform.dto.BillingRecordResponse;
import com.deepseek.apiplatform.dto.RechargeRequest;
import com.deepseek.apiplatform.dto.UsageStatsResponse;
import com.deepseek.apiplatform.entity.BillingRecord;
import com.deepseek.apiplatform.entity.UsageStats;
import com.deepseek.apiplatform.entity.User;
import com.deepseek.apiplatform.repository.BillingRecordRepository;
import com.deepseek.apiplatform.repository.UsageStatsRepository;
import com.deepseek.apiplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillingService {
    private final UserRepository userRepository;
    private final BillingRecordRepository billingRecordRepository;
    private final UsageStatsRepository usageStatsRepository;

    public BillingService(UserRepository userRepository, BillingRecordRepository billingRecordRepository, UsageStatsRepository usageStatsRepository) {
        this.userRepository = userRepository;
        this.billingRecordRepository = billingRecordRepository;
        this.usageStatsRepository = usageStatsRepository;
    }
    
    public UsageStatsResponse getUsageStats(Long userId) {
        UsageStats stats = usageStatsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultUsageStats(userId));
        
        return new UsageStatsResponse(
            stats.getTotalTokens(),
            stats.getPromptTokens(),
            stats.getCompletionTokens(),
            stats.getRequestCount()
        );
    }
    
    public List<BillingRecordResponse> getBillingRecords(Long userId) {
        return billingRecordRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toBillingRecordResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public BillingRecordResponse recharge(Long userId, RechargeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        BigDecimal amount = BigDecimal.valueOf(request.getAmount());
        BigDecimal newBalance = user.getBalance().add(amount);
        user.setBalance(newBalance);
        userRepository.save(user);
        
        BillingRecord record = new BillingRecord();
        record.setUserId(userId);
        record.setType(BillingRecord.RecordType.RECHARGE);
        record.setAmount(amount);
        record.setBalance(newBalance);
        record.setDescription("账户充值 - " + ("alipay".equals(request.getPaymentMethod()) ? "支付宝" : "微信支付"));
        
        record = billingRecordRepository.save(record);
        return toBillingRecordResponse(record);
    }
    
    private UsageStats createDefaultUsageStats(Long userId) {
        UsageStats stats = new UsageStats();
        stats.setUserId(userId);
        stats.setTotalTokens(0L);
        stats.setPromptTokens(0L);
        stats.setCompletionTokens(0L);
        stats.setRequestCount(0L);
        return usageStatsRepository.save(stats);
    }
    
    private BillingRecordResponse toBillingRecordResponse(BillingRecord record) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return new BillingRecordResponse(
            record.getId(),
            record.getType().name().toLowerCase(),
            record.getAmount().doubleValue(),
            record.getBalance().doubleValue(),
            record.getDescription(),
            record.getCreatedAt() != null ? record.getCreatedAt().format(formatter) : null
        );
    }
}
