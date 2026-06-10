package com.ddogalmap.domain.regions.dto.projection;

/**
 * 지역 트리 조회용 가벼운 프로젝션.
 * 응답 DTO 에 들어가는 5개 컬럼만 SELECT 하여 무거운 geom 컬럼(MultiPolygon)을 제외.
 */
public interface RegionLiteProjection {
    Long getRegionId();
    String getLegalCode();
    String getSidoName();
    String getSigunguName();
    String getEupmyeondongName();
}
