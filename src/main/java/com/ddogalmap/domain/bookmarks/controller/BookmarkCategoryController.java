package com.ddogalmap.domain.bookmarks.controller;

import com.ddogalmap.domain.bookmarks.dto.request.CreateBookmarkCategoryRequest;
import com.ddogalmap.domain.bookmarks.dto.response.BookmarkCategoryResponse;
import com.ddogalmap.domain.bookmarks.dto.response.BookmarkRestaurantResponse;
import com.ddogalmap.domain.bookmarks.service.BookmarkCommandService;
import com.ddogalmap.domain.bookmarks.service.BookmarkQueryService;
import com.ddogalmap.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "BookmarkCategory", description = "즐겨찾기 카테고리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmark-categories")
@SecurityRequirement(name = "bearerAuth")
public class BookmarkCategoryController {

    private final BookmarkQueryService bookmarkQueryService;
    private final BookmarkCommandService bookmarkCommandService;

    @Operation(
            summary = "내 즐겨찾기 카테고리 목록 조회",
            description = """
                    로그인한 사용자의 즐겨찾기 카테고리 목록을 조회합니다.
                    
                    신규 가입 시 자동 생성된 기본 폴더와 사용자가 추가한 폴더들이 함께 반환됩니다.
                    각 카테고리에는 카테고리명, 정렬 순서, 기본 폴더 여부, 해당 카테고리에 저장된 맛집 개수가 포함됩니다.
                    """
    )
    @GetMapping
    public List<BookmarkCategoryResponse> getMyBookmarkCategories(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return bookmarkQueryService.getMyBookmarkCategories(userPrincipal.userId());
    }

    @Operation(
            summary = "즐겨찾기 카테고리 생성",
            description = """
                로그인한 사용자가 새로운 즐겨찾기 카테고리를 생성합니다.

                화면에서는 카테고리를 폴더처럼 표시할 수 있습니다.
                카테고리명은 사용자별로 중복될 수 없습니다.
                생성된 카테고리는 기존 카테고리 목록의 마지막 순서로 추가됩니다.
                """
    )
    @PostMapping
    public BookmarkCategoryResponse createBookmarkCategory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody CreateBookmarkCategoryRequest request
    ) {
        return bookmarkCommandService.createBookmarkCategory(
                userPrincipal.userId(),
                request
        );
    }

    @Operation(
            summary = "카테고리별 북마크 맛집 조회",
            description = """
                    로그인한 사용자의 특정 즐겨찾기 카테고리에 저장된 맛집 목록을 조회합니다.
                    
                    요청한 카테고리가 현재 로그인한 사용자의 카테고리인지 검증한 뒤,
                    해당 카테고리에 포함된 북마크 맛집을 최신 저장순으로 반환합니다.
                    """
    )
    @GetMapping("/{bookmarkCategoryId}/bookmarks")
    public List<BookmarkRestaurantResponse> getMyBookmarksByCategory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long bookmarkCategoryId
    ) {
        return bookmarkQueryService.getMyBookmarksByCategory(
                userPrincipal.userId(),
                bookmarkCategoryId
        );
    }

    @Operation(
            summary = "특정 폴더에서 맛집 북마크 제거",
            description = """
                로그인한 사용자의 특정 즐겨찾기 폴더에서 맛집 북마크를 제거합니다.

                같은 맛집을 여러 폴더에 저장할 수 있으므로,
                restaurantId만으로 삭제하지 않고 폴더 ID와 식당 ID를 함께 사용합니다.
                """
    )
    @DeleteMapping("/{bookmarkCategoryId}/restaurants/{restaurantId}")
    public void deleteBookmarkFromCategory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long bookmarkCategoryId,
            @PathVariable Long restaurantId
    ) {
        bookmarkCommandService.deleteBookmarkFromCategory(
                userPrincipal.userId(),
                bookmarkCategoryId,
                restaurantId
        );
    }

    @Operation(
            summary = "즐겨찾기 카테고리 삭제",
            description = """
                로그인한 사용자가 생성한 즐겨찾기 카테고리를 삭제합니다.

                기본 카테고리는 삭제할 수 없습니다.
                카테고리를 삭제하면 해당 카테고리에 저장된 북마크도 함께 삭제됩니다.
                """
    )
    @DeleteMapping("/{bookmarkCategoryId}")
    public void deleteBookmarkCategory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long bookmarkCategoryId
    ) {
        bookmarkCommandService.deleteBookmarkCategory(
                userPrincipal.userId(),
                bookmarkCategoryId
        );
    }
}
