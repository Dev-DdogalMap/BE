package com.ddogalmap.domain.visit.repository;

import com.ddogalmap.domain.visit.entity.VisitVerification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VisitVerificationRepository extends JpaRepository<VisitVerification, Long> {
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
}
