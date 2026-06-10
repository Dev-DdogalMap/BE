package com.ddogalmap.domain.restaurants.service;

import com.ddogalmap.domain.restaurants.dto.projection.RestaurantInfoProjection;
import com.ddogalmap.domain.restaurants.dto.projection.RestaurantPreviewProjection;
import com.ddogalmap.domain.restaurants.dto.response.RestaurantInfoResponse;
import com.ddogalmap.domain.restaurants.dto.response.RestaurantMapResponse;
import com.ddogalmap.domain.restaurants.dto.response.RestaurantPreviewResponse;
import com.ddogalmap.domain.restaurants.repository.RestaurantRepository;
import com.ddogalmap.domain.reviews.repository.ReviewImgRepository;
import com.ddogalmap.domain.reviews.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImgRepository reviewImgRepository;

    /**
     * 현재 지도 화면 영역 내 식당 목록을 조회한다.
     *
     * @param swLat 남서쪽 위도
     * @param swLng 남서쪽 경도
     * @param neLat 북동쪽 위도
     * @param neLng 북동쪽 경도
     * @param limit 식당 개수
     * @return 지도에 표시할 식당 목록
     */
    @Override
    public RestaurantMapResponse getRestaurantsOnMap(double swLat, double swLng, double neLat, double neLng, int limit) {
        log.info("[RestaurantService] 지도 식당 조회 요청 - limit: {}", limit);

        if (swLat >= neLat || swLng >= neLng) {
            throw new IllegalArgumentException("잘못된 지도 영역입니다.");
        }

        RestaurantMapResponse response = RestaurantMapResponse.from(
                restaurantRepository.findRestaurantsInBounds(swLat, swLng, neLat, neLng, limit)
        );

        log.info("[RestaurantService] 지도 식당 조회 완료 - 조회 건수: {}", response.restaurants().size());

        return response;
    }

    /**
     * 식당 미리보기 정보를 조회한다.
     *
     * @param restaurantId 식당 ID
     * @param lat 사용자 현재 위도
     * @param lng 사용자 현재 경도
     * @return 식당 미리보기 정보
     */
    @Override
    public RestaurantPreviewResponse getRestaurantPreview(Long restaurantId, Double lat, Double lng) {
        log.info("[RestaurantService] 식당 미리보기 조회 요청 - restaurantId: {}", restaurantId);

        RestaurantPreviewProjection projection =
                restaurantRepository.findRestaurantPreview(restaurantId, lat, lng)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 식당입니다."));
        RestaurantExtraInfo extraInfo = getRestaurantExtraInfo(restaurantId);

        RestaurantPreviewResponse response =
                RestaurantPreviewResponse.from(projection, extraInfo.imageUrl, extraInfo.topTags);

        log.info("[RestaurantService] 식당 미리보기 조회 완료 - restaurantId: {}, reviewCount: {}", response.restaurantId(), response.reviewCount());

        return response;
	}

    @Override
    public RestaurantInfoResponse getRestaurantInfo(Long restaurantId, Double lat, Double lng) {

        log.info("[RestaurantService] 식당 정보 조회 요청 - restaurantId: {}", restaurantId);

        RestaurantInfoProjection projection = restaurantRepository.findRestaurantInfo(restaurantId, lat, lng)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 식당입니다."));

        RestaurantExtraInfo extraInfo = getRestaurantExtraInfo(restaurantId);

		RestaurantInfoResponse response = RestaurantInfoResponse.from(projection, extraInfo.imageUrl, extraInfo.topTags);

        log.info("[RestaurantService] 식당 정보 조회 완료 - restaurantId: {}, placeName: {}", response.restaurantId(), response.placeName());

        return response;
    }

    private RestaurantExtraInfo getRestaurantExtraInfo(Long restaurantId) {
        String imageUrl = reviewImgRepository.findTopLikedReviewImageUrl(restaurantId)
                .orElse(null);

        List<String> topTags = reviewRepository.findTop3TagsByRestaurantId(restaurantId);

        return new RestaurantExtraInfo(imageUrl, topTags);
    }

    private record RestaurantExtraInfo(
            String imageUrl,
            List<String> topTags
    ) {
    }
}
