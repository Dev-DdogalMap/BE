package com.ddogalmap.domain.users.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@EnableJpaAuditing
@Table(name = "regions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long regionId;

	@Column(nullable = false, unique = true, length = 10)
	private String legalCode;

	@Column(nullable = false, length = 50)
	private String sidoName;

	@Column(length = 50)
	private String sigunguName;

	@Column(length = 50)
	private String eupmyeondongName;

	@Column(columnDefinition = "geometry(MultiPolygon,4326)")
	private MultiPolygon geom;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;
}