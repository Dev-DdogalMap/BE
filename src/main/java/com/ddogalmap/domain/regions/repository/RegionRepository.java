package com.ddogalmap.domain.regions.repository;

import com.ddogalmap.domain.regions.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

	List<Region> findAllByOrderBySidoNameAscSigunguNameAscEupmyeondongNameAsc();

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
