package com.ddogalmap.domain.restaurants.service;

import com.ddogalmap.domain.foodtypes.service.FoodTypeService;
import com.ddogalmap.domain.restaurants.dto.GeneralRestaurantsItem;
import com.ddogalmap.domain.restaurants.dto.GeneralRestaurantsPageResponse;
import com.ddogalmap.domain.restaurants.dto.ImportResult;
import com.ddogalmap.domain.restaurants.dto.RestaurantInsertParam;
import com.ddogalmap.domain.restaurants.dto.SeoulOpenApiPageResponse;
import com.ddogalmap.domain.restaurants.dto.SeoulOpenApiRow;
import com.ddogalmap.domain.restaurants.repository.RestaurantBatchInsertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 서울 열린데이터광장에서 음식점을 받아와 DB에 적재.
 *
 * - 1,000건씩 페이지 호출 (서울 OpenAPI 한 호출당 최대 1,000건 제한)
 * - 영업/정상(01) + 휴업(02) 만 적재, 폐업(03) 등은 스킵
 * - food_type 자동 생성 (캐시 활용)
 * - JdbcTemplate batch INSERT 로 1,000건 단위 multi-value INSERT
 * - 호출 사이 100ms sleep (rate limit 대응)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantImportService {

    private static final int PAGE_SIZE = 1000;
    private static final int NATIONAL_PAGE_SIZE = 100;
    private static final long INTER_CALL_SLEEP_MS = 100L;
    private static final String STATE_OPERATING = "01";
    private static final String STATE_TEMP_CLOSED = "02";

    private final SeoulOpenApiClient apiClient;
    private final GeneralRestaurantsApiClient generalRestaurantsApiClient;
    private final FoodTypeService foodTypeService;
    private final RestaurantBatchInsertRepository batchInsertRepository;

    public ImportResult importAll() {
        long start = System.currentTimeMillis();
        int foodTypesBefore = foodTypeService.getCacheSize();

        int totalCount = -1;
        int totalFetched = 0;
        int totalInserted = 0;
        int skippedByState = 0;

        int startIndex = 1;
        while (true) {
            int endIndex = startIndex + PAGE_SIZE - 1;
            SeoulOpenApiPageResponse page = apiClient.fetchPage(startIndex, endIndex);
            if (totalCount == -1) {
                totalCount = page.totalCount();
                log.info("Seoul OpenAPI list_total_count = {}", totalCount);
            }

            List<SeoulOpenApiRow> rows = page.rows();
            if (rows.isEmpty()) break;
            totalFetched += rows.size();

            List<RestaurantInsertParam> batch = new ArrayList<>(rows.size());
            for (SeoulOpenApiRow row : rows) {
                if (!isOperating(row.stateCode())) {
                    skippedByState++;
                    continue;
                }
                batch.add(toInsertParam(row));
            }

            if (!batch.isEmpty()) {
                int inserted = batchInsertRepository.batchInsert(batch);
                totalInserted += inserted;
            }

            log.info("page {}-{}: fetched={}, inserted total={}, skippedState total={}",
                    startIndex, endIndex, rows.size(), totalInserted, skippedByState);

            if (rows.size() < PAGE_SIZE) break;
            startIndex += PAGE_SIZE;
            sleepQuietly(INTER_CALL_SLEEP_MS);
        }

        long elapsed = System.currentTimeMillis() - start;
        int foodTypesCreated = foodTypeService.getCacheSize() - foodTypesBefore;
        log.info("import done. totalCount={}, fetched={}, inserted={}, skippedState={}, " +
                        "foodTypesCreated={}, elapsedMs={}",
                totalCount, totalFetched, totalInserted, skippedByState, foodTypesCreated, elapsed);

        return new ImportResult(totalCount, totalFetched, totalInserted, skippedByState,
                foodTypesCreated, elapsed);
    }

    /**
     * 행정안전부 식품_일반음식점 조회서비스(전국)에서 음식점 데이터를 받아와 DB에 적재.
     * - 영업/정상(01) 만 적재 (휴업/폐업 제외)
     * - x, y 좌표 둘 다 있는 행만 적재
     * - 기존 서울 데이터와 통합 (management_no 기준 ON CONFLICT 자동 스킵)
     *
     * @param startPage 시작 페이지 번호 (1부터). 일일 트래픽 한도로 중단된 후 이어서 받을 때 사용.
     */
    public ImportResult importAllNational(int startPage) {
        long start = System.currentTimeMillis();
        int foodTypesBefore = foodTypeService.getCacheSize();

        int totalCount = -1;
        int totalFetched = 0;
        int totalInserted = 0;
        int skippedByState = 0;
        int skippedByCoord = 0;

        int pageNo = startPage < 1 ? 1 : startPage;
        while (true) {
            GeneralRestaurantsPageResponse page = generalRestaurantsApiClient.fetchPage(pageNo, NATIONAL_PAGE_SIZE);
            if (totalCount == -1) {
                totalCount = page.totalCount();
                log.info("General restaurants API totalCount = {}", totalCount);
            }

            List<GeneralRestaurantsItem> items = page.items();
            if (items.isEmpty()) break;
            totalFetched += items.size();

            List<RestaurantInsertParam> batch = new ArrayList<>(items.size());
            for (GeneralRestaurantsItem item : items) {
                if (!isOperatingOnly(item.stateCode())) {
                    skippedByState++;
                    continue;
                }
                Double x = parseDouble(item.x());
                Double y = parseDouble(item.y());
                if (x == null || y == null) {
                    skippedByCoord++;
                    continue;
                }
                batch.add(toInsertParamFromNational(item, x, y));
            }

            if (!batch.isEmpty()) {
                int inserted = batchInsertRepository.batchInsert(batch);
                totalInserted += inserted;
            }

            log.info("national page {}: fetched={}, inserted total={}, skippedState total={}, skippedCoord total={}",
                    pageNo, items.size(), totalInserted, skippedByState, skippedByCoord);

            if (items.isEmpty()) break;
            if (totalCount > 0 && totalFetched >= totalCount) break;
            pageNo++;
            sleepQuietly(INTER_CALL_SLEEP_MS);
        }

        long elapsed = System.currentTimeMillis() - start;
        int foodTypesCreated = foodTypeService.getCacheSize() - foodTypesBefore;
        log.info("national import done. totalCount={}, fetched={}, inserted={}, skippedState={}, " +
                        "skippedCoord={}, foodTypesCreated={}, elapsedMs={}",
                totalCount, totalFetched, totalInserted, skippedByState, skippedByCoord, foodTypesCreated, elapsed);

        return new ImportResult(totalCount, totalFetched, totalInserted,
                skippedByState + skippedByCoord, foodTypesCreated, elapsed);
    }

    private RestaurantInsertParam toInsertParamFromNational(GeneralRestaurantsItem item, Double x, Double y) {
        Long foodTypeId = foodTypeService.getOrCreate(item.foodType());
        return new RestaurantInsertParam(
                trimToNull(item.managementNo()),
                trimToNull(item.placeName()),
                foodTypeId,
                trimToNull(item.phone()),
                trimToNull(item.addressName()),
                trimToNull(item.roadAddressName()),
                x,
                y,
                trimToNull(item.homepage())
        );
    }

    /**
     * 영업/정상(01) 만 통과 (휴업/폐업 제외) — 전국 import 용.
     */
    private boolean isOperatingOnly(String stateCode) {
        return STATE_OPERATING.equals(stateCode);
    }

    private boolean isOperating(String stateCode) {
        return STATE_OPERATING.equals(stateCode) || STATE_TEMP_CLOSED.equals(stateCode);
    }

    private RestaurantInsertParam toInsertParam(SeoulOpenApiRow row) {
        Long foodTypeId = foodTypeService.getOrCreate(row.foodType());
        return new RestaurantInsertParam(
                trimToNull(row.managementNo()),
                trimToNull(row.placeName()),
                foodTypeId,
                trimToNull(row.phone()),
                trimToNull(row.addressName()),
                trimToNull(row.roadAddressName()),
                parseDouble(row.x()),
                parseDouble(row.y()),
                trimToNull(row.homepage())
        );
    }

    private Double parseDouble(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        try {
            return Double.parseDouble(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
