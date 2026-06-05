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
        WHERE ST_Contains(
            r.geom,
            ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)
        )
        LIMIT 1
    """, nativeQuery = true)
    Optional<com.ddogalmap.domain.regions.entity.Region> findRegionByPoint(@Param("lat") double lat, @Param("lng") double lng);
}
