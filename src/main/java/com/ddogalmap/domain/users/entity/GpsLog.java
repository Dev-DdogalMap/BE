package com.ddogalmap.domain.users.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@EnableJpaAuditing
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "gps_logs")
public class GpsLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	private Double accuracy;

	@Column(columnDefinition = "geometry(POINT,4326)")
	private Point geom;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;


	public GpsLog(
			User user,
			Point geom,
			Double accuracy
	) {
		this.user = user;
		this.geom = geom;
		this.accuracy = accuracy;
	}
}