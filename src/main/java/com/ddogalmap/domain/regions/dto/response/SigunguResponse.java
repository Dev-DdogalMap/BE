package com.ddogalmap.domain.regions.dto.response;

import com.ddogalmap.domain.regions.entity.Region;

import java.util.List;


public record SigunguResponse(
        String sigunguName,
        List<DongResponse> dongs
) {

    public static SigunguResponse of(
            String sigunguName,
            List<Region> regions
    ) {
        return new SigunguResponse(
                sigunguName,
                regions.stream()
                        .map(DongResponse::from)
                        .toList()
        );
    }
}
