package com.chhavi.prodee.journaling.repository;

import com.chhavi.prodee.journaling.entity.ScrapbookEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrapbookEntryRepository extends JpaRepository<ScrapbookEntry, Long> {
    List<ScrapbookEntry> findByUserIdOrderByCreatedAtDesc(Long userId);
}
