package com.ddogalmap.domain.users.service;

import com.ddogalmap.domain.users.dto.request.RegionVerificationRequest;
import com.ddogalmap.domain.users.dto.response.RegionVerificationResponse;
import com.ddogalmap.domain.users.entity.GpsLog;
import com.ddogalmap.domain.regions.entity.Region;
import com.ddogalmap.domain.users.entity.User;
import com.ddogalmap.domain.users.repository.GpsLogRepository;
import com.ddogalmap.domain.regions.repository.RegionRepository;
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


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionVerificationServiceImpl implements RegionVerificationService {

	private static final double MAX_ACCURACY = 50.0;
	private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

	private final UserRepository userRepository;
	private final RegionRepository regionRepository;
	private final GpsLogRepository gpsLogRepository;

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

		LocalDateTime verifiedAt = LocalDateTime.now();

		user.updateRegion(region.getEupmyeondongName());

		saveGpsLog(user, request.latitude(), request.longitude(), request.accuracy());

		log.info("[Region Verify Success] userId={}, region={}", userId, region.getEupmyeondongName());

		return new RegionVerificationResponse(
				region.getEupmyeondongName(), true, verifiedAt);
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
}