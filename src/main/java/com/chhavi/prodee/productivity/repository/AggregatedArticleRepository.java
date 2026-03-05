package com.chhavi.prodee.productivity.repository;

import com.chhavi.prodee.productivity.entity.AggregatedArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AggregatedArticleRepository extends JpaRepository<AggregatedArticle, Long> {

    Optional<AggregatedArticle> findByUrl(String url);

    /** Find articles whose comma-separated tags column contains the given tag (case-insensitive) */
    @Query("SELECT a FROM AggregatedArticle a WHERE LOWER(a.tags) LIKE LOWER(CONCAT('%', :tag, '%')) ORDER BY a.fetchedAt DESC")
    List<AggregatedArticle> findByTagContainingIgnoreCase(String tag);

    List<AggregatedArticle> findTop20ByOrderByFetchedAtDesc();

    /** Find articles matching any of the supplied tags */
    @Query("SELECT DISTINCT a FROM AggregatedArticle a WHERE " +
           "LOWER(a.tags) LIKE LOWER(CONCAT('%', :tag1, '%')) OR " +
           "LOWER(a.tags) LIKE LOWER(CONCAT('%', :tag2, '%')) OR " +
           "LOWER(a.tags) LIKE LOWER(CONCAT('%', :tag3, '%')) " +
           "ORDER BY a.fetchedAt DESC")
    List<AggregatedArticle> findByAnyOfThreeTags(String tag1, String tag2, String tag3);

    /** Bulk-delete articles older than the given cutoff */
    @Modifying
    @Query("DELETE FROM AggregatedArticle a WHERE a.fetchedAt < :cutoff")
    int deleteByFetchedAtBefore(Instant cutoff);
}
