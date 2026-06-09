package com.ddogalmap.domain.regions.repository;

import com.ddogalmap.domain.regions.dto.projection.RegionLiteProjection;
import com.ddogalmap.domain.regions.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

	List<Region> findAllByOrderBySidoNameAscSigunguNameAscEupmyeondongNameAsc();

	/**
	 * 지역 트리 조회용. 무거운 geom 컬럼(MultiPolygon) 제외, 응답 DTO 에 필요한 5개 컬럼만 SELECT.
	 */
	@Query("""
        SELECT r.regionId AS regionId,
               r.legalCode AS legalCode,
               r.sidoName AS sidoName,
               r.sigunguName AS sigunguName,
               r.eupmyeondongName AS eupmyeondongName
        FROM Region r
        ORDER BY r.sidoName, r.sigunguName, r.eupmyeondongName
    """)
	List<RegionLiteProjection> findAllForTree();

	@Query(value = """
        SELECT r.*
        FROM regions r
        WHERE ST_Covers(
            r.geom,
            ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)
        )
        ORDER BY
            CASE
                WHEN ST_Contains(r.geom, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)) THEN 0
                ELSE 1
            END,
            r.region_id
        LIMIT 1
    """, nativeQuery = true)
	Optional<Region> findRegionByPoint(@Param("lat") double lat, @Param("lng") double lng);

}
