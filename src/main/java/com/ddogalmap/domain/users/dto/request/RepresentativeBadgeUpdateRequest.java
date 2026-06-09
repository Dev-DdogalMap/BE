package com.ddogalmap.domain.users.dto.request;

import jakarta.validation.constraints.NotNull;

public record RepresentativeBadgeUpdateRequest(

        @NotNull
        Long badgeId
) {
}
