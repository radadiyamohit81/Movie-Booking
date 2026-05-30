# 🎬 MovieDB

A full-stack **IMDb-style movie database** built with Java 21 + Spring Boot 3 (backend) and React 18 (frontend). Features JWT authentication, advanced search with JPA Specifications, ratings, reviews, watchlists, custom lists, follow system, and an admin panel — all seeded with 42 real movies and series.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.2.5, Spring Security, Spring Data JPA |
| Database | H2 (file-based, dev) — drop-in ready for PostgreSQL |
| Auth | JWT (JJWT 0.11.5), BCrypt strength 12 |
| Frontend | React 18, React Router v6, Axios |
| Build | Maven (backend), npm (frontend) |

---

## Project Structure

```
Movie/
├── backend/                        # Spring Boot API
│   ├── src/main/java/com/moviedb/
│   │   ├── config/                 # SecurityConfig, CorsConfig
│   │   ├── controller/             # 10 REST controllers
│   │   ├── dto/                    # Request/Response DTOs
│   │   ├── entity/                 # 9 JPA entities
│   │   ├── enums/                  # MovieType, MovieStatus
│   │   ├── exception/              # GlobalExceptionHandler
│   │   ├── repository/             # 9 repositories + JPA Specifications
│   │   ├── security/               # JwtUtil, JwtFilter, UserDetailsService
│   │   ├── seed/                   # DataInitializer (42 movies on first boot)
│   │   └── service/                # 8 business-logic services
│   └── src/main/resources/
│       ├── application.yml         # Dev config (H2 file-based, JWT settings)
│       └── application-test.yml    # Test config (H2 in-memory)
├── frontend/                       # React SPA
│   ├── public/
│   └── src/
│       ├── api/                    # Axios instances + API calls
│       ├── components/             # Navbar, MovieCard, FilterPanel, etc.
│       ├── context/                # AuthContext (JWT in localStorage)
│       ├── hooks/                  # useAuth, useMovies
│       ├── pages/                  # Home, Search, MovieDetail, Profile, Admin…
│       └── utils/                  # helpers (formatRating, formatDate…)
├── .gitignore
├── Makefile                        # One-command runner
└── README.md
```

---

## Quick Start — One Command

```bash
make dev
```

This installs frontend deps (first run), then starts both backend and frontend in parallel.

**Requirements:** Java 21+, Maven 3.9+, Node 18+

---

## Manual Setup

### 1 — Backend

```bash
cd backend
mvn spring-boot:run
```

API runs at **http://localhost:8080**  
H2 Console: **http://localhost:8080/h2-console**  
JDBC URL: `jdbc:h2:file:./data/moviedb` · User: `sa` · Password: *(empty)*

### 2 — Frontend

```bash
cd frontend
npm install
npm start
```

App runs at **http://localhost:3000** (proxies API calls to `:8080` automatically)

---

## Seeded Demo Accounts

| Username | Password | Role |
|---|---|---|
| `testuser` | `test123` | ROLE_USER |
| `admin` | `admin123` | ROLE_ADMIN |

---

## Seeded Movie Data (42 titles)

Includes classics and recent releases across both movies and series:

**Classics (pre-2020):** The Godfather, Pulp Fiction, The Shawshank Redemption, Forrest Gump, Fight Club, The Matrix, Inception, Interstellar, The Dark Knight, Parasite, Avengers: Endgame, Joker, La La Land, 1917, Mad Max: Fury Road, Get Out, The Revenant, Gladiator, Good Will Hunting, Breaking Bad, Stranger Things

**Recent (2021–2024):** Dune, The Batman, Everything Everywhere All at Once, Top Gun: Maverick, RRR, The Fabelmans, Severance, Oppenheimer, Barbie, Guardians Vol. 3, Killers of the Flower Moon, Poor Things, The Last of Us, Dune: Part Two, Deadpool & Wolverine, Inside Out 2, Alien: Romulus, Conclave, Anora, Nosferatu, Shogun, Fallout

**Upcoming:** Avatar 3

---

## API Endpoints

### Auth
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register new user |
| POST | `/api/auth/login` | Public | Login, returns JWT |

### Movies
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/movies` | Public | List / search all movies |
| GET | `/api/movies/{id}` | Public | Movie detail |
| GET | `/api/movies/search` | Public | Advanced search (title, genre, year, rating, director, celebs) |

### Home
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/home/featured` | Public | Top-rated featured movies |
| GET | `/api/home/trending` | Public | Trending by popularity |
| GET | `/api/home/upcoming` | Public | Upcoming releases |

### User Features (JWT required)
| Method | Endpoint | Description |
|---|---|---|
| POST/DELETE | `/api/ratings/{movieId}` | Rate a movie (0–10) |
| POST/GET/DELETE | `/api/reviews/{movieId}` | Submit / list reviews |
| GET/POST/DELETE | `/api/watchlist` | Manage watchlist |
| GET/POST/DELETE | `/api/lists` | Custom lists |
| POST/DELETE | `/api/follow/{userId}` | Follow / unfollow |
| GET | `/api/notifications` | Notification feed |

### Admin (ROLE_ADMIN + JWT required)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/admin/movies` | Create movie |
| PUT | `/api/admin/movies/{id}` | Update movie |
| DELETE | `/api/admin/movies/{id}` | Delete movie |

---

## Key Architectural Decisions

### Why JPA Specifications (not JPQL) for search?
JPQL cannot express `:param IS NULL OR field = :param` checks on `@ElementCollection` columns. `JpaSpecificationExecutor<Movie>` lets each filter return `null` (= no-op predicate) when absent, composing cleanly at runtime.

### Why `@ElementCollection(fetch = FetchType.EAGER)` on genre/writers/stars?
Hibernate silently returns empty results from EXISTS subqueries on LAZY-loaded element collections. EAGER fetch ensures correlated subquery predicates see the data.

### Why `@Table(name = "app_user")`?
`user` is a reserved SQL keyword in H2 and PostgreSQL. Mapping to `app_user` avoids DDL errors across both databases.

### Why H2 file-based (not in-memory) for dev?
Persists seeded data between restarts. Switch to PostgreSQL by changing the JDBC URL in `application.yml` — no code changes required.

### Why BCrypt strength 12 (not default 10)?
Each +2 strength doubles hash computation time, significantly raising the cost of brute-force attacks.

---

## Running Tests

```bash
cd backend
mvn test
```

Tests use an in-memory H2 database (`application-test.yml`) with a fresh seed of 10 targeted movies. Covers: title search, genre filter, year range, rating range, celeb search, director + rating combined.

---

## Switching to PostgreSQL

1. Add PostgreSQL driver to `pom.xml`:
   ```xml
   <dependency>
     <groupId>org.postgresql</groupId>
     <artifactId>postgresql</artifactId>
     <scope>runtime</scope>
   </dependency>
   ```
2. Update `application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/moviedb
       username: your_user
       password: your_password
     jpa:
       database-platform: org.hibernate.dialect.PostgreSQLDialect
   ```

---

## Environment Variables (optional overrides)

| Variable | Default | Description |
|---|---|---|
| `JWT_SECRET` | *(see application.yml)* | Base64-encoded HS256 secret |
| `JWT_EXPIRATION` | `86400000` | Token TTL in ms (24 h) |
| `SPRING_DATASOURCE_URL` | H2 file URL | Override for production DB |

---

## License

MIT
