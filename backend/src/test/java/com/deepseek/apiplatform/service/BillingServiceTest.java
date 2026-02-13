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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BillingRecordRepository billingRecordRepository;

    @Mock
    private UsageStatsRepository usageStatsRepository;

    private BillingService billingService;

    @BeforeEach
    void setUp() {
        billingService = new BillingService(userRepository, billingRecordRepository, usageStatsRepository);
    }

    @Test
    @DisplayName("获取用量统计 - 有数据")
    void getUsageStats_WithExistingData() {
        UsageStats stats = new UsageStats();
        stats.setUserId(1L);
        stats.setTotalTokens(1000L);
        stats.setPromptTokens(600L);
        stats.setCompletionTokens(400L);
        stats.setRequestCount(10L);

        when(usageStatsRepository.findByUserId(1L)).thenReturn(Optional.of(stats));

        UsageStatsResponse response = billingService.getUsageStats(1L);

        assertNotNull(response);
        assertEquals(1000L, response.getTotalTokens());
        assertEquals(600L, response.getPromptTokens());
        assertEquals(400L, response.getCompletionTokens());
        assertEquals(10L, response.getRequestCount());
    }

    @Test
    @DisplayName("获取用量统计 - 无数据时创建默认值")
    void getUsageStats_WithNoData_CreatesDefault() {
        when(usageStatsRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(usageStatsRepository.save(any(UsageStats.class))).thenAnswer(invocation -> {
            UsageStats stats = invocation.getArgument(0);
            stats.setId(1L);
            return stats;
        });

        UsageStatsResponse response = billingService.getUsageStats(1L);

        assertNotNull(response);
        assertEquals(0L, response.getTotalTokens());
        assertEquals(0L, response.getPromptTokens());
        assertEquals(0L, response.getCompletionTokens());
        assertEquals(0L, response.getRequestCount());
    }

    @Test
    @DisplayName("充值成功")
    void recharge_Success() {
        User user = new User();
        user.setId(1L);
        user.setBalance(BigDecimal.valueOf(100));

        RechargeRequest request = new RechargeRequest();
        request.setAmount(50.0);
        request.setPaymentMethod("alipay");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(billingRecordRepository.save(any(BillingRecord.class))).thenAnswer(invocation -> {
            BillingRecord record = invocation.getArgument(0);
            record.setId(1L);
            return record;
        });

        BillingRecordResponse response = billingService.recharge(1L, request);

        assertNotNull(response);
        assertEquals("recharge", response.getType());
        assertEquals(50.0, response.getAmount());
        assertEquals(150.0, response.getBalance());
        assertTrue(response.getDescription().contains("支付宝"));

        verify(userRepository).save(any(User.class));
        verify(billingRecordRepository).save(any(BillingRecord.class));
    }

    @Test
    @DisplayName("充值失败 - 用户不存在")
    void recharge_UserNotFound_ThrowsException() {
        RechargeRequest request = new RechargeRequest();
        request.setAmount(50.0);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            billingService.recharge(999L, request);
        });

        assertEquals("用户不存在", exception.getMessage());
        verify(billingRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("获取账单记录")
    void getBillingRecords_Success() {
        BillingRecord record1 = new BillingRecord();
        record1.setId(1L);
        record1.setUserId(1L);
        record1.setType(BillingRecord.RecordType.RECHARGE);
        record1.setAmount(BigDecimal.valueOf(100));
        record1.setBalance(BigDecimal.valueOf(100));
        record1.setDescription("充值");

        BillingRecord record2 = new BillingRecord();
        record2.setId(2L);
        record2.setUserId(1L);
        record2.setType(BillingRecord.RecordType.USAGE);
        record2.setAmount(BigDecimal.valueOf(-10));
        record2.setBalance(BigDecimal.valueOf(90));
        record2.setDescription("消费");

        when(billingRecordRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(record1, record2));

        var responses = billingService.getBillingRecords(1L);

        assertEquals(2, responses.size());
        assertEquals("recharge", responses.get(0).getType());
        assertEquals("usage", responses.get(1).getType());
    }
}
