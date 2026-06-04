package com.ddogalmap.domain.visit.entity;

import com.ddogalmap.domain.restaurants.entity.Restaurant;
import com.ddogalmap.domain.users.BaseEntity;
import com.ddogalmap.domain.users.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "visit_verifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VisitVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visit_verification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "user_latitude", nullable = false)
    private Double userLatitude;

    @Column(name = "user_longitude", nullable = false)
    private Double userLongitude;

    @Column(name = "store_latitude", nullable = false)
    private Double storeLatitude;

    @Column(name = "store_longitude", nullable = false)
    private Double storeLongitude;

    @Column(name = "distance_meter", nullable = false)
    private Double distanceMeter;

    @Column(name = "accuracy_meter")
    private Double accuracyMeter;

    @Column(name = "verified_at", nullable = false)
    private LocalDateTime verifiedAt;

    private VisitVerification(
            Restaurant restaurant,
            User user,
            Double userLatitude,
            Double userLongitude,
            Double storeLatitude,
            Double storeLongitude,
            Double distanceMeter,
            Double accuracyMeter
    ) {
        this.restaurant = restaurant;
        this.user = user;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
        this.storeLatitude = storeLatitude;
        this.storeLongitude = storeLongitude;
        this.distanceMeter = distanceMeter;
        this.accuracyMeter = accuracyMeter;
        this.verifiedAt = LocalDateTime.now();
    }

    public static VisitVerification create(
            Restaurant restaurant,
            User user,
            Double userLatitude,
            Double userLongitude,
            Double storeLatitude,
            Double storeLongitude,
            Double distanceMeter,
            Double accuracyMeter
    ) {
        return new VisitVerification(
                restaurant,
                user,
                userLatitude,
                userLongitude,
                storeLatitude,
                storeLongitude,
                distanceMeter,
                accuracyMeter
        );
    }
}