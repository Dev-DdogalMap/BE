<div>

# 🍊 또갈지도 Backend

**또갈지도**는 사용자가 실제로 방문한 맛집을 인증하고, 인증된 방문을 기반으로 리뷰를 작성하며, 사용자 간 실시간 채팅으로 로컬 맛집 경험을 공유할 수 있는 위치 기반 맛집 서비스입니다.

<br />



</div>

---

## 📌 프로젝트 소개

**또갈지도**는 사용자가 실제로 방문한 맛집을 인증하고, 인증된 방문을 기반으로 리뷰를 작성할 수 있는 위치 기반 로컬 맛집 서비스입니다.

백엔드는 카카오 로그인, JWT 인증, 맛집 지도 조회, 맛집 검색, PostGIS 기반 방문 인증, 리뷰 작성, 북마크, 동네 인증, 실시간 채팅, 레벨/뱃지 이벤트 처리를 담당합니다.

---

## 👥 Backend Team

<table align="center">
  <tr>
    <td align="center">
      <a href="https://github.com/Junbro-design">
        <img src="https://github.com/Junbro-design.png" width="100px;" alt="김준형"/>
        <br />
        <b>김준형</b>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/ss0510s">
        <img src="https://github.com/ss0510s.png" width="100px;" alt="남수진"/>
        <br />
        <b>남수진</b>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/Hojin00">
        <img src="https://github.com/Hojin00.png" width="100px;" alt="류효진"/>
        <br />
        <b>류효진</b>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/Lee-Yeonjoo">
        <img src="https://github.com/Lee-Yeonjoo.png" width="100px;" alt="이연주"/>
        <br />
        <b>이연주</b>
      </a>
    </td>
  </tr>
  <tr>
    <td align="center">
      <a href="https://github.com/viviamm7-code">
        <img src="https://github.com/viviamm7-code.png" width="100px;" alt="김진성"/>
        <br />
        <b>김진성</b>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/Eunseong33">
        <img src="https://github.com/Eunseong33.png" width="100px;" alt="이은성"/>
        <br />
        <b>이은성</b>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/tjtjdfbf">
        <img src="https://github.com/tjtjdfbf.png" width="100px;" alt="서성률"/>
        <br />
        <b>서성률</b>
      </a>
    </td>
  </tr>
</table>

---

## 🛠️ Tech Stack

| Category | Stack |
| --- | --- |
| Language | Java 21 |
| Backend Framework | Spring Boot 3.5.14 |
| Web | Spring Web, WebFlux |
| Realtime | WebSocket, STOMP |
| Security | Spring Security, JWT, OAuth 2.0 Kakao Login |
| Database | PostgreSQL 18.3, PostGIS |
| ORM | Spring Data JPA, Querydsl, Hibernate Spatial |
| File Storage | AWS S3 |
| Infra | AWS EC2, AWS RDS, Nginx, Docker, Jenkins |
| Monitoring | Spring Actuator, Micrometer, Elasticsearch, Logstash, Kibana 9.1.0 |
| Logging | Logstash Logback Encoder |
| API Docs | Springdoc OpenAPI / Swagger UI |

## 📂 프로젝트 구조

```text
src/main/java/com/ddogalmap
├── DdogalmapApplication.java
├── domain
│   ├── users
│   ├── restaurants
│   ├── reviews
│   ├── visit
│   ├── bookmarks
│   ├── foodtypes
│   ├── regions
│   ├── levels
│   └── badges
└── global
    ├── common
    ├── config
    ├── exception
    ├── infrastructure
    ├── security
    └── util
```

---

## 🏗️ Backend Architecture

<img width="1536" height="1024" alt="image" src="https://github.com/user-attachments/assets/e062f5f2-babf-478f-a94c-3dfeb369d9cd" />


---

## 🗺️ Backend ERD

<img width="788" height="522" alt="image" src="https://github.com/user-attachments/assets/fbae879a-6f03-442c-999a-a772a7b2182c" />


---


## 🧭 주요 기능

### 1. 카카오 로그인 & JWT 인증

* 카카오 OAuth 로그인 지원
* 로그인 성공 시 Refresh Token을 HttpOnly Cookie로 발급
* Access Token 만료 시 Refresh Token 기반 재발급
* Spring Security + JWT Filter 기반 인증 처리
* 인증 API와 공개 API 분리

---

### 2. 맛집 지도 조회

* 현재 지도 화면의 남서/북동 좌표를 기준으로 음식점 목록 조회
* PostGIS의 공간 연산을 활용해 지도 영역 내 음식점만 필터링
* 음식점 위치, 음식 종류, 주소 정보를 지도 마커용 응답으로 제공

---

### 3. 맛집 검색

