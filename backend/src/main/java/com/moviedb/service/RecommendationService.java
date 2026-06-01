package com.moviedb.service;

import com.moviedb.dto.MovieDto;
import com.moviedb.model.Movie;
import com.moviedb.model.Rating;
import com.moviedb.repository.MovieRepository;
import com.moviedb.repository.RatingRepository;
import com.moviedb.repository.WatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates personalised movie recommendations for a user.
 *
 * WHY a dedicated service?
 *   Recommendation logic is an independent concern — it aggregates data from
 *   ratings, watch history, and movie metadata. Keeping it separate from
 *   MovieService avoids coupling browsing logic with personalisation logic.
 *
 * Algorithm (v1 — collaborative signals):
 *   1. Find all movies the user has rated ≥ 7.0
 *   2. Extract the genres of those well-rated movies
 *   3. Return published movies matching those genres, excluding already-seen ones
 *   4. Sort by rating DESC, popularity DESC
 *
 * Future: matrix factorisation, item-based collaborative filtering, or
 * embedding-based nearest-neighbour search.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final MovieRepository        movieRepository;
    private final RatingRepository       ratingRepository;
    private final WatchHistoryRepository watchHistoryRepository;
    private final MovieService           movieService;

    private static final double LIKED_THRESHOLD = 7.0;
    private static final int    MAX_RESULTS     = 20;

    /**
     * Returns a personalised list of recommended movies for the given user.
     *
     * @param userId the authenticated user's ID
     * @return up to MAX_RESULTS recommended MovieDto objects
     */
    @Transactional(readOnly = true)
    public List<MovieDto> getRecommendations(Long userId) {
        // Step 1: collect IDs of movies the user liked (rating >= threshold)
        List<Rating> userRatings = ratingRepository.findByUserId(userId);
        List<Long> likedMovieIds = userRatings.stream()
                .filter(r -> r.getRating() >= LIKED_THRESHOLD)
                .map(Rating::getMovieId)
                .toList();

        // Step 2: collect IDs of already-watched movies (exclude from results)
        List<Long> watchedIds = watchHistoryRepository
                .findByUserIdOrderByWatchedAtDesc(userId)
                .stream()
                .map(wh -> wh.getMovieId())
                .toList();

        // Step 3: extract genres from liked movies
        if (likedMovieIds.isEmpty()) {
            // Cold start: return top-rated published movies
            log.debug("Cold start for user {}: returning top-rated movies", userId);
            return movieRepository
                    .findByStatusOrderByRatingDescPopularityDesc(Movie.MovieStatus.PUBLISHED)
                    .stream()
                    .filter(m -> !watchedIds.contains(m.getId()))
                    .limit(MAX_RESULTS)
                    .map(movieService::toDto)
                    .toList();
        }

        // Collect genre frequencies from liked movies
        Map<String, Long> genreFrequency = likedMovieIds.stream()
                .flatMap(id -> movieRepository.findById(id).stream())
                .flatMap(m -> m.getGenre().stream())
                .collect(Collectors.groupingBy(g -> g, Collectors.counting()));

        // Top genres by frequency
        List<String> topGenres = genreFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        log.debug("Recommending for user {} based on genres: {}", userId, topGenres);

        // Step 4: return published movies matching top genres, excluding already seen
        return movieRepository
                .findByStatusOrderByRatingDescPopularityDesc(Movie.MovieStatus.PUBLISHED)
                .stream()
                .filter(m -> !likedMovieIds.contains(m.getId()))
                .filter(m -> !watchedIds.contains(m.getId()))
                .filter(m -> m.getGenre().stream().anyMatch(topGenres::contains))
                .limit(MAX_RESULTS)
                .map(movieService::toDto)
                .toList();
    }
}
