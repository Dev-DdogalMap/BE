package com.ddogalmap.domain.users.service;

import com.ddogalmap.domain.users.dto.projection.UserStatsProjection;
import com.ddogalmap.domain.users.dto.response.UserStatsResponse;
import com.ddogalmap.domain.users.repository.UserRepository;
import com.ddogalmap.domain.users.service.UserStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatsServiceImpl implements UserStatsService {

	private final UserRepository userRepository;

	@Override
	public UserStatsResponse getMyStats(Long userId) {

		log.info("[UserStats] START userId={}", userId);

		UserStatsProjection projection =
				userRepository.getUserStats(userId);

		UserStatsResponse response =
				UserStatsResponse.from(projection);

		log.info("[UserStats] END userId={} visit={} review={} bookmark={} chatRoom={}",
				userId,
				response.visitCount(),
				response.reviewCount(),
				response.bookmarkCount(),
				response.chatRoomCount()
		);

		return response;
	}
}