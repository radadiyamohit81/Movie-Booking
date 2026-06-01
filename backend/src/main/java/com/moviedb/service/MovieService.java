package com.moviedb.service;

import com.moviedb.dto.MovieAdminRequest;
import com.moviedb.dto.MovieDto;
import com.moviedb.model.Movie;
import com.moviedb.exception.ResourceNotFoundException;
import com.moviedb.repository.MovieRepository;
import com.moviedb.repository.spec.MovieSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Movie business logic — search, browse, admin CRUD.
 *
 * Search architecture:
 *   The "type" parameter routes to a different Specification:
 *     type=title  → hasTextQueryTitle  (title column only)
 *     type=celebs → hasTextQueryCelebs (stars collection only)
 *     type=all    → hasTextQueryAll    (title, director, description, writers, stars)
 *   All other filters (rating, year, genre, director, writer) are always applied
 *   regardless of the "type" route.
 *
 *   Results are sorted by rating DESC, popularity DESC in the DB query (Sort object
 *   passed to findAll) — never in Java stream, which would not honour DB-level DISTINCT.
 */
@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    // ── Public search ─────────────────────────────────────────────────────

    /**
     * Dynamic search with composable filters.
     *
     * Critical rule: NEVER call q.trim() before null-checking q.
     * If q is null and we call q.trim(), we get a NullPointerException.
     * The null check inside each Specification handles this correctly.
     */
    @Transactional(readOnly = true)
    public List<MovieDto> search(
            String q,
            String type,
            Double minRating,
            Double maxRating,
            String genre,
            Integer year,
            Integer minYear,
            Integer maxYear,
            String director,
            String writer) {

        // Build base spec — always filter to published only
        Specification<Movie> spec = Specification.where(MovieSpecifications.isPublished());

        // Route text query by "type" parameter
        if ("title".equalsIgnoreCase(type)) {
            spec = spec.and(MovieSpecifications.hasTextQueryTitle(q));
        } else if ("celebs".equalsIgnoreCase(type)) {
            spec = spec.and(MovieSpecifications.hasTextQueryCelebs(q));
        } else {
            // "all" or any unrecognised value → search everywhere
            spec = spec.and(MovieSpecifications.hasTextQueryAll(q));
        }

        // Parse comma-separated genre list; null if not provided
        List<String> genreList = null;
        if (genre != null && !genre.isBlank()) {
            genreList = Arrays.asList(genre.split(","));
        }

        // Compose remaining filters — each returns null when param is absent
        spec = spec
                .and(MovieSpecifications.hasMinRating(minRating))
                .and(MovieSpecifications.hasMaxRating(maxRating))
                .and(MovieSpecifications.hasGenres(genreList))
                .and(MovieSpecifications.hasYear(year))
                .and(MovieSpecifications.hasMinYear(minYear))
                .and(MovieSpecifications.hasMaxYear(maxYear))
                .and(MovieSpecifications.hasDirector(director))
                .and(MovieSpecifications.hasWriter(writer));

        // Sort in the DB query — do NOT re-sort in Java after fetch
        Sort sort = Sort.by(Sort.Direction.DESC, "rating")
                        .and(Sort.by(Sort.Direction.DESC, "popularity"));

        return movieRepository.findAll(spec, sort)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // ── Public browse endpoints ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public MovieDto findById(Long id) {
        return movieRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", id));
    }

    @Transactional(readOnly = true)
    public List<MovieDto> getFeatured(int limit) {
        return movieRepository
                .findByStatusOrderByPopularityDescRatingDesc(Movie.MovieStatus.PUBLISHED)
                .stream()
                .limit(limit)
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovieDto> getTrending(int limit) {
        return movieRepository
                .findByStatusOrderByRatingDescPopularityDesc(Movie.MovieStatus.PUBLISHED)
                .stream()
                .limit(limit)
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovieDto> getUpcoming() {
        return movieRepository
                .findByStatusOrderByCreatedAtDesc(Movie.MovieStatus.UPCOMING)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovieDto> getByType(String type) {
        Movie.MovieType movieType;
        try {
            movieType = Movie.MovieType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid type: " + type + ". Must be 'MOVIE' or 'SERIES'");
        }
        return movieRepository
                .findByTypeAndStatusOrderByPopularityDescRatingDesc(movieType, Movie.MovieStatus.PUBLISHED)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // ── Admin CRUD ────────────────────────────────────────────────────────

    @Transactional
    public MovieDto create(MovieAdminRequest req) {
        Movie movie = fromRequest(req, new Movie());
        return toDto(movieRepository.save(movie));
    }

    @Transactional
    public MovieDto update(Long id, MovieAdminRequest req) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", id));
        fromRequest(req, movie);
        return toDto(movieRepository.save(movie));
    }

    @Transactional
    public void delete(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Movie", id);
        }
        movieRepository.deleteById(id);
    }

    // ── Mapping helpers ───────────────────────────────────────────────────

    public MovieDto toDto(Movie m) {
        return MovieDto.builder()
                .id(String.valueOf(m.getId()))
                .title(m.getTitle())
                .releaseYear(m.getReleaseYear())
                .duration(m.getDuration())
                .rating(m.getRating())
                .popularity(m.getPopularity())
                .description(m.getDescription())
                .director(m.getDirector())
                .writers(m.getWriters())
                .stars(m.getStars())
                .genre(m.getGenre())
                .type(m.getType() != null ? m.getType().name() : null)
                .status(m.getStatus() != null ? m.getStatus().name() : null)
                .createdAt(m.getCreatedAt() != null ? m.getCreatedAt().format(ISO_FORMATTER) : null)
                .updatedAt(m.getUpdatedAt() != null ? m.getUpdatedAt().format(ISO_FORMATTER) : null)
                .build();
    }

    private Movie fromRequest(MovieAdminRequest req, Movie movie) {
        if (req.getTitle()       != null) movie.setTitle(req.getTitle());
        if (req.getReleaseYear()        != null) movie.setReleaseYear(req.getReleaseYear());
        if (req.getDuration()    != null) movie.setDuration(req.getDuration());
        if (req.getRating()      != null) movie.setRating(req.getRating());
        if (req.getPopularity()  != null) movie.setPopularity(req.getPopularity());
        if (req.getDescription() != null) movie.setDescription(req.getDescription());
        if (req.getDirector()    != null) movie.setDirector(req.getDirector());
        if (req.getWriters()     != null) movie.setWriters(req.getWriters());
        if (req.getStars()       != null) movie.setStars(req.getStars());
        if (req.getGenre()       != null) movie.setGenre(req.getGenre());

        if (req.getType() != null) {
            movie.setType(Movie.MovieType.valueOf(req.getType().toUpperCase()));
        }
        if (req.getStatus() != null) {
            movie.setStatus(Movie.MovieStatus.valueOf(req.getStatus().toUpperCase()));
        }
        return movie;
    }
}
