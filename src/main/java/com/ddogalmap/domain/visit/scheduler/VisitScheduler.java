package com.ddogalmap.domain.visit.scheduler;

import com.ddogalmap.domain.visit.repository.VisitVerificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class VisitScheduler {
    private final VisitVerificationRepository visitVerificationRepository;

    /**
     * 매일 새벽 0시에 실행되어 인증 후 7일이 지난 미작성 방문 인증을 삭제합니다.
     */
    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul") // 초 분 시 일 월 요일 (매일 00:00:00)
    public void deleteExpiredUnwrittenReviews() {
        // 현재 시간 기준으로 7일 전 시점 계산
        LocalDateTime targetTime = LocalDateTime.now().minusDays(7);

        // 삭제 연산 실행 후 삭제된 건수 반환
        int deletedCount = visitVerificationRepository.deleteExpiredVerifications(targetTime);

        log.info("[스케줄러] 7일 경과된 미작성 방문 인증 {}건 자동 삭제 완료. (기준 시점: {})", deletedCount, targetTime);
    }
}
