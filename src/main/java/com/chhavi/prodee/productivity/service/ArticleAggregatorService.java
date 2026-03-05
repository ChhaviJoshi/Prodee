package com.chhavi.prodee.productivity.service;

import com.chhavi.prodee.auth.entity.User;
import com.chhavi.prodee.auth.repository.UserRepository;
import com.chhavi.prodee.common.exception.ResourceNotFoundException;
import com.chhavi.prodee.productivity.dto.ArticleResponse;
import com.chhavi.prodee.productivity.entity.AggregatedArticle;
import com.chhavi.prodee.productivity.repository.AggregatedArticleRepository;
import com.chhavi.prodee.productivity.repository.HabitRepository;
import com.chhavi.prodee.productivity.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Fetches articles from Dev.to daily for every distinct active habit + task tag.
 * De-duplicates by URL and appends new tags to existing articles.
 * Cleans up articles older than 30 days.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleAggregatorService {

    private final HabitRepository habitRepository;
    private final TaskRepository taskRepository;
    private final AggregatedArticleRepository articleRepository;
    private final UserRepository userRepository;

    private static final List<String> DEFAULT_TAGS = List.of("productivity", "technology");

    @Value("${prodee.devto.api-url}")
    private String devtoApiUrl;

    // ── Scheduled Jobs ──────────────────────────────────────

    /**
     * CRON: runs every day at 06:00 AM IST.
     * Collects tags from active habits + incomplete tasks, fetches from Dev.to.
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void fetchArticles() {
        Set<String> allTags = collectGlobalTags();
        if (allTags.isEmpty()) {
            allTags = new LinkedHashSet<>(DEFAULT_TAGS);
        }
        fetchAndPersistArticlesForTags(allTags);
    }

    /**
     * CRON: runs every day at 03:00 AM IST.
     * Purges articles older than 30 days.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeOldArticles() {
        Instant cutoff = Instant.now().minus(30, ChronoUnit.DAYS);
        int deleted = articleRepository.deleteByFetchedAtBefore(cutoff);
        log.info("Purged {} articles older than 30 days", deleted);
    }

    // ── Public Query Methods ────────────────────────────────

    public List<ArticleResponse> getArticlesByTag(String tag) {
        return articleRepository.findByTagContainingIgnoreCase(tag)
                .stream().map(this::toResponse).toList();
    }

    public List<ArticleResponse> getRecentArticles() {
        return articleRepository.findTop20ByOrderByFetchedAtDesc()
                .stream().map(this::toResponse).toList();
    }

    /**
     * Personalized feed: fetches LIVE articles from Dev.to based on the user's
     * active habit tags + incomplete task tags. Falls back to default
     * "productivity" and "technology" tags if user has zero active tags.
     * Always returns fresh article links.
     *
     * NOT @Transactional — the live API fetch + opportunistic DB save are
     * handled independently to prevent a failed save from poisoning the response.
     */
    public List<ArticleResponse> getPersonalizedFeed(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Set<String> userTags = collectUserTags(user.getId());

        // Fallback: if user has zero active tags, use defaults
        if (userTags.isEmpty()) {
            userTags = new LinkedHashSet<>(DEFAULT_TAGS);
        }

        // 1. Fetch live articles straight from the Dev.to API
        List<ArticleResponse> liveResults = fetchArticlesFromApi(userTags);

        if (!liveResults.isEmpty()) {
            // Opportunistically persist to DB for caching (best-effort)
            persistArticlesBestEffort(liveResults);
            return liveResults;
        }

        // 2. If live fetch returned nothing, fall back to DB
        log.warn("Live Dev.to fetch returned 0 articles — falling back to DB cache");
        Map<Long, AggregatedArticle> uniqueArticles = new LinkedHashMap<>();
        for (String tag : userTags) {
            articleRepository.findByTagContainingIgnoreCase(tag)
                    .forEach(a -> uniqueArticles.putIfAbsent(a.getId(), a));
        }

        if (!uniqueArticles.isEmpty()) {
            return uniqueArticles.values().stream()
                    .sorted(Comparator.comparing(AggregatedArticle::getFetchedAt).reversed())
                    .limit(20)
                    .map(this::toResponse)
                    .toList();
        }

        // 3. Last resort: try default tags if we haven't already
        if (!userTags.equals(new LinkedHashSet<>(DEFAULT_TAGS))) {
            log.warn("DB cache empty too — trying default tags as last resort");
            liveResults = fetchArticlesFromApi(new LinkedHashSet<>(DEFAULT_TAGS));
            if (!liveResults.isEmpty()) return liveResults;
        }

        return List.of();
    }

    // ── Helpers ─────────────────────────────────────────────

    /**
     * Fetch articles directly from the Dev.to API and return them as
     * ArticleResponse DTOs.  No database interaction happens here, so network
     * failures never corrupt a transaction.
     */
    private List<ArticleResponse> fetchArticlesFromApi(Set<String> tags) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Prodee/1.0 (Smart Feed Aggregator)");
        headers.set("Accept", "application/json");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        Map<String, ArticleResponse> resultMap = new LinkedHashMap<>();

        for (String tag : tags) {
            try {
                String url = devtoApiUrl + "?tag=" + tag + "&per_page=10";
                log.info("Fetching from Dev.to: {}", url);
                ResponseEntity<List> response = restTemplate.exchange(
                        url, HttpMethod.GET, requestEntity, List.class);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> articles = response.getBody();
                if (articles == null) {
                    log.warn("Dev.to returned null body for tag '{}'", tag);
                    continue;
                }

                for (Map<String, Object> a : articles) {
                    String articleUrl = (String) a.get("url");
                    String title      = (String) a.get("title");
                    String coverImage  = (String) a.get("cover_image");

                    if (articleUrl == null || articleUrl.isBlank()) continue;

                    resultMap.putIfAbsent(articleUrl, new ArticleResponse(
                            null, title, articleUrl, "dev.to", tag, coverImage, Instant.now()));
                }
                log.info("Fetched {} articles from Dev.to for tag '{}'", articles.size(), tag);
            } catch (Exception e) {
                log.error("Failed to fetch articles for tag '{}': {} — {}", tag, e.getClass().getSimpleName(), e.getMessage());
            }
        }

        log.info("Total unique articles from live API: {}", resultMap.size());
        return new ArrayList<>(resultMap.values());
    }

    /**
     * Best-effort persistence of fetched articles to the DB for caching.
     * Each article is saved individually; failures are logged and skipped.
     */
    private void persistArticlesBestEffort(List<ArticleResponse> articles) {
        for (ArticleResponse ar : articles) {
            try {
                saveOrUpdateArticle(ar.url(), ar.title(), ar.coverImageUrl(), ar.tags());
            } catch (Exception e) {
                log.debug("Skipped DB persist for '{}': {}", ar.url(), e.getMessage());
            }
        }
    }

    /**
     * Save a new article or update an existing one (matching URL).
     * Runs in its own transaction so a collision doesn't affect the caller.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AggregatedArticle saveOrUpdateArticle(String articleUrl, String title, String coverImage, String tag) {
        Optional<AggregatedArticle> existing = articleRepository.findByUrl(articleUrl);
        if (existing.isPresent()) {
            AggregatedArticle article = existing.get();
            article.addTag(tag);
            return articleRepository.save(article);
        }
        try {
            AggregatedArticle article = AggregatedArticle.builder()
                    .title(title)
                    .url(articleUrl)
                    .source("dev.to")
                    .tags(tag)
                    .coverImageUrl(coverImage)
                    .build();
            return articleRepository.save(article);
        } catch (DataIntegrityViolationException e) {
            log.warn("URL collision for '{}', updating existing record", articleUrl);
            Optional<AggregatedArticle> retry = articleRepository.findByUrl(articleUrl);
            if (retry.isPresent()) {
                retry.get().addTag(tag);
                return articleRepository.save(retry.get());
            }
            return null;
        }
    }

    /**
     * Fetch and persist articles for the scheduled cron job.
     */
    private void fetchAndPersistArticlesForTags(Set<String> tags) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Prodee/1.0 (Smart Feed Aggregator)");
        headers.set("Accept", "application/json");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        for (String tag : tags) {
            try {
                String url = devtoApiUrl + "?tag=" + tag + "&per_page=10";
                ResponseEntity<List> response = restTemplate.exchange(
                        url, HttpMethod.GET, requestEntity, List.class);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> articles = response.getBody();
                if (articles == null) continue;

                for (Map<String, Object> a : articles) {
                    String articleUrl = (String) a.get("url");
                    String articleTitle = (String) a.get("title");
                    String coverImage = (String) a.get("cover_image");
                    if (articleUrl == null || articleUrl.isBlank()) continue;
                    saveOrUpdateArticle(articleUrl, articleTitle, coverImage, tag);
                }
                log.info("Fetched {} articles for tag '{}'", articles.size(), tag);
            } catch (Exception e) {
                log.error("Failed to fetch articles for tag '{}': {}", tag, e.getMessage());
            }
        }
    }

    /**
     * Collect all unique tags across ALL users' active habits + incomplete tasks.
     * Used by the global fetch cron job.
     */
    private Set<String> collectGlobalTags() {
        Set<String> tags = new LinkedHashSet<>();
        habitRepository.findAllDistinctActiveTags().forEach(t -> tags.add(t.toLowerCase().trim()));
        taskRepository.findAllDistinctActiveTaskTags().stream()
                .flatMap(csv -> Arrays.stream(csv.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .forEach(tags::add);
        return tags;
    }

    /**
     * Collect tags for a specific user (habits + incomplete tasks).
     * Used by the personalized feed endpoint.
     */
    private Set<String> collectUserTags(Long userId) {
        Set<String> tags = new LinkedHashSet<>();
        habitRepository.findDistinctActiveTagsByUserId(userId)
                .forEach(t -> tags.add(t.toLowerCase().trim()));
        taskRepository.findDistinctActiveTagsByUserId(userId).stream()
                .flatMap(csv -> Arrays.stream(csv.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .forEach(tags::add);
        return tags;
    }

    private ArticleResponse toResponse(AggregatedArticle a) {
        return new ArticleResponse(
                a.getId(), a.getTitle(), a.getUrl(), a.getSource(),
                a.getTags(), a.getCoverImageUrl(), a.getFetchedAt());
    }
}
