-- DB 생성
CREATE DATABASE ddogalmap;

-- 앱 사용자 생성
CREATE USER ddogalmap_user WITH PASSWORD '원하는비밀번호';

-- DB 권한 부여
GRANT ALL PRIVILEGES ON DATABASE ddogalmap TO ddogalmap_user;

-- ddogalmap DB로 이동
\c ddogalmap

-- 스키마 생성
CREATE SCHEMA ddogalmap_schema AUTHORIZATION ddogalmap_user;

-- 기본 search_path 설정
ALTER DATABASE ddogalmap SET search_path TO ddogalmap_schema, public;

-- 사용자 기본 search_path 설정
ALTER USER ddogalmap_user SET search_path TO ddogalmap_schema, public;

-- 스키마 사용/생성 권한
GRANT USAGE, CREATE ON SCHEMA ddogalmap_schema TO ddogalmap_user;

-- 기존 테이블/시퀀스 권한
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA ddogalmap_schema TO ddogalmap_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA ddogalmap_schema TO ddogalmap_user;

-- 앞으로 생성될 테이블/시퀀스 기본 권한
ALTER DEFAULT PRIVILEGES IN SCHEMA ddogalmap_schema
GRANT ALL PRIVILEGES ON TABLES TO ddogalmap_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA ddogalmap_schema
GRANT ALL PRIVILEGES ON SEQUENCES TO ddogalmap_user;

-- postgis 확장
CREATE EXTENSION IF NOT EXISTS postgis;

-- ddogalmap_schema.badges definition

-- Drop table

-- DROP TABLE ddogalmap_schema.badges;

CREATE TABLE ddogalmap_schema.badges (
	badge_id bigserial NOT NULL,
	"name" varchar(255) NOT NULL,
	icon_image varchar(255) NOT NULL,
	condition_type varchar(255) NULL,
	condition_value int4 NULL,
	created_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT badges_pkey PRIMARY KEY (badge_id)
);


-- ddogalmap_schema.food_types definition

-- Drop table

-- DROP TABLE ddogalmap_schema.food_types;

CREATE TABLE ddogalmap_schema.food_types (
	food_type_id bigserial NOT NULL,
	"type" varchar(50) NOT NULL,
	created_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT food_types_pkey PRIMARY KEY (food_type_id)
);


-- ddogalmap_schema.level_policies definition

-- Drop table

-- DROP TABLE ddogalmap_schema.level_policies;

CREATE TABLE ddogalmap_schema.level_policies (
	level_policy_id bigserial NOT NULL,
	activity_type varchar(50) NOT NULL,
	"exp" int4 NOT NULL,
	created_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT level_policies_pkey PRIMARY KEY (level_policy_id)
);


-- ddogalmap_schema.levels definition

-- Drop table

-- DROP TABLE ddogalmap_schema.levels;

CREATE TABLE ddogalmap_schema.levels (
	level_id bigserial NOT NULL,
	"level" int4 NOT NULL,
	"name" varchar(100) NOT NULL,
	required_exp int4 NOT NULL,
	created_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT levels_pkey PRIMARY KEY (level_id)
);


-- ddogalmap_schema.regions definition

-- Drop table

-- DROP TABLE ddogalmap_schema.regions;

CREATE TABLE ddogalmap_schema.regions (
	region_id bigserial NOT NULL,
	legal_code varchar(10) NOT NULL,
	sido_name varchar(50) NOT NULL,
	sigungu_name varchar(50) NULL,
	eupmyeondong_name varchar(50) NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	geom ddogalmap_schema.geometry(multipolygon, 4326) NULL,
	CONSTRAINT regions_legal_code_key UNIQUE (legal_code),
	CONSTRAINT regions_pkey PRIMARY KEY (region_id),
	CONSTRAINT uk_regions_legal_code UNIQUE (legal_code)
);
CREATE INDEX regions_geom_idx ON ddogalmap_schema.regions USING gist (geom);


-- ddogalmap_schema.spatial_ref_sys definition

-- Drop table