* 검색어, 지역, 음식 종류 필터 지원
* 거리순, 맛집지수순, 별점순 정렬 지원
* 로그인 사용자의 인증 동네를 기반으로 지역 검색 자동 적용
* 음식점별 상위 태그 3개와 대표 리뷰 이미지를 함께 제공
* 음식점 ID 리스트 기반 일괄 조회로 N+1 문제 완화

---

### 4. 방문 인증

* 사용자의 현재 좌표와 음식점 좌표를 비교해 방문 여부 검증
* 음식점 기준 반경 50m 이내일 때만 방문 인증 허용
* 프론트의 거리 계산 결과를 신뢰하지 않고 백엔드에서 최종 검증
* PostGIS `ST_Distance`를 활용한 미터 단위 거리 계산

```sql
ST_Distance(
    restaurant.location,
    ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
)
```

방문 인증 성공 시 방문 인증 기록을 저장하고, 레벨 경험치 및 뱃지 이벤트를 발행합니다.

---

### 5. 방문 인증 기반 리뷰

* 방문 인증 내역이 있어야 리뷰 작성 가능
* 하나의 방문 인증에 대해 중복 리뷰 작성 방지
* 리뷰 점수, 재방문 여부, 내용, 태그, 이미지 등록 지원
* 이미지가 포함된 리뷰와 일반 리뷰를 구분해 경험치 이벤트 발행
* 리뷰 작성 후 음식점 통계 갱신 이벤트 발행

---

### 6. 북마크 / 나의 맛집

* 맛집 북마크 저장
* 사용자별 북마크 카테고리 생성
* 기본 카테고리 및 사용자 생성 카테고리 관리
* 카테고리별 저장 맛집 목록 조회
* 특정 폴더에서 맛집 북마크 제거
* 기본 카테고리 삭제 방지

---

### 7. 내 동네 인증

* 사용자의 현재 좌표를 기반으로 동네 인증 처리
* 인증된 동네 정보를 사용자 정보에 저장
* 맛집 검색 시 로그인 사용자의 인증 동네를 자동 적용 가능

---

### 8. 레벨 / 뱃지 / 활동 내역

* 방문 인증, 리뷰 작성, 사진 리뷰 작성 등의 활동에 따라 경험치 이벤트 발행
* 사용자의 레벨 정보, 대표 뱃지, 최근 획득 뱃지 조회
* 대표 뱃지 변경 지원
* 활동 상세에서 전체 뱃지 목록 및 레벨 히스토리 조회

---

### 9. 실시간 1:1 채팅 / 그룹 채팅

- WebSocket 기반 채팅 기능을 지원합니다.
- `/ws-chat/**` 경로를 통해 WebSocket 연결을 처리합니다.
- 프론트에서 WebSocket 연결 정보를 조회할 수 있도록 `/api/chats/ws-info` API를 제공합니다.
- 사용자는 마이페이지에서 채팅 수신 여부를 설정할 수 있습니다.
- Spring Security 설정에서 WebSocket 연결 경로는 별도로 허용하고, 채팅 관련 일반 API는 인증 기반으로 관리합니다.

---

## 🔐 JWT 인증 흐름

```text
1. 사용자가 카카오 로그인 요청
2. 서버가 카카오 로그인 URL로 리다이렉트
3. 카카오 인가 코드 callback 수신
4. 서버가 사용자 정보 조회 후 로그인 처리
5. Refresh Token을 HttpOnly Cookie로 저장
6. 프론트는 Access Token을 사용해 인증 API 요청
7. Access Token 만료 시 /api/auth/refresh 요청
8. 서버가 Refresh Token을 검증하고 새로운 Access Token 발급
9. 로그아웃 시 Refresh Token 삭제
```

---

## 📖 API 요약

### Auth

| Method | URI                        | Description      |
| ------ | -------------------------- | ---------------- |
| GET    | `/api/auth/kakao/login`    | 카카오 로그인 시작       |
| GET    | `/api/auth/kakao/callback` | 카카오 로그인 콜백       |
| POST   | `/api/auth/refresh`        | Access Token 재발급 |
| POST   | `/api/auth/logout`         | 로그아웃             |

### User

| Method | URI                                  | Description   |
| ------ | ------------------------------------ | ------------- |
| GET    | `/api/users/me`                      | 내 정보 조회       |
| GET    | `/api/users/me/chat-preference`      | 채팅 수신 설정 조회   |
| PATCH  | `/api/users/me/chat-preference`      | 채팅 수신 설정 변경   |
| POST   | `/api/users/me/region-verification`  | 내 동네 인증       |
| GET    | `/api/users/me/region-verification`  | 내 동네 인증 정보 조회 |
| GET    | `/api/users/me/activity`             | 내 활동 내역 조회    |
| GET    | `/api/users/me/activity/detail`      | 내 활동 상세 조회    |
| PATCH  | `/api/users/me/representative-badge` | 대표 뱃지 변경      |
| DELETE | `/api/users/me`                      | 회원 탈퇴         |

