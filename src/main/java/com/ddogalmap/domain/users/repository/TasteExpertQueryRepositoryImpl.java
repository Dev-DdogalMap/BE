package com.ddogalmap.domain.users.repository;

import com.ddogalmap.domain.users.dto.TasteExpertSearchCondition;
import com.ddogalmap.domain.users.dto.response.TasteExpertPageResponse;
import com.ddogalmap.domain.users.dto.response.TasteExpertResponse;
import com.ddogalmap.domain.users.enumtype.TasteExpertSortType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class TasteExpertQueryRepositoryImpl implements TasteExpertQueryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Value("${spring.jpa.properties.hibernate.default_schema}")
    private String schema;

    @Override
    public TasteExpertPageResponse search(TasteExpertSearchCondition condition) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("keyword", likeKeyword(condition.keyword()))
                .addValue("region", emptyToNull(condition.region()))
                .addValue("minLevel", condition.minLevel());

        String baseFromWhere = """
                from %1$s.users u
                left join %1$s.user_levels ul on ul.user_id = u.user_id
                left join %1$s.levels l on l.level_id = ul.level_id
                left join %1$s.reviews r on r.user_id = u.user_id
                left join %1$s.visit_verification vv on vv.user_id = u.user_id
                where (cast(:keyword as text) is null or u.nickname ilike cast(:keyword as text))
                    and (cast(:region as text) is null or u.region = cast(:region as text))
                    and (cast(:minLevel as integer) is null or l.level >= cast(:minLevel as integer))
                """.formatted(schema);

        String select = """
                select
                    u.user_id as user_id,
                    u.nickname as nickname,
                    u.profile_image_url as profile_image_url,
                    u.region as region,
                    coalesce(l.level, 0) as level,
                    coalesce(l.name, '레벨 미설정') as level_name,
                    coalesce(ul.exp, 0) as exp,
                    count(distinct r.review_id) as review_count,
                    count(distinct vv.visit_verification_id) as visit_verification_count,
                    coalesce(avg(r.score), 0) as rating_average,
                    case
                        when u.nickname ilike '%%카페%%' then '카페 전문'
                        when u.nickname ilike '%%한식%%' then '한식 전문'
                        when u.nickname ilike '%%술%%' then '술집 전문'
                        when u.nickname ilike '%%양식%%' then '양식 전문'
                        else '양식 전문'
                    end as specialty,
                    case when count(distinct vv.visit_verification_id) > 0 then true else false end as is_certified,
                    u.created_at as user_created_at
                """
                + baseFromWhere
                + """
                group by
                    u.user_id, u.nickname, u.profile_image_url, u.region,
                    l.level, l.name, ul.exp, u.created_at
                """;

        String orderBy = switch (condition.sort()) {
            case REVIEW_COUNT -> " order by review_count desc, level desc, visit_verification_count desc, rating_average desc, user_id desc ";
            case VISIT_COUNT -> " order by visit_verification_count desc, level desc, review_count desc, rating_average desc, user_id desc ";
            case RATING -> " order by rating_average desc, level desc, review_count desc, visit_verification_count desc, user_id desc ";
            case RECENT -> " order by user_created_at desc, user_id desc ";
            case EXPERTISE -> " order by level desc, review_count desc, visit_verification_count desc, rating_average desc, user_id desc ";
        };

        int offset = condition.page() * condition.size();
        params.addValue("limit", condition.size());
        params.addValue("offset", offset);

        String pageQuery = select + orderBy + " limit :limit offset :offset";
        List<TasteExpertResponse> content = jdbcTemplate.query(pageQuery, params, new TasteExpertRowMapper());

        String countQuery = "select count(*) from (" + select + ") taste_experts";
        Long totalElements = jdbcTemplate.queryForObject(countQuery, params, Long.class);
        long safeTotalElements = totalElements == null ? 0 : totalElements;
        int totalPages = safeTotalElements == 0 ? 0 : (int) Math.ceil((double) safeTotalElements / condition.size());

        return new TasteExpertPageResponse(content, condition.page(), condition.size(), safeTotalElements, totalPages);
    }

    private String likeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return "%" + keyword.trim() + "%";
    }

    private String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private static final class TasteExpertRowMapper implements RowMapper<TasteExpertResponse> {
        @Override
        public TasteExpertResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new TasteExpertResponse(
                    rs.getLong("user_id"),
                    rs.getString("nickname"),
                    rs.getString("profile_image_url"),
                    rs.getString("region"),
                    rs.getInt("level"),
                    rs.getString("level_name"),
                    rs.getLong("exp"),
                    rs.getLong("review_count"),
                    rs.getLong("visit_verification_count"),
                    rs.getDouble("rating_average"),
                    rs.getString("specialty"),
                    rs.getBoolean("is_certified")
            );
        }
    }
}
