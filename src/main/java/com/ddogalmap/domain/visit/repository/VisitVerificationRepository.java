package com.ddogalmap.domain.visit.repository;

import com.ddogalmap.domain.visit.dto.projection.VisitVerificationCountProjection;
import com.ddogalmap.domain.visit.entity.VisitVerification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

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

    // 미작성 리뷰 조회용 JPQL 쿼리 명시
    @Query("SELECT vv FROM VisitVerification vv " +
            "JOIN FETCH vv.restaurant r " +
            "WHERE vv.user.userId = :userId " +
            "AND NOT EXISTS (" +
            "    SELECT 1 FROM Review rev " +
            "    WHERE rev.visitVerification = vv" +
            "    ORDER BY vv.verifiedAt DESC" +
            ")")
    Page<VisitVerification> findUnwrittenReviews(@Param("userId") Long userId, Pageable pageable);

    // 💡 7일이 지났고, 작성된 리뷰가 없는 방문 인증 데이터를 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM VisitVerification vv WHERE vv.verifiedAt < :targetTime " +
            "AND NOT EXISTS (SELECT 1 FROM Review r WHERE r.visitVerification = vv)")
    int deleteExpiredVerifications(@Param("targetTime") LocalDateTime targetTime);
}