### Restaurant

| Method | URI                                       | Description    |
| ------ | ----------------------------------------- | -------------- |
| GET    | `/api/restaurants/map`                    | 지도 영역 내 음식점 조회 |
| GET    | `/api/restaurants/search`                 | 맛집 검색          |
| GET    | `/api/restaurants/{restaurantId}/preview` | 음식점 미리보기 조회    |
| GET    | `/api/restaurants/{restaurantId}/info`    | 음식점 상세 정보 조회   |

### Visit Verification

| Method | URI                             | Description |
| ------ | ------------------------------- | ----------- |
| POST   | `/api/visit/visit-verification` | 방문 인증       |

### Review

| Method | URI                                                     | Description             |
| ------ | ------------------------------------------------------- | ----------------------- |
| POST   | `/api/visit-verifications/{visitVerificationId}/review` | 방문 인증 기반 리뷰 등록          |
| GET    | `/api/restaurants/{restaurantId}/reviews`               | 음식점별 리뷰 목록 조회           |
| GET    | `/api/my/reviews`                                       | 내가 작성한 리뷰 조회            |
| GET    | `/api/my/unwritten-reviews`                             | 방문했지만 리뷰를 작성하지 않은 목록 조회 |

### Bookmark

| Method | URI                                                                        | Description         |
| ------ | -------------------------------------------------------------------------- | ------------------- |
| POST   | `/api/bookmarks`                                                           | 맛집 북마크 저장           |
| GET    | `/api/bookmarks/restaurants/{restaurantId}/bookmarkcategories`             | 맛집의 폴더별 저장 상태 조회    |
| GET    | `/api/bookmarks/{bookmarkCategoryId}/restaurants`                          | 북마크 카테고리 내 맛집 목록 조회 |
| GET    | `/api/bookmark-categories`                                                 | 내 북마크 카테고리 목록 조회    |
| POST   | `/api/bookmark-categories`                                                 | 북마크 카테고리 생성         |
| GET    | `/api/bookmark-categories/{bookmarkCategoryId}/bookmarks`                  | 카테고리별 북마크 맛집 조회     |
| DELETE | `/api/bookmark-categories/{bookmarkCategoryId}/restaurants/{restaurantId}` | 특정 폴더에서 맛집 제거       |
| DELETE | `/api/bookmark-categories/{bookmarkCategoryId}`                            | 북마크 카테고리 삭제         |

### Chat

| Method | URI | Description |
| --- | --- | --- |
| GET | `/api/chats/ws-info` | WebSocket 연결 정보 조회 |
| GET | `/api/users/me/chat-preference` | 내 채팅 수신 설정 조회 |
| PATCH | `/api/users/me/chat-preference` | 내 채팅 수신 설정 변경 |

### Common

| Method | URI                 | Description |
| ------ | ------------------- | ----------- |
| GET    | `/api/food-types`   | 음식 종류 목록 조회 |
| GET    | `/api/regions/tree` | 지역 트리 조회    |

---

## 📖 API 문서

서버 실행 후 Swagger UI에서 API 문서를 확인할 수 있습니다.

```text
http://localhost:8080/swagger-ui/index.html
```
---

## ✅ 주요 구현 포인트

* PostGIS 기반 방문 인증 거리 검증
* 프론트 1차 검증 + 백엔드 최종 검증 구조
* 카카오 OAuth + JWT 기반 인증 흐름
* Refresh Token HttpOnly Cookie 저장
* Spring Security Stateless 인증 처리
* 지도 영역 기반 맛집 조회
* 거리순 / 맛집지수순 / 별점순 맛집 검색
* 음식점 태그 및 대표 이미지 일괄 조회로 N+1 완화
* 방문 인증 기반 리뷰 작성 권한 검증
* 리뷰 작성, 방문 인증 후 레벨/뱃지/통계 이벤트 발행
* 북마크 카테고리 기반 나의 맛집 관리
* 실시간 Websocket 1:1 / 그룹 채팅
* ELK 셋업 및 로그 조회

---

<div>

**또 가고 싶은 맛집을 지도 위에 기록하다, 또갈지도 🍊**
- [팀 자료](https://app.notion.com/p/goormkdx/1-361c0ff4ce31812abd25e434af3d29ca)
- [기획 자료](https://app.notion.com/p/3671b6d49fe68020b81ed12bae3bcd83)

</div>
