package com.deepseek.apiplatform.repository;

import com.deepseek.apiplatform.entity.BillingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BillingRecordRepository extends JpaRepository<BillingRecord, Long> {
    List<BillingRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
}
