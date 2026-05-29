package com.ddogalmap.domain.restaurants.service;

import com.ddogalmap.domain.restaurants.dto.SeoulOpenApiPageResponse;
import com.ddogalmap.domain.restaurants.dto.SeoulOpenApiRow;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

/**
 * 서울 열린데이터광장 OpenAPI 호출.
 *
 * URL 패턴:
 *   http://openapi.seoul.go.kr:8088/{KEY}/json/{SERVICE}/{START_INDEX}/{END_INDEX}/
 *
 * 응답 구조 (첫 번째 키 == serviceName):
 *   {
 *     "LOCALDATA_072404": {
 *       "list_total_count": 533406,
 *       "RESULT": { "CODE": "INFO-000", "MESSAGE": "..." },
 *       "row": [ { "MGTNO": "...", "BPLCNM": "...", ... }, ... ]
 *     }
 *   }
 */
@Slf4j
@Component
public class SeoulOpenApiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String serviceName;

    public SeoulOpenApiClient(
            @Qualifier("seoulOpenApiWebClient") WebClient webClient,
            ObjectMapper objectMapper,
            @Value("${seoul-open-api.key}") String apiKey,
            @Value("${seoul-open-api.service-name}") String serviceName) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.serviceName = serviceName;
    }

    public SeoulOpenApiPageResponse fetchPage(int startIndex, int endIndex) {
        String uri = String.format("/%s/json/%s/%d/%d/", apiKey, serviceName, startIndex, endIndex);

        JsonNode response;
        try {
            response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (Exception e) {
            log.error("Seoul OpenAPI call failed at {}-{}: {}", startIndex, endIndex, e.getMessage());
            return new SeoulOpenApiPageResponse(0, Collections.emptyList());
        }

        if (response == null) {
            return new SeoulOpenApiPageResponse(0, Collections.emptyList());
        }

        JsonNode serviceNode = response.get(serviceName);
        if (serviceNode == null) {
            log.warn("Unexpected response shape: missing key '{}'. body={}", serviceName, response);
            return new SeoulOpenApiPageResponse(0, Collections.emptyList());
        }

        int totalCount = serviceNode.path("list_total_count").asInt(0);
        JsonNode rowsNode = serviceNode.get("row");

        List<SeoulOpenApiRow> rows;
        if (rowsNode == null || !rowsNode.isArray()) {
            rows = Collections.emptyList();
        } else {
            CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, SeoulOpenApiRow.class);
            rows = objectMapper.convertValue(rowsNode, listType);
        }

        return new SeoulOpenApiPageResponse(totalCount, rows);
    }
}
