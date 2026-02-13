package com.deepseek.apiplatform.repository;

import com.deepseek.apiplatform.entity.UserOAuth;
import com.deepseek.apiplatform.entity.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserOAuthRepository extends JpaRepository<UserOAuth, Long> {
    Optional<UserOAuth> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);
    Optional<UserOAuth> findByUserIdAndProvider(Long userId, OAuthProvider provider);
    List<UserOAuth> findByUserId(Long userId);
    
    @Modifying
    @Query("DELETE FROM UserOAuth uo WHERE uo.userId = :userId AND uo.provider = :provider")
    void deleteByUserIdAndProvider(@Param("userId") Long userId, @Param("provider") OAuthProvider provider);
}
