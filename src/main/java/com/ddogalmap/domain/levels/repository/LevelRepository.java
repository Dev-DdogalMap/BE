package com.ddogalmap.domain.levels.repository;


import com.ddogalmap.domain.levels.entity.Level;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LevelRepository extends JpaRepository<Level, Long> {

    Optional<Level> findTopByRequiredExpLessThanEqualOrderByRequiredExpDesc(Integer exp);

    Optional<Level> findByLevel(Integer level);
}
