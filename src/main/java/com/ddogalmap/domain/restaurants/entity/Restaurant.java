package com.ddogalmap.domain.restaurants.entity;

import com.ddogalmap.domain.foodtypes.entity.FoodType;
import com.ddogalmap.domain.users.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "restaurants")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Restaurant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long restaurantId;

    @Column(unique = true, length = 50)
    private String managementNo;

    @Column(nullable = false, length = 250)
    private String placeName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_type_id", nullable = false)
    private FoodType foodType;

    @Column(length = 30)
    private String phone;

    @Column(length = 250)
    private String addressName;

    @Column(length = 250)
    private String roadAddressName;

    private Double x;

    private Double y;

    // location (PostGIS geography)은 JPA가 직접 다루기 까다로워
    // 적재 후 native query (UPDATE ... ST_SetSRID(ST_MakePoint, 4326)::geography)로 채움
    // → 엔티티에서는 매핑하지 않음

    @Column(length = 250)
    private String placeUrl;

    protected Restaurant(String managementNo, String placeName, FoodType foodType,
                         String phone, String addressName, String roadAddressName,
                         Double x, Double y, String placeUrl) {
        this.managementNo = managementNo;
        this.placeName = placeName;
        this.foodType = foodType;
        this.phone = phone;
        this.addressName = addressName;
        this.roadAddressName = roadAddressName;
        this.x = x;
        this.y = y;
        this.placeUrl = placeUrl;
    }

    public static Restaurant createFromSeoulApi(
            String managementNo, String placeName, FoodType foodType,
            String phone, String addressName, String roadAddressName,
            Double x, Double y, String placeUrl) {
        return new Restaurant(managementNo, placeName, foodType,
                              phone, addressName, roadAddressName,
                              x, y, placeUrl);
    }
}
