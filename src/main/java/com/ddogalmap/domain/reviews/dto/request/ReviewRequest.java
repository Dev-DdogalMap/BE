package com.ddogalmap.domain.reviews.dto.request;

import jakarta.validation.constraints.*;
import lombok.Setter;

import java.util.List;

public record ReviewRequest(
        @NotNull(message = "식당 ID는 필수입니다.")
        Long restaurantId,

        @NotNull(message = "별점은 필수 입력 항목입니다.")
        @Min(value = 1, message = "별점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "별점은 5점 이하여야 합니다.")
        Integer score,

        @NotNull(message = "재방문 여부는 필수 입력 항목입니다.")
        Boolean isRevisit,

        @NotBlank(message = "후기 내용은 필수 입력 항목입니다.")
        String content,

        @NotNull(message = "태그는 필수입니다.")
        @Size(min = 1, max = 3, message = "태그는 최소 1개, 최대 3개 선택해야 합니다.")
        List<String> tags // 프론트엔드에서 ["데이트", "혼밥 가능"] 형태로 받기 위한 필드 추가
) {
}