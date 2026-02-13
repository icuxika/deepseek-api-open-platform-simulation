package com.deepseek.apiplatform.repository;

import com.deepseek.apiplatform.entity.UsageStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsageStatsRepository extends JpaRepository<UsageStats, Long> {
    Optional<UsageStats> findByUserId(Long userId);
}
