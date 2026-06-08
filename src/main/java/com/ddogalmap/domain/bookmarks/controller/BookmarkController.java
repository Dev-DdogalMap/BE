package com.ddogalmap.domain.bookmarks.controller;

import com.ddogalmap.domain.bookmarks.dto.request.CreateBookmarkRequest;
import com.ddogalmap.domain.bookmarks.dto.response.*;
import com.ddogalmap.domain.bookmarks.service.BookmarkCommandService;
import com.ddogalmap.domain.bookmarks.service.BookmarkQueryService;
import com.ddogalmap.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Bookmark", description = "즐겨찾기/나의 맛집 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmarks")
@SecurityRequirement(name = "bearerAuth")
public class BookmarkController {

    private final BookmarkQueryService bookmarkQueryService;
    private final BookmarkCommandService bookmarkCommandService;

    @Operation(
            summary = "맛집 북마크 저장",
            description = """
                    로그인한 사용자가 특정 맛집을 선택한 즐겨찾기 폴더에 저장합니다.

                    요청한 폴더가 현재 로그인한 사용자의 폴더인지 검증한 뒤 저장합니다.
                    같은 사용자가 같은 맛집을 중복 저장할 수 없습니다.
                    """
    )
    @PostMapping
    public CreateBookmarkResponse createBookmark(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody CreateBookmarkRequest request
    ) {
        return bookmarkCommandService.createBookmark(userPrincipal.userId(), request);
    }

    @Operation(
            summary = "맛집의 폴더별 저장 상태 조회",
            description = """
                로그인한 사용자의 즐겨찾기 폴더 목록을 조회하면서,
                특정 맛집이 각 폴더에 저장되어 있는지 함께 반환합니다.

                구글맵처럼 저장됨(n) 상태와 폴더 체크 모달을 구성할 때 사용합니다.
                """
    )
    @GetMapping("/restaurants/{restaurantId}/bookmarkcategories")
    public List<BookmarkCategoryStatusResponse> getBookmarkCategoryStatuses(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long restaurantId
    ) {
        return bookmarkQueryService.getBookmarkCategoryStatuses(
                userPrincipal.userId(),
                restaurantId
        );
    }

    @Operation(
            summary = "북마크 카테고리 내 맛집 목록 조회",
            description = "로그인한 사용자가 생성한 특정 북마크 폴더에 저장한 맛집 목록을 조회합니다."
    )
    @GetMapping("/{bookmarkCategoryId}/restaurants")
    public ResponseEntity<BookmarkCategoryRestaurantsResponse> getBookmarkCategoryRestaurants(
            @PathVariable Long bookmarkCategoryId,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        return ResponseEntity.ok(
                bookmarkQueryService.getBookmarkCategoryRestaurants(
                        user.userId(),
                        bookmarkCategoryId
                )
        );
    }


}