-- DROP TABLE ddogalmap_schema.spatial_ref_sys;

CREATE TABLE ddogalmap_schema.spatial_ref_sys (
	srid int4 NOT NULL,
	auth_name varchar(256) NULL,
	auth_srid int4 NULL,
	srtext varchar(2048) NULL,
	proj4text varchar(2048) NULL,
	CONSTRAINT spatial_ref_sys_pkey PRIMARY KEY (srid),
	CONSTRAINT spatial_ref_sys_srid_check CHECK (((srid > 0) AND (srid <= 998999)))
);


-- ddogalmap_schema.badge_food_types definition

-- Drop table

-- DROP TABLE ddogalmap_schema.badge_food_types;

CREATE TABLE ddogalmap_schema.badge_food_types (
	badge_food_type_id bigserial NOT NULL,
	badge_id int8 NOT NULL,
	food_type_id int8 NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT badge_food_types_pkey PRIMARY KEY (badge_food_type_id),
	CONSTRAINT uk_badge_food_type UNIQUE (badge_id, food_type_id),
	CONSTRAINT fk_badge_food_types_badge FOREIGN KEY (badge_id) REFERENCES ddogalmap_schema.badges(badge_id),
	CONSTRAINT fk_badge_food_types_food_type FOREIGN KEY (food_type_id) REFERENCES ddogalmap_schema.food_types(food_type_id)
);


-- ddogalmap_schema.chat_rooms definition

-- Drop table

-- DROP TABLE ddogalmap_schema.chat_rooms;

CREATE TABLE ddogalmap_schema.chat_rooms (
	chat_room_id bigserial NOT NULL,
	room_name varchar(100) NULL,
	region varchar(50) NULL,
	participant_count int4 NULL,
	food_type_id int8 NULL,
	created_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	max_participant_count int4 NULL,
	image_url varchar(255) NULL,
	CONSTRAINT chat_rooms_pkey PRIMARY KEY (chat_room_id),
	CONSTRAINT fk_chat_room_food_type FOREIGN KEY (food_type_id) REFERENCES ddogalmap_schema.food_types(food_type_id)
);


-- ddogalmap_schema.restaurants definition

-- Drop table

-- DROP TABLE ddogalmap_schema.restaurants;

CREATE TABLE ddogalmap_schema.restaurants (
	restaurant_id bigserial NOT NULL,
	place_name varchar(250) NOT NULL,
	food_type_id int8 NOT NULL,
	phone varchar(30) NULL,
	address_name varchar(250) NULL,
	road_address_name varchar(250) NULL,
	x float8 NULL,
	y float8 NULL,
	"location" ddogalmap_schema.geography(point, 4326) NULL,
	place_url varchar(250) NULL,
	created_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	management_no varchar(50) NULL,
	CONSTRAINT restaurants_pkey PRIMARY KEY (restaurant_id),
	CONSTRAINT uq_restaurants_management_no UNIQUE (management_no),
	CONSTRAINT fk_restaurant_food_type FOREIGN KEY (food_type_id) REFERENCES ddogalmap_schema.food_types(food_type_id)
);
CREATE INDEX idx_restaurants_address_name ON ddogalmap_schema.restaurants USING btree (address_name);
CREATE INDEX idx_restaurants_address_trgm ON ddogalmap_schema.restaurants USING gin (address_name gin_trgm_ops);
CREATE INDEX idx_restaurants_food_type_id ON ddogalmap_schema.restaurants USING btree (food_type_id);
CREATE INDEX idx_restaurants_location_geom_gist ON ddogalmap_schema.restaurants USING gist (((location)::geometry));
CREATE INDEX idx_restaurants_location_gist ON ddogalmap_schema.restaurants USING gist (location);
CREATE INDEX idx_restaurants_place_name ON ddogalmap_schema.restaurants USING btree (place_name);
CREATE INDEX idx_restaurants_place_trgm ON ddogalmap_schema.restaurants USING gin (place_name gin_trgm_ops);


-- ddogalmap_schema.users definition

-- Drop table

-- DROP TABLE ddogalmap_schema.users;

