package com.ddogalmap.domain.levels.entity;

import com.ddogalmap.domain.users.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_level_histories")
public class UserLevelHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_level_history_id")
    private Long userLevelHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_policy_id", nullable = false)
    private LevelPolicy levelPolicy;

    @Column(name = "exp_amount", nullable = false)
    private Integer expAmount;

    @Column(name = "total_exp_after", nullable = false)
    private Integer totalExpAfter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id_after", nullable = false)
    private Level levelAfter;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static UserLevelHistory create(
            User user,
            LevelPolicy levelPolicy,
            Integer expAmount,
            Integer totalExpAfter,
            Level levelAfter,
            Long referenceId
    ) {
        UserLevelHistory history = new UserLevelHistory();
        history.user = user;
        history.levelPolicy = levelPolicy;
        history.expAmount = expAmount;
        history.totalExpAfter = totalExpAfter;
        history.levelAfter = levelAfter;
        history.createdAt = LocalDateTime.now();
        history.referenceId = referenceId;
        return history;
    }
}
