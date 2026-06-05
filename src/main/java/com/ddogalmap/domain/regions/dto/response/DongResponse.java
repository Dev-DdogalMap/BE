package com.ddogalmap.domain.regions.dto.response;

import com.ddogalmap.domain.regions.entity.Region;

public record DongResponse(
        Long regionId,
        String legalCode,
        String eupmyeondongName
) {

    public static DongResponse from(
            Region region
    ) {
        return new DongResponse(
                region.getRegionId(),
                region.getLegalCode(),
                region.getEupmyeondongName()
        );
    }
}
