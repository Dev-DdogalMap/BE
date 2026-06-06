package com.ddogalmap.domain.users.service;

import com.ddogalmap.domain.users.dto.request.RegionVerificationRequest;
import com.ddogalmap.domain.users.dto.response.RegionVerificationResponse;
import com.ddogalmap.domain.users.dto.response.RegionVerificationStatusResponse;
import com.ddogalmap.domain.users.entity.GpsLog;
import com.ddogalmap.domain.regions.entity.Region;
import com.ddogalmap.domain.users.entity.User;
import com.ddogalmap.domain.users.entity.UserRegionAttempt;
import com.ddogalmap.domain.users.repository.GpsLogRepository;
import com.ddogalmap.domain.regions.repository.RegionRepository;
import com.ddogalmap.domain.users.repository.UserRegionAttemptRepository;
import com.ddogalmap.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionVerificationServiceImpl implements RegionVerificationService {

	private static final double MAX_ACCURACY = 50.0;
	private static final int STABLE_ATTEMPT_COUNT = 3;
	private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

	private final UserRepository userRepository;
	private final RegionRepository regionRepository;
	private final GpsLogRepository gpsLogRepository;
	private final UserRegionAttemptRepository userRegionAttemptRepository;

	@Override
	public RegionVerificationStatusResponse getRegionVerification(Long userId) {

		log.info("[내 동네 인증 정보 조회 시작] userId={}", userId);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

		RegionVerificationStatusResponse response =
				new RegionVerificationStatusResponse(
						user.getRegion() != null ? user.getRegion() : null,
						user.getRegion() != null,
						user.getRegionVerifiedAt()
				);

		log.info("[내 동네 인증 정보 조회 완료] userId={}, verified={}, region={}", userId, response.verified(), response.eupmyeondongName());

		return response;
	}


	@Override
	@Transactional
	public RegionVerificationResponse verifyRegion(Long userId, RegionVerificationRequest request) {

		log.info("[Region Verify Start] userId={}, lat={}, lng={}, accuracy={}", userId, request.latitude(), request.longitude(), request.accuracy());

		validateCoordinates(request.latitude(), request.longitude());
		validateAccuracy(request.accuracy());

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		Region region = regionRepository.findRegionByPoint(request.latitude(), request.longitude())
				.orElseThrow(() -> new IllegalArgumentException("인증 가능한 지역이 아닙니다."));

		LocalDateTime verifiedAt = null;

		// GPS 로그 저장
		saveGpsLog(user, request.latitude(), request.longitude(), request.accuracy());

		// 인증 시도 저장
		userRegionAttemptRepository.save(new UserRegionAttempt(user, region, request.latitude(), request.longitude(), request.accuracy()));

		// 최근 3회 동일 지역 인증 여부 확인
		boolean stable = isLastThreeSame(user, region);

		log.debug("[Region Verify] userId={}, region={}, stable={}",
				userId, region.getEupmyeondongName(), stable);

		// 3회 연속 동일 지역인 경우에만 사용자 지역 정보 갱신
		if (stable) {
			verifiedAt = LocalDateTime.now();
			user.updateRegion(region.getEupmyeondongName(), verifiedAt);
			log.debug("[Region Verify] 사용자 지역 갱신 완료 - userId={}, region={}",
					userId, region.getEupmyeondongName());
		}

		return new RegionVerificationResponse(
				region.getEupmyeondongName(), stable, verifiedAt);
	}

	private void validateAccuracy(Double accuracy) {

		if (accuracy == null) {
			throw new IllegalArgumentException("accuracy is null");
		}

		if (accuracy <= 0) {
			throw new IllegalArgumentException("잘못된 accuracy 값입니다.");
		}

		if (accuracy > MAX_ACCURACY) {
			throw new IllegalArgumentException("GPS 정확도가 낮습니다.");
		}
	}

	private void validateCoordinates(Double lat, Double lng) {
		if (lat == null || lng == null) {
			throw new IllegalArgumentException("좌표가 없습니다.");
		}
		if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
			throw new IllegalArgumentException("좌표 범위가 잘못되었습니다.");
		}
	}

	private void saveGpsLog(User user, Double lat, Double lng, Double accuracy) {
		Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(lng, lat));
		gpsLogRepository.save(new GpsLog(user, point, accuracy));
	}

	private boolean isLastThreeSame(User user, Region region) {

		if (user == null || user.getUserId() == null) {
			throw new IllegalArgumentException("유효하지 않은 사용자입니다.");
		}

		if (region == null || region.getRegionId() == null) {
			throw new IllegalArgumentException("유효하지 않은 지역입니다.");
		}

		List<UserRegionAttempt> attempts =
				userRegionAttemptRepository.findTop3ByUser_UserIdOrderByCreatedAtDesc(
						user.getUserId()
				);

		log.debug("[연속 인증 확인] userId={}, 조회된 시도 수={}",
				user.getUserId(), attempts.size());

		if (attempts.size() < STABLE_ATTEMPT_COUNT) {
			log.debug("[연속 인증 확인] 시도 횟수 부족 → stable=false (currentSize={}, required={})",
					attempts.size(), STABLE_ATTEMPT_COUNT);
			return false;
		}

		for (int i = 0; i < attempts.size(); i++) {
			UserRegionAttempt a = attempts.get(i);
			log.debug("[연속 인증 확인] {}번째 시도 - regionId={}, regionName={}, createdAt={}",
					i + 1,
					a.getRegion().getRegionId(),
					a.getRegion().getEupmyeondongName(),
					a.getCreatedAt());
		}

		for (UserRegionAttempt a : attempts) {
			if (!a.getRegion().getRegionId().equals(region.getRegionId())) {
				log.debug("[연속 인증 확인] 다른 지역 발견 → stable=false (expected={}, actual={})",
						region.getRegionId(), a.getRegion().getRegionId());
				return false;
			}
		}

		log.debug("[연속 인증 확인] 최근 {}회 모두 동일 지역 → stable=true (regionId={})",
				STABLE_ATTEMPT_COUNT, region.getRegionId());
		return true;
	}
}


//
//	private boolean isLastThreeSame(User user, Region region) {
//
//		if (user == null || user.getUserId() == null) {
//			throw new IllegalArgumentException("유효하지 않은 사용자입니다.");
//		}
//
//		if (region == null || region.getRegionId() == null) {
//			throw new IllegalArgumentException("유효하지 않은 지역입니다.");
//		}
//
//		List<UserRegionAttempt> attempts =
//				userRegionAttemptRepository.findTop3ByUser_UserIdOrderByCreatedAtDesc(
//						user.getUserId()
//				);
//
//		if (attempts.size() < 3) {
//			return false;
//		}
//
//		for (UserRegionAttempt a : attempts) {
//			if (!a.getRegion().getRegionId().equals(region.getRegionId())) {
//				return false;
//			}
//		}
//
//		return true;
//	}
