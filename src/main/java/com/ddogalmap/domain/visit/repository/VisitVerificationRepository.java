package com.ddogalmap.domain.visit.repository;

import com.ddogalmap.domain.visit.dto.projection.VisitVerificationCountProjection;
import com.ddogalmap.domain.visit.entity.VisitVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VisitVerificationRepository extends JpaRepository<VisitVerification, Long> {
    int countByUserUserId(Long userId);

    @Query(value = """
        SELECT
            COUNT(*) AS totalCount,
            COUNT(*) FILTER (
                WHERE EXTRACT(HOUR FROM created_at) < 12
            ) AS morningCount,
            COUNT(*) FILTER (
                WHERE EXTRACT(HOUR FROM created_at) >= 20
            ) AS nightCount
        FROM visit_verifications
        WHERE user_id = :userId
    """, nativeQuery = true)
    VisitVerificationCountProjection countVisitBadgesByUserId(
            @Param("userId") Long userId
    );
}
