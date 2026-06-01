package com.chhavi.prodee.journaling.repository;

import com.chhavi.prodee.journaling.entity.DailyPixel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyPixelRepository extends JpaRepository<DailyPixel, Long> {
    List<DailyPixel> findByUserIdAndPixelDateBetweenOrderByPixelDateAsc(Long userId, LocalDate start, LocalDate end);
    List<DailyPixel> findByUserIdAndTemplateIdOrderByPixelDateAsc(Long userId, Long templateId);
    List<DailyPixel> findByUserIdOrderByPixelDateDesc(Long userId);
    Optional<DailyPixel> findByUserIdAndTemplateIdAndPixelDate(Long userId, Long templateId, LocalDate pixelDate);
    void deleteByUserIdAndTemplateId(Long userId, Long templateId);
}
