package com.ddogalmap.domain.regions.service;

import com.ddogalmap.domain.regions.dto.response.RegionTreeResponse;

import java.util.List;

public interface RegionService {
    List<RegionTreeResponse> getRegionTree();
}
