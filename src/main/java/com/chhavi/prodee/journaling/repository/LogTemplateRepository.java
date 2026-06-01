package com.chhavi.prodee.journaling.repository;

import com.chhavi.prodee.journaling.entity.LogTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogTemplateRepository extends JpaRepository<LogTemplate, Long> {
    List<LogTemplate> findByUserId(Long userId);
    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
}