CREATE TABLE ddogalmap_schema.users (
	user_id bigserial NOT NULL,
	kakao_id int8 NULL,
	nickname varchar(50) NOT NULL,
	profile_image_url varchar(500) NULL,
	email varchar(255) NULL,
	"role" varchar(20) NOT NULL,
	created_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	is_chat_enabled bool DEFAULT true NOT NULL,
	region varchar(100) NULL,
	region_verified_at timestamp NULL,
	representative_badge_id int8 NULL,
	status varchar(255) DEFAULT 'ACTIVE'::character varying NOT NULL,
	CONSTRAINT users_pkey PRIMARY KEY (user_id),
	CONSTRAINT fk_users_representative_badge FOREIGN KEY (representative_badge_id) REFERENCES ddogalmap_schema.badges(badge_id)
);
CREATE INDEX idx_users_user_id_region ON ddogalmap_schema.users USING btree (user_id, region);


-- ddogalmap_schema.visit_verifications definition

-- Drop table

-- DROP TABLE ddogalmap_schema.visit_verifications;

CREATE TABLE ddogalmap_schema.visit_verifications (
	visit_verification_id bigserial NOT NULL,
	restaurant_id int8 NOT NULL,
	user_latitude float8 NOT NULL,
	user_longitude float8 NOT NULL,
	store_latitude float8 NOT NULL,
	store_longitude float8 NOT NULL,
	distance_meter float8 NOT NULL,
	accuracy_meter float8 NOT NULL,
	verified_at timestamp(6) NOT NULL,
	created_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	user_id int8 NULL,
	CONSTRAINT visit_verifications_pkey PRIMARY KEY (visit_verification_id),
	CONSTRAINT fk_visit_verification_restaurant FOREIGN KEY (restaurant_id) REFERENCES ddogalmap_schema.restaurants(restaurant_id),
	CONSTRAINT fk_visit_verification_user FOREIGN KEY (user_id) REFERENCES ddogalmap_schema.users(user_id)
);
CREATE INDEX idx_visit_verifications_restaurant_id ON ddogalmap_schema.visit_verifications USING btree (restaurant_id);


-- ddogalmap_schema.bookmark_categories definition

-- Drop table

-- DROP TABLE ddogalmap_schema.bookmark_categories;

CREATE TABLE ddogalmap_schema.bookmark_categories (
	bookmark_category_id bigserial NOT NULL,
	user_id int8 NOT NULL,
	bookmark_category_name varchar(100) NOT NULL,
	sort_order int4 NULL,
	is_default bool DEFAULT false NOT NULL,
	created_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT bookmark_categories_pkey PRIMARY KEY (bookmark_category_id),
	CONSTRAINT fk_bookmark_category_user FOREIGN KEY (user_id) REFERENCES ddogalmap_schema.users(user_id)
);
CREATE INDEX idx_bookmark_categories_user_sort ON ddogalmap_schema.bookmark_categories USING btree (user_id, sort_order, bookmark_category_id);


-- ddogalmap_schema.bookmarks definition

-- Drop table

-- DROP TABLE ddogalmap_schema.bookmarks;

CREATE TABLE ddogalmap_schema.bookmarks (
	bookmark_id bigserial NOT NULL,
	user_id int8 NOT NULL,
	restaurant_id int8 NOT NULL,
	bookmark_category_id int8 NOT NULL,
	memo text NULL,
	created_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT bookmarks_pkey PRIMARY KEY (bookmark_id),
	CONSTRAINT fk_bookmark_category FOREIGN KEY (bookmark_category_id) REFERENCES ddogalmap_schema.bookmark_categories(bookmark_category_id),
	CONSTRAINT fk_bookmark_restaurant FOREIGN KEY (restaurant_id) REFERENCES ddogalmap_schema.restaurants(restaurant_id),
	CONSTRAINT fk_bookmark_user FOREIGN KEY (user_id) REFERENCES ddogalmap_schema.users(user_id)
);
CREATE INDEX idx_bookmarks_restaurant_id ON ddogalmap_schema.bookmarks USING btree (restaurant_id);
CREATE INDEX idx_bookmarks_restaurant_user ON ddogalmap_schema.bookmarks USING btree (restaurant_id, user_id);
CREATE INDEX idx_bookmarks_user_category ON ddogalmap_schema.bookmarks USING btree (user_id, bookmark_category_id);
CREATE INDEX idx_bookmarks_user_restaurant ON ddogalmap_schema.bookmarks USING btree (user_id, restaurant_id);
CREATE UNIQUE INDEX uk_bookmarks_user_restaurant_category ON ddogalmap_schema.bookmarks USING btree (user_id, restaurant_id, bookmark_category_id);


