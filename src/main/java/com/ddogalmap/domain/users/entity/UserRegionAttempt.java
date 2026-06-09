package com.ddogalmap.domain.users.entity;

import com.ddogalmap.domain.regions.entity.Region;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;

@Entity
@EnableJpaAuditing
@Table(name = "user_region_attempts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRegionAttempt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userRegionAttemptId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "region_id", nullable = false)
	private Region region;

	private Double latitude;
	private Double longitude;
	private Double accuracy;

	private LocalDateTime createdAt = LocalDateTime.now();

	public UserRegionAttempt(User user,
							 Region region,
							 Double latitude,
							 Double longitude,
							 Double accuracy) {
		this.user = user;
		this.region = region;
		this.latitude = latitude;
		this.longitude = longitude;
		this.accuracy = accuracy;
	}
}