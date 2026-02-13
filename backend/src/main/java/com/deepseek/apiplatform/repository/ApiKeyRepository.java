package com.deepseek.apiplatform.repository;

import com.deepseek.apiplatform.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    List<ApiKey> findByUserId(Long userId);
    void deleteByIdAndUserId(Long id, Long userId);
    Optional<ApiKey> findByApiKey(String apiKey);
}
