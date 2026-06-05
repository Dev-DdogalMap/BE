package com.ddogalmap.domain.regions.dto.response;

import java.util.List;

public record RegionTreeResponse(
        String sidoName,
        List<SigunguResponse> sigungus
) {

    public static RegionTreeResponse of(
            String sidoName,
            List<SigunguResponse> sigungus
    ) {
        return new RegionTreeResponse(
                sidoName,
                sigungus
        );
    }
}
