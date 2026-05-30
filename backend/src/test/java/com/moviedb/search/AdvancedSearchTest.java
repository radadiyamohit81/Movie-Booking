package com.moviedb.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviedb.entity.Movie;
import com.moviedb.enums.MovieStatus;
import com.moviedb.enums.MovieType;
import com.moviedb.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Movie advanced search feature.
 *
 * Why @SpringBootTest + @AutoConfigureMockMvc?
 *   - Full application context verifies all layers (Controller → Service → Repository).
 *   - MockMvc lets us make HTTP-level calls without a real server (fast + isolated).
 *
 * Why @ActiveProfiles("test")?
 *   - Switches to in-memory H2 (application-test.yml), giving each test run a
 *     clean schema. Tests are independent of any dev database state.
 *
 * Test data is seeded in @BeforeEach to guarantee known data for each test
 * regardless of insertion order or seed initializer side effects.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdvancedSearchTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        movieRepository.deleteAll();

        List<Movie> seed = List.of(
            movie("The Dark Knight",             2008, 9.0, 98, List.of("Action","Crime","Drama"),
                  "Christopher Nolan", List.of("Jonathan Nolan","Christopher Nolan"),
                  List.of("Christian Bale","Heath Ledger","Aaron Eckhart")),
            movie("The Shawshank Redemption",    1994, 9.3, 97, List.of("Drama"),
                  "Frank Darabont",    List.of("Frank Darabont"),
                  List.of("Tim Robbins","Morgan Freeman")),
            movie("Inception",                   2010, 8.8, 93, List.of("Action","Sci-Fi","Thriller"),
                  "Christopher Nolan", List.of("Christopher Nolan"),
                  List.of("Leonardo DiCaprio","Joseph Gordon-Levitt")),
            movie("Interstellar",                2014, 8.6, 90, List.of("Adventure","Drama","Sci-Fi"),
                  "Christopher Nolan", List.of("Jonathan Nolan","Christopher Nolan"),
                  List.of("Matthew McConaughey","Anne Hathaway")),
            movie("Pulp Fiction",                1994, 8.9, 94, List.of("Crime","Drama"),
                  "Quentin Tarantino", List.of("Quentin Tarantino","Roger Avary"),
                  List.of("John Travolta","Uma Thurman","Robert De Niro")),
            movie("The Godfather",               1972, 9.2, 96, List.of("Crime","Drama"),
                  "Francis Ford Coppola", List.of("Mario Puzo","Francis Ford Coppola"),
                  List.of("Marlon Brando","Al Pacino","Robert Duvall")),
            movie("Gladiator",                   2000, 8.5, 88, List.of("Action","Drama"),
                  "Ridley Scott",      List.of("David Franzoni"),
                  List.of("Russell Crowe","Joaquin Phoenix")),
            movie("Get Out",                     2017, 7.7, 78, List.of("Horror","Thriller"),
                  "Jordan Peele",      List.of("Jordan Peele"),
                  List.of("Daniel Kaluuya","Allison Williams")),
            movie("La La Land",                  2016, 8.0, 82, List.of("Drama","Romance"),
                  "Damien Chazelle",   List.of("Damien Chazelle"),
                  List.of("Ryan Gosling","Emma Stone","Robert De Niro")),
            movie("Mad Max: Fury Road",          2015, 8.1, 83, List.of("Action","Adventure"),
                  "George Miller",     List.of("George Miller","Brendan McCarthy"),
                  List.of("Tom Hardy","Charlize Theron"))
        );

        movieRepository.saveAll(seed);
    }

    // ── Test 1: Search by movie title ─────────────────────────────────────

    @Test
    void shouldFindMoviesWhenSearchingByMovieTitle() throws Exception {
        MvcResult result = mvc.perform(get("/api/movies/search?q=dark&type=title"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        List<Map<String, Object>> movies = parseMovieList(result);
        assertFalse(movies.isEmpty(), "Should return at least one movie");
        movies.forEach(m ->
            assertTrue(
                m.get("title").toString().toLowerCase().contains("dark"),
                "Every result must contain 'dark' in title"
            )
        );
    }

    // ── Test 2: Search across all fields (Nolan is director + writer) ─────

    @Test
    void shouldSearchAcrossAllMovieFieldsAndReturnRelevantResults() throws Exception {
        MvcResult result = mvc.perform(get("/api/movies/search?q=nolan&type=all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        List<Map<String, Object>> movies = parseMovieList(result);
        assertFalse(movies.isEmpty(), "Should return Nolan movies");
        assertMoviesSortedCorrectly(movies);
    }

    // ── Test 3: Genre filter with OR logic + no text query ────────────────

    @Test
    void shouldReturnAllMoviesWhenNoFiltersAreAppliedAndFilterMoviesByGenres() throws Exception {
        MvcResult result = mvc.perform(get("/api/movies/search?genre=Action,Drama"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        List<Map<String, Object>> movies = parseMovieList(result);
        assertFalse(movies.isEmpty(), "Should return Action or Drama movies");

        // Every result must have Action OR Drama in its genre list
        movies.forEach(m -> {
            @SuppressWarnings("unchecked")
            List<String> genres = (List<String>) m.get("genre");
            assertTrue(
                genres.contains("Action") || genres.contains("Drama"),
                "Each movie must have Action or Drama genre, but got: " + genres
            );
        });

        assertMoviesSortedCorrectly(movies);
    }

    // ── Test 4: Rating + year range filter ───────────────────────────────

    @Test
    void shouldFilterMoviesByRatingAndYearRange() throws Exception {
        MvcResult result = mvc.perform(
                get("/api/movies/search?minRating=8.0&minYear=2000&maxYear=2020"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        List<Map<String, Object>> movies = parseMovieList(result);
        assertFalse(movies.isEmpty(), "Should return at least one movie in range");

        movies.forEach(m -> {
            double rating = ((Number) m.get("rating")).doubleValue();
            int year      = ((Number) m.get("year")).intValue();
            assertTrue(rating >= 8.0, "Rating must be >= 8.0 but was: " + rating);
            assertTrue(year >= 2000 && year <= 2020,
                "Year must be 2000-2020 but was: " + year);
        });

        assertMoviesSortedCorrectly(movies);
    }

    // ── Test 5: Search by actor/celebrity name ───────────────────────────

    @Test
    void shouldFindMoviesWhenSearchingByActorOrCelebrityName() throws Exception {
        // "robert" matches "Robert De Niro" in stars list
        MvcResult result = mvc.perform(get("/api/movies/search?q=robert&type=celebs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        List<Map<String, Object>> movies = parseMovieList(result);
        assertFalse(movies.isEmpty(), "Should find movies with 'robert' in stars");

        movies.forEach(m -> {
            @SuppressWarnings("unchecked")
            List<String> stars = (List<String>) m.get("stars");
            assertTrue(
                stars.stream().anyMatch(s -> s.toLowerCase().contains("robert")),
                "Each result must have 'robert' in stars, but got: " + stars
            );
        });
    }

    // ── Test 6: Combine director + minRating filter ───────────────────────

    @Test
    void shouldFilterMoviesByDirectorWriterAndCombineMultipleFiltersTogether() throws Exception {
        MvcResult result = mvc.perform(
                get("/api/movies/search?director=nolan&minRating=8.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        List<Map<String, Object>> movies = parseMovieList(result);
        assertFalse(movies.isEmpty(), "Should return Nolan movies with rating >= 8.0");

        movies.forEach(m -> {
            String director = m.get("director").toString();
            double rating   = ((Number) m.get("rating")).doubleValue();
            assertTrue(director.toLowerCase().contains("nolan"),
                "Director must contain 'nolan' but was: " + director);
            assertTrue(rating >= 8.0,
                "Rating must be >= 8.0 but was: " + rating);
        });

        assertMoviesSortedCorrectly(movies);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /**
     * Verifies that the movie list is sorted by rating DESC.
     * Adjacent pairs must satisfy r[i] >= r[i+1].
     */
    private void assertMoviesSortedCorrectly(List<Map<String, Object>> movies) {
        for (int i = 0; i < movies.size() - 1; i++) {
            double r1 = ((Number) movies.get(i).get("rating")).doubleValue();
            double r2 = ((Number) movies.get(i + 1).get("rating")).doubleValue();
            assertTrue(r1 >= r2,
                "Movies should be sorted by rating DESC but got " + r1 + " before " + r2);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseMovieList(MvcResult result) throws Exception {
        String json = result.getResponse().getContentAsString();
        return objectMapper.readValue(json, List.class);
    }

    // ── Factory ───────────────────────────────────────────────────────────

    private Movie movie(String title, int year, double rating, int popularity,
                        List<String> genre, String director,
                        List<String> writers, List<String> stars) {
        return Movie.builder()
                .title(title).year(year).rating(rating).popularity(popularity)
                .genre(genre).director(director).writers(writers).stars(stars)
                .type(MovieType.movie).status(MovieStatus.published)
                .description("Test description for " + title)
                .duration("2h 0m")
                .build();
    }
}
