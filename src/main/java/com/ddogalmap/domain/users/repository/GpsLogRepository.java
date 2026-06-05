package com.ddogalmap.domain.users.repository;

import com.ddogalmap.domain.users.entity.GpsLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GpsLogRepository extends JpaRepository<GpsLog, Long> {
}
