package com.ddogalmap.domain.users.repository;

import com.ddogalmap.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKakaoId(Long kakaoId);

    @Query("""
        select u
        from User u
        left join fetch u.representativeBadge
        where u.userId = :userId
    """)
    Optional<User> findByIdWithRepresentativeBadge(@Param("userId") Long userId);
}