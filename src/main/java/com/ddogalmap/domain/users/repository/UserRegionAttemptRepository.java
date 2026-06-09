package com.ddogalmap.domain.users.repository;

import com.ddogalmap.domain.users.entity.UserRegionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRegionAttemptRepository extends JpaRepository<UserRegionAttempt, Long> {

	List<UserRegionAttempt> findTop3ByUser_UserIdOrderByCreatedAtDesc(Long userId);
}