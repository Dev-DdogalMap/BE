package com.ddogalmap.domain.restaurants.repository;

import com.ddogalmap.domain.restaurants.dto.RestaurantInsertParam;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 1,000건 단위 multi-value INSERT.
 *
 * - JPA를 우회하여 JdbcTemplate 으로 직접 SQL 실행 (IDENTITY 전략 + Hibernate batch 불가 회피)
 * - 한 번의 SQL 호출로 N건 INSERT → JDBC round-trip 1/N
 * - 좌표는 ST_Transform 으로 EPSG:2097 → 4326 변환을 같은 SQL 안에서 처리
 *   (x 또는 y 가 NULL 이면 ST_MakePoint 가 NULL 반환 → location 도 NULL)
 * - ON CONFLICT (management_no) DO NOTHING 으로 중복 행은 자동 스킵
 */
@Repository
@RequiredArgsConstructor
public class RestaurantBatchInsertRepository {

    private static final String INSERT_PREFIX =
            "INSERT INTO ddogalmap_schema.restaurants " +
            "(management_no, place_name, food_type_id, phone, " +
            "address_name, road_address_name, x, y, location, place_url, " +
            "created_at, updated_at) VALUES ";

    private static final String VALUES_TEMPLATE =
            "(?, ?, ?, ?, ?, ?, ?, ?, " +
            "ST_Transform(ST_SetSRID(ST_MakePoint(?, ?), 2097), 4326)::geography, " +
            "?, NOW(), NOW())";

    private static final String ON_CONFLICT_SUFFIX = " ON CONFLICT (management_no) DO NOTHING";

    private final JdbcTemplate jdbcTemplate;

    public int batchInsert(List<RestaurantInsertParam> batch) {
        if (batch.isEmpty()) return 0;

        StringBuilder sql = new StringBuilder(INSERT_PREFIX);
        List<Object> params = new ArrayList<>(batch.size() * 11);

        for (int i = 0; i < batch.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append(VALUES_TEMPLATE);

            RestaurantInsertParam r = batch.get(i);
            params.add(r.managementNo());
            params.add(r.placeName());
            params.add(r.foodTypeId());
            params.add(r.phone());
            params.add(r.addressName());
            params.add(r.roadAddressName());
            params.add(r.x());
            params.add(r.y());
            // ST_MakePoint 의 두 인자 (x, y 동일 값 재전송)
            params.add(r.x());
            params.add(r.y());
            params.add(r.placeUrl());
        }
        sql.append(ON_CONFLICT_SUFFIX);

        return jdbcTemplate.update(sql.toString(), params.toArray());
    }
}
