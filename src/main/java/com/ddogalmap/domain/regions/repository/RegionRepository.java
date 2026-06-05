package com.ddogalmap.domain.regions.repository;

import com.ddogalmap.domain.regions.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findAllByOrderBySidoNameAscSigunguNameAscEupmyeondongNameAsc();
}
