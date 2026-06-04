package com.ddogalmap.domain.restaurants.service;

import com.ddogalmap.domain.restaurants.dto.GeneralRestaurantsItem;
import com.ddogalmap.domain.restaurants.dto.GeneralRestaurantsPageResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

/**
 * 행정안전부 식품_일반음식점 조회서비스 OpenAPI 호출.
 *
 * Endpoint:
 *   GET https://apis.data.go.kr/1741000/general_restaurants/info
 *       ?serviceKey={KEY}&pageNo={PAGE}&numOfRows={SIZE}&type=json
 *
 * 응답 구조:
 *   {
 *     "response": {
 *       "header": { "resultCode": "0", "resultMsg": "정상" },
 *       "body": {
 *         "dataType": "JSON",
 *         "items": { "item": [ { "MNG_NO": "...", "BPLC_NM": "...", ... }, ... ] },
 *         "numOfRows": 1000,
 *         "pageNo": 1,
 *         "totalCount": 2278469
 *       }
 *     }
 *   }
 *
 * 좌표계: EPSG:5174 (Bessel 중부원점 TM)
 */
@Slf4j
@Component
public class GeneralRestaurantsApiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String serviceKey;
    private final String servicePath;

    public GeneralRestaurantsApiClient(
            @Qualifier("generalRestaurantsWebClient") WebClient webClient,
            ObjectMapper objectMapper,
            @Value("${general-restaurants-api.service-key}") String serviceKey,
            @Value("${general-restaurants-api.service-path}") String servicePath) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.serviceKey = serviceKey;
        this.servicePath = servicePath;
    }

    public GeneralRestaurantsPageResponse fetchPage(int pageNo, int numOfRows) {
        String uri = UriComponentsBuilder.fromPath(servicePath)
                .queryParam("serviceKey", serviceKey)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", numOfRows)
                .queryParam("type", "json")
                .build(false)
                .toUriString();

        JsonNode response;
        try {
            response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (Exception e) {
            log.error("General restaurants API call failed at page {} size {}: {}",
                    pageNo, numOfRows, e.getMessage());
            throw new RuntimeException(
                    "General restaurants API call failed at page " + pageNo, e);
        }

        if (response == null) {
            return new GeneralRestaurantsPageResponse(0, Collections.emptyList());
        }

        JsonNode body = response.path("response").path("body");
        if (body.isMissingNode()) {
            log.warn("Unexpected response shape: missing response.body. body={}", response);
            return new GeneralRestaurantsPageResponse(0, Collections.emptyList());
        }

        int totalCount = body.path("totalCount").asInt(0);
        JsonNode itemsNode = body.path("items").path("item");

        List<GeneralRestaurantsItem> items;
        if (itemsNode.isMissingNode() || !itemsNode.isArray()) {
            items = Collections.emptyList();
        } else {
            CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, GeneralRestaurantsItem.class);
            items = objectMapper.convertValue(itemsNode, listType);
        }

        return new GeneralRestaurantsPageResponse(totalCount, items);
    }
}
