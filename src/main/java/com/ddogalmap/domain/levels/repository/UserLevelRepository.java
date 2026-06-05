package com.ddogalmap.domain.levels.repository;

import com.ddogalmap.domain.levels.entity.UserLevel;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserLevelRepository extends JpaRepository<UserLevel, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
    })
    @Query("""
        select ul
        from UserLevel ul
        join fetch ul.user
        join fetch ul.level
        where ul.user.userId = :userId
    """)
    Optional<UserLevel> findForUpdate(@Param("userId") Long userId);

    boolean existsByUserUserId(Long userId);
}