-- ddogalmap_schema.chat_room_members definition

-- Drop table

-- DROP TABLE ddogalmap_schema.chat_room_members;

CREATE TABLE ddogalmap_schema.chat_room_members (
	chat_room_member_id bigserial NOT NULL,
	chat_room_id int8 NOT NULL,
	user_id int8 NOT NULL,
	"role" varchar(255) NOT NULL,
	created_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT chat_room_members_pkey PRIMARY KEY (chat_room_member_id),
	CONSTRAINT uk_chat_room_member UNIQUE (chat_room_id, user_id),
	CONSTRAINT fk_chat_room_member_room FOREIGN KEY (chat_room_id) REFERENCES ddogalmap_schema.chat_rooms(chat_room_id),
	CONSTRAINT fk_chat_room_member_user FOREIGN KEY (user_id) REFERENCES ddogalmap_schema.users(user_id)
);


-- ddogalmap_schema.direct_chat_rooms definition

-- Drop table

-- DROP TABLE ddogalmap_schema.direct_chat_rooms;

CREATE TABLE ddogalmap_schema.direct_chat_rooms (
	direct_chat_room_id bigserial NOT NULL,
	receiver_id int8 NOT NULL,
	requester_id int8 NOT NULL,
	created_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp NULL,
	requester_left_at timestamp NULL,
	receiver_left_at timestamp NULL,
	deleted_at timestamp NULL,
	CONSTRAINT direct_chat_rooms_pkey PRIMARY KEY (direct_chat_room_id),
	CONSTRAINT fk_direct_chat_receiver FOREIGN KEY (receiver_id) REFERENCES ddogalmap_schema.users(user_id),
	CONSTRAINT fk_direct_chat_requester FOREIGN KEY (requester_id) REFERENCES ddogalmap_schema.users(user_id)
);


-- ddogalmap_schema.gps_logs definition

-- Drop table

-- DROP TABLE ddogalmap_schema.gps_logs;

CREATE TABLE ddogalmap_schema.gps_logs (
	gps_log_id int8 DEFAULT nextval('gps_logs_id_seq'::regclass) NOT NULL,
	user_id int8 NOT NULL,
	accuracy float8 NULL,
	geom ddogalmap_schema.geometry(point, 4326) NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT gps_logs_pkey PRIMARY KEY (gps_log_id),
	CONSTRAINT fk_gps_logs_user FOREIGN KEY (user_id) REFERENCES ddogalmap_schema.users(user_id)
);
CREATE INDEX gps_logs_geom_idx ON ddogalmap_schema.gps_logs USING gist (geom);


-- ddogalmap_schema.restaurant_stats definition

-- Drop table

-- DROP TABLE ddogalmap_schema.restaurant_stats;

CREATE TABLE ddogalmap_schema.restaurant_stats (
	restaurant_id int8 NOT NULL,
	food_score numeric(4, 1) NULL,
	resident_recommend_rate int4 DEFAULT 0 NULL,
	revisit_rate int4 DEFAULT 0 NULL,
	visit_verify_count int8 DEFAULT 0 NULL,
	average_score numeric(2, 1) NULL,
	review_count int8 DEFAULT 0 NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT restaurant_stats_pkey PRIMARY KEY (restaurant_id),
	CONSTRAINT fk_restaurant_stats_restaurant FOREIGN KEY (restaurant_id) REFERENCES ddogalmap_schema.restaurants(restaurant_id) ON DELETE CASCADE
);
CREATE INDEX idx_restaurant_stats_average_score ON ddogalmap_schema.restaurant_stats USING btree (average_score DESC NULLS LAST);
CREATE INDEX idx_restaurant_stats_food_score ON ddogalmap_schema.restaurant_stats USING btree (food_score DESC NULLS LAST);


