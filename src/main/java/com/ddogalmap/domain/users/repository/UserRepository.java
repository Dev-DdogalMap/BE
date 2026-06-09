package com.ddogalmap.domain.users.repository;

import com.ddogalmap.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKakaoId(Long kakaoId);

    /**
     * 사용자의 인증된 동네(region) 만 가벼운 쿼리로 조회.
     * 인증 안 했거나 region 비어있으면 빈 Optional.
     */
    @Query("SELECT u.region FROM User u WHERE u.userId = :userId")
    Optional<String> findRegionByUserId(@Param("userId") Long userId);
}