-- ddogalmap_schema.reviews definition

-- Drop table

-- DROP TABLE ddogalmap_schema.reviews;

CREATE TABLE ddogalmap_schema.reviews (
	review_id bigserial NOT NULL,
	score int4 NOT NULL,
	"content" varchar(500) NOT NULL,
	created_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	user_id int8 NOT NULL,
	restaurant_id int8 NOT NULL,
	is_revisit bool NULL,
	visit_verification_id int8 NULL,
	CONSTRAINT reviews_pkey PRIMARY KEY (review_id),
	CONSTRAINT uke7it88qx1m7o08xt7m896m5hg UNIQUE (visit_verification_id),
	CONSTRAINT fk_review_restaurant FOREIGN KEY (restaurant_id) REFERENCES ddogalmap_schema.restaurants(restaurant_id),
	CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES ddogalmap_schema.users(user_id),
	CONSTRAINT fkg8dgs59nrf7kr1vpk5ouavsys FOREIGN KEY (visit_verification_id) REFERENCES ddogalmap_schema.visit_verifications(visit_verification_id)
);
CREATE INDEX idx_reviews_restaurant_id ON ddogalmap_schema.reviews USING btree (restaurant_id);


-- ddogalmap_schema.tags definition

-- Drop table

-- DROP TABLE ddogalmap_schema.tags;

CREATE TABLE ddogalmap_schema.tags (
	tag_id bigserial NOT NULL,
	"content" varchar(255) NOT NULL,
	created_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	review_id int8 NOT NULL,
	CONSTRAINT tags_pkey PRIMARY KEY (tag_id),
	CONSTRAINT fk_tag_review FOREIGN KEY (review_id) REFERENCES ddogalmap_schema.reviews(review_id)
);


-- ddogalmap_schema.user_badges definition

-- Drop table

-- DROP TABLE ddogalmap_schema.user_badges;

CREATE TABLE ddogalmap_schema.user_badges (
	user_badge_id bigserial NOT NULL,
	badge_id int8 NOT NULL,
	user_id int8 NOT NULL,
	created_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT uk_user_badge UNIQUE (user_id, badge_id),
	CONSTRAINT user_badges_pkey PRIMARY KEY (user_badge_id),
	CONSTRAINT fk_user_badge_badge FOREIGN KEY (badge_id) REFERENCES ddogalmap_schema.badges(badge_id),
	CONSTRAINT fk_user_badge_user FOREIGN KEY (user_id) REFERENCES ddogalmap_schema.users(user_id)
);


-- ddogalmap_schema.user_level_histories definition

-- Drop table

-- DROP TABLE ddogalmap_schema.user_level_histories;

CREATE TABLE ddogalmap_schema.user_level_histories (
	user_level_history_id bigserial NOT NULL,
	user_id int8 NOT NULL,
	level_policy_id int8 NOT NULL,
	created_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	exp_amount int4 NOT NULL,
	total_exp_after int4 NOT NULL,
	level_id_after int8 NOT NULL,
	reference_id int8 NULL,
	CONSTRAINT uk_user_level_histories_policy_reference UNIQUE (user_id, level_policy_id, reference_id),
	CONSTRAINT user_level_histories_pkey PRIMARY KEY (user_level_history_id),
	CONSTRAINT fk_user_level_histories_level_after FOREIGN KEY (level_id_after) REFERENCES ddogalmap_schema.levels(level_id),
	CONSTRAINT fk_user_level_history_policy FOREIGN KEY (level_policy_id) REFERENCES ddogalmap_schema.level_policies(level_policy_id),
	CONSTRAINT fk_user_level_history_user FOREIGN KEY (user_id) REFERENCES ddogalmap_schema.users(user_id)
);


-- ddogalmap_schema.user_levels definition

-- Drop table

-- DROP TABLE ddogalmap_schema.user_levels;

CREATE TABLE ddogalmap_schema.user_levels (
	user_level_id bigserial NOT NULL,
	user_id int8 NOT NULL,
	level_id int8 NOT NULL,
	"exp" int4 NOT NULL,
	created_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT uk_user_levels_user_id UNIQUE (user_id),
	CONSTRAINT user_levels_pkey PRIMARY KEY (user_level_id),
	CONSTRAINT fk_user_level_level FOREIGN KEY (level_id) REFERENCES ddogalmap_schema.levels(level_id),
	CONSTRAINT fk_user_level_user FOREIGN KEY (user_id) REFERENCES ddogalmap_schema.users(user_id)
);


-- ddogalmap_schema.user_region_attempts definition

-- Drop table

-- DROP TABLE ddogalmap_schema.user_region_attempts;

CREATE TABLE ddogalmap_schema.user_region_attempts (
	user_region_attempt_id bigserial NOT NULL,
	user_id int8 NOT NULL,
	region_id int8 NOT NULL,
	latitude float8 NULL,
	longitude float8 NULL,
	accuracy float8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT user_region_attempts_pkey PRIMARY KEY (user_region_attempt_id),
	CONSTRAINT fk_user_region_attempt_region FOREIGN KEY (region_id) REFERENCES ddogalmap_schema.regions(region_id),
	CONSTRAINT fk_user_region_attempt_user FOREIGN KEY (user_id) REFERENCES ddogalmap_schema.users(user_id)
);


-- ddogalmap_schema.chat_messages definition

-- Drop table

-- DROP TABLE ddogalmap_schema.chat_messages;

CREATE TABLE ddogalmap_schema.chat_messages (
	chat_message_id bigserial NOT NULL,
	direct_chat_room_id int8 NULL,
	writer int8 NOT NULL,
	status varchar(20) NOT NULL,
	message varchar(255) NOT NULL,
	created_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp NULL,
	chat_room_id int8 NULL,
	CONSTRAINT chat_messages_pkey PRIMARY KEY (chat_message_id),
	CONSTRAINT fk_chat_message_direct_room FOREIGN KEY (direct_chat_room_id) REFERENCES ddogalmap_schema.direct_chat_rooms(direct_chat_room_id),
	CONSTRAINT fk_chat_message_writer FOREIGN KEY (writer) REFERENCES ddogalmap_schema.users(user_id),
	CONSTRAINT fkbcsxusjp1v4rd8879fhvq8ssb FOREIGN KEY (chat_room_id) REFERENCES ddogalmap_schema.chat_rooms(chat_room_id)
);


-- ddogalmap_schema.likes definition

-- Drop table

-- DROP TABLE ddogalmap_schema.likes;

CREATE TABLE ddogalmap_schema.likes (
	like_id bigserial NOT NULL,
	review_id int8 NOT NULL,
	user_id int8 NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT likes_pkey PRIMARY KEY (like_id),
	CONSTRAINT unique_user_review UNIQUE (user_id, review_id),
	CONSTRAINT fk_like_review FOREIGN KEY (review_id) REFERENCES ddogalmap_schema.reviews(review_id) ON DELETE CASCADE,
	CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES ddogalmap_schema.users(user_id) ON DELETE CASCADE
);


-- ddogalmap_schema.review_imgs definition

-- Drop table

-- DROP TABLE ddogalmap_schema.review_imgs;

CREATE TABLE ddogalmap_schema.review_imgs (
	img_id bigserial NOT NULL,
	img_url varchar(255) NOT NULL,
	created_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	review_id int8 NOT NULL,
	org_img_name varchar(255) NOT NULL,
	CONSTRAINT review_imgs_pkey PRIMARY KEY (img_id),
	CONSTRAINT fk_review_img_review FOREIGN KEY (review_id) REFERENCES ddogalmap_schema.reviews(review_id)
);
