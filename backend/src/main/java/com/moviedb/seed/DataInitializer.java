package com.moviedb.seed;

import com.moviedb.entity.Movie;
import com.moviedb.entity.User;
import com.moviedb.enums.MovieStatus;
import com.moviedb.enums.MovieType;
import com.moviedb.repository.MovieRepository;
import com.moviedb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seeds initial data on first startup (when tables are empty).
 *
 * WHY @EventListener(ApplicationReadyEvent)?
 *   ApplicationReadyEvent fires after the full Spring context and all beans
 *   are ready — including JPA, connection pool, and schema creation. Using
 *   @PostConstruct on a bean could race against Hibernate DDL execution.
 *   ApplicationReadyEvent is the safest hook for startup data seeding.
 *
 * WHY check movieRepo.count() == 0?
 *   Idempotent seeding: the initializer can be called many times (tests,
 *   hot reload) without duplicating data. A count check is simpler than
 *   upsert logic for seed data.
 *
 * Seed catalogue: 42 movies + series with varied genres, years (1972-2024),
 * and ratings (6.9-9.5) — includes a dedicated 2021-2024 block of recent
 * releases so trending/year-filter queries return meaningful results.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final MovieRepository movieRepository;
    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (movieRepository.count() == 0) {
            seedMovies();
            log.info("Seeded {} movies", movieRepository.count());
        }
        if (userRepository.count() == 0) {
            seedUsers();
            log.info("Seeded users: admin, testuser");
        }
    }

    // ── Movies ────────────────────────────────────────────────────────────

    private void seedMovies() {
        List<Movie> movies = List.of(

            // ── Spec-required movies ──────────────────────────────────────

            Movie.builder()
                .title("The Dark Knight")
                .year(2008).duration("2h 32m").rating(9.0).popularity(98)
                .description("Batman faces the Joker, a criminal mastermind who plunges Gotham into anarchy.")
                .director("Christopher Nolan")
                .writers(List.of("Jonathan Nolan", "Christopher Nolan"))
                .stars(List.of("Christian Bale", "Heath Ledger", "Aaron Eckhart"))
                .genre(List.of("Action", "Crime", "Drama"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("The Shawshank Redemption")
                .year(1994).duration("2h 22m").rating(9.3).popularity(97)
                .description("Two imprisoned men bond over years, finding solace and redemption through acts of decency.")
                .director("Frank Darabont")
                .writers(List.of("Stephen King", "Frank Darabont"))
                .stars(List.of("Tim Robbins", "Morgan Freeman", "Bob Gunton"))
                .genre(List.of("Drama"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Good Will Hunting")
                .year(1997).duration("2h 6m").rating(8.3).popularity(85)
                .description("A janitor at MIT hides his genius until a therapist helps him find direction.")
                .director("Gus Van Sant")
                .writers(List.of("Matt Damon", "Ben Affleck"))
                .stars(List.of("Matt Damon", "Robin Williams", "Ben Affleck"))
                .genre(List.of("Drama"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Oppenheimer")
                .year(2023).duration("3h 0m").rating(8.3).popularity(92)
                .description("The story of American scientist J. Robert Oppenheimer and the development of the atomic bomb.")
                .director("Christopher Nolan")
                .writers(List.of("Christopher Nolan"))
                .stars(List.of("Cillian Murphy", "Emily Blunt", "Robert Downey Jr."))
                .genre(List.of("Biography", "Drama", "History"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Gladiator")
                .year(2000).duration("2h 35m").rating(8.5).popularity(88)
                .description("A former Roman general sets out to exact vengeance against the corrupt emperor who murdered his family.")
                .director("Ridley Scott")
                .writers(List.of("David Franzoni", "John Logan"))
                .stars(List.of("Russell Crowe", "Joaquin Phoenix", "Connie Nielsen"))
                .genre(List.of("Action", "Drama"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            // ── Additional 15+ movies ──────────────────────────────────────

            Movie.builder()
                .title("The Godfather")
                .year(1972).duration("2h 55m").rating(9.2).popularity(96)
                .description("The aging patriarch of an organized crime dynasty transfers control of his empire to his reluctant son.")
                .director("Francis Ford Coppola")
                .writers(List.of("Mario Puzo", "Francis Ford Coppola"))
                .stars(List.of("Marlon Brando", "Al Pacino", "James Caan"))
                .genre(List.of("Crime", "Drama"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Pulp Fiction")
                .year(1994).duration("2h 34m").rating(8.9).popularity(94)
                .description("The lives of two mob hitmen, a boxer, a gangster and his wife intertwine in four tales of violence and redemption.")
                .director("Quentin Tarantino")
                .writers(List.of("Quentin Tarantino", "Roger Avary"))
                .stars(List.of("John Travolta", "Uma Thurman", "Samuel L. Jackson"))
                .genre(List.of("Crime", "Drama"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Inception")
                .year(2010).duration("2h 28m").rating(8.8).popularity(93)
                .description("A thief who steals corporate secrets through dream-sharing technology is given the task of planting an idea.")
                .director("Christopher Nolan")
                .writers(List.of("Christopher Nolan"))
                .stars(List.of("Leonardo DiCaprio", "Joseph Gordon-Levitt", "Elliot Page"))
                .genre(List.of("Action", "Sci-Fi", "Thriller"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("The Matrix")
                .year(1999).duration("2h 16m").rating(8.7).popularity(91)
                .description("A computer hacker learns from mysterious rebels about the true nature of his reality.")
                .director("Lana Wachowski")
                .writers(List.of("Lana Wachowski", "Lilly Wachowski"))
                .stars(List.of("Keanu Reeves", "Laurence Fishburne", "Carrie-Anne Moss"))
                .genre(List.of("Action", "Sci-Fi"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Forrest Gump")
                .year(1994).duration("2h 22m").rating(8.8).popularity(95)
                .description("The presidencies of Kennedy and Johnson, Vietnam, Watergate, and other events unfold from the perspective of an Alabama man.")
                .director("Robert Zemeckis")
                .writers(List.of("Winston Groom", "Eric Roth"))
                .stars(List.of("Tom Hanks", "Robin Wright", "Gary Sinise"))
                .genre(List.of("Drama", "Romance"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Fight Club")
                .year(1999).duration("2h 19m").rating(8.8).popularity(92)
                .description("An insomniac office worker and a devil-may-care soap maker form an underground fight club.")
                .director("David Fincher")
                .writers(List.of("Jim Uhls", "Chuck Palahniuk"))
                .stars(List.of("Brad Pitt", "Edward Norton", "Helena Bonham Carter"))
                .genre(List.of("Drama", "Thriller"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Interstellar")
                .year(2014).duration("2h 49m").rating(8.6).popularity(90)
                .description("A team of explorers travel through a wormhole in space to ensure humanity's survival.")
                .director("Christopher Nolan")
                .writers(List.of("Jonathan Nolan", "Christopher Nolan"))
                .stars(List.of("Matthew McConaughey", "Anne Hathaway", "Jessica Chastain"))
                .genre(List.of("Adventure", "Drama", "Sci-Fi"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Parasite")
                .year(2019).duration("2h 12m").rating(8.5).popularity(87)
                .description("Greed and class discrimination threaten the newly formed symbiotic relationship between the wealthy Park family and the destitute Kim clan.")
                .director("Bong Joon-ho")
                .writers(List.of("Bong Joon-ho", "Han Jin-won"))
                .stars(List.of("Song Kang-ho", "Lee Sun-kyun", "Cho Yeo-jeong"))
                .genre(List.of("Comedy", "Drama", "Thriller"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Avengers: Endgame")
                .year(2019).duration("3h 1m").rating(8.4).popularity(96)
                .description("After the devastating events of Infinity War, the Avengers assemble once more to reverse Thanos's actions.")
                .director("Anthony Russo")
                .writers(List.of("Christopher Markus", "Stephen McFeely"))
                .stars(List.of("Robert Downey Jr.", "Chris Evans", "Mark Ruffalo"))
                .genre(List.of("Action", "Adventure", "Sci-Fi"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Joker")
                .year(2019).duration("2h 2m").rating(8.4).popularity(86)
                .description("A mentally troubled comedian embarks on a downward spiral leading to the creation of an iconic villain.")
                .director("Todd Phillips")
                .writers(List.of("Todd Phillips", "Scott Silver"))
                .stars(List.of("Joaquin Phoenix", "Robert De Niro", "Zazie Beetz"))
                .genre(List.of("Crime", "Drama", "Thriller"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("La La Land")
                .year(2016).duration("2h 8m").rating(8.0).popularity(82)
                .description("While navigating their careers in Los Angeles, a pianist and an actress fall in love.")
                .director("Damien Chazelle")
                .writers(List.of("Damien Chazelle"))
                .stars(List.of("Ryan Gosling", "Emma Stone", "John Legend"))
                .genre(List.of("Drama", "Music", "Romance"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("1917")
                .year(2019).duration("1h 59m").rating(8.3).popularity(84)
                .description("Two British soldiers are sent on a seemingly impossible mission to deliver a message that could save 1,600 lives.")
                .director("Sam Mendes")
                .writers(List.of("Sam Mendes", "Krysty Wilson-Cairns"))
                .stars(List.of("George MacKay", "Dean-Charles Chapman", "Mark Strong"))
                .genre(List.of("Drama", "War"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Mad Max: Fury Road")
                .year(2015).duration("2h 0m").rating(8.1).popularity(83)
                .description("In a post-apocalyptic wasteland, a woman rebels against a tyrannical ruler.")
                .director("George Miller")
                .writers(List.of("George Miller", "Brendan McCarthy"))
                .stars(List.of("Tom Hardy", "Charlize Theron", "Nicholas Hoult"))
                .genre(List.of("Action", "Adventure", "Sci-Fi"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Get Out")
                .year(2017).duration("1h 44m").rating(7.7).popularity(78)
                .description("A young African-American visits his white girlfriend's parents for the weekend, where his simmering uneasiness gives way to a full-on revelation.")
                .director("Jordan Peele")
                .writers(List.of("Jordan Peele"))
                .stars(List.of("Daniel Kaluuya", "Allison Williams", "Bradley Whitford"))
                .genre(List.of("Horror", "Mystery", "Thriller"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("The Revenant")
                .year(2015).duration("2h 36m").rating(8.0).popularity(80)
                .description("A frontiersman on a fur trading expedition fights for survival after being mauled by a bear.")
                .director("Alejandro G. Iñárritu")
                .writers(List.of("Mark L. Smith", "Alejandro G. Iñárritu"))
                .stars(List.of("Leonardo DiCaprio", "Tom Hardy", "Will Poulter"))
                .genre(List.of("Action", "Adventure", "Drama"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Dune")
                .year(2021).duration("2h 35m").rating(8.0).popularity(87)
                .description("A noble family becomes embroiled in a war for control over the galaxy's most valuable asset.")
                .director("Denis Villeneuve")
                .writers(List.of("Jon Spaihts", "Denis Villeneuve"))
                .stars(List.of("Timothée Chalamet", "Rebecca Ferguson", "Oscar Isaac"))
                .genre(List.of("Action", "Adventure", "Drama", "Sci-Fi"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Breaking Bad")
                .year(2008).duration("47m per episode").rating(9.5).popularity(99)
                .description("A high school chemistry teacher diagnosed with inoperable lung cancer turns to manufacturing methamphetamine.")
                .director("Vince Gilligan")
                .writers(List.of("Vince Gilligan"))
                .stars(List.of("Bryan Cranston", "Aaron Paul", "Anna Gunn"))
                .genre(List.of("Crime", "Drama", "Thriller"))
                .type(MovieType.series).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Stranger Things")
                .year(2016).duration("51m per episode").rating(8.7).popularity(93)
                .description("When a young boy disappears, his mother and friends uncover a secret government experiment.")
                .director("Matt Duffer")
                .writers(List.of("Matt Duffer", "Ross Duffer"))
                .stars(List.of("Millie Bobby Brown", "Finn Wolfhard", "Winona Ryder"))
                .genre(List.of("Drama", "Fantasy", "Horror", "Sci-Fi"))
                .type(MovieType.series).status(MovieStatus.published).build(),

            // ── Last 5 years (2021-2026) ──────────────────────────────────

            Movie.builder()
                .title("The Batman")
                .year(2022).duration("2h 56m").rating(7.8).popularity(88)
                .description("When a sadistic serial killer begins murdering key political figures in Gotham, Batman is forced to investigate the city's hidden corruption.")
                .director("Matt Reeves")
                .writers(List.of("Matt Reeves", "Peter Craig"))
                .stars(List.of("Robert Pattinson", "Zoë Kravitz", "Jeffrey Wright"))
                .genre(List.of("Action", "Crime", "Drama"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Everything Everywhere All at Once")
                .year(2022).duration("2h 19m").rating(7.8).popularity(86)
                .description("A middle-aged Chinese immigrant is swept up into an insane adventure in which she alone can save existence by exploring other universes.")
                .director("Daniel Kwan")
                .writers(List.of("Daniel Kwan", "Daniel Scheinert"))
                .stars(List.of("Michelle Yeoh", "Ke Huy Quan", "Jamie Lee Curtis"))
                .genre(List.of("Action", "Adventure", "Comedy", "Sci-Fi"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Top Gun: Maverick")
                .year(2022).duration("2h 10m").rating(8.3).popularity(91)
                .description("After more than thirty years of service as one of the Navy's top aviators, Pete Mitchell is pushed to train a detachment of graduates for a specialized mission.")
                .director("Joseph Kosinski")
                .writers(List.of("Ehren Kruger", "Eric Warren Singer", "Christopher McQuarrie"))
                .stars(List.of("Tom Cruise", "Miles Teller", "Jennifer Connelly"))
                .genre(List.of("Action", "Drama"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("RRR")
                .year(2022).duration("3h 2m").rating(7.9).popularity(85)
                .description("A fictitious story about two legendary revolutionaries and their journey away from home before they started fighting for their country in 1920s.")
                .director("S.S. Rajamouli")
                .writers(List.of("V. Vijayendra Prasad"))
                .stars(List.of("N.T. Rama Rao Jr.", "Ram Charan", "Alia Bhatt"))
                .genre(List.of("Action", "Drama"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("The Fabelmans")
                .year(2022).duration("2h 31m").rating(7.7).popularity(74)
                .description("Growing up in post-World War II era Arizona, a young man named Sammy Fabelman discovers a shattering family secret and explores how the power of films can help him see the truth.")
                .director("Steven Spielberg")
                .writers(List.of("Steven Spielberg", "Tony Kushner"))
                .stars(List.of("Michelle Williams", "Paul Dano", "Gabriel LaBelle"))
                .genre(List.of("Drama"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Barbie")
                .year(2023).duration("1h 54m").rating(6.9).popularity(89)
                .description("Barbie and Ken are having the time of their lives in the colorful and seemingly perfect world of Barbie Land, until they begin questioning their existence.")
                .director("Greta Gerwig")
                .writers(List.of("Greta Gerwig", "Noah Baumbach"))
                .stars(List.of("Margot Robbie", "Ryan Gosling", "America Ferrera"))
                .genre(List.of("Adventure", "Comedy", "Fantasy"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Guardians of the Galaxy Vol. 3")
                .year(2023).duration("2h 30m").rating(7.9).popularity(87)
                .description("Still reeling from the loss of Gamora, Peter Quill must rally his team around him to defend the universe along with protecting one of their own.")
                .director("James Gunn")
                .writers(List.of("James Gunn"))
                .stars(List.of("Chris Pratt", "Zoe Saldana", "Bradley Cooper"))
                .genre(List.of("Action", "Adventure", "Comedy", "Sci-Fi"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Killers of the Flower Moon")
                .year(2023).duration("3h 26m").rating(7.7).popularity(79)
                .description("Members of the Osage Nation are murdered under mysterious circumstances in 1920s Oklahoma sparking a major FBI investigation.")
                .director("Martin Scorsese")
                .writers(List.of("Eric Roth", "Martin Scorsese"))
                .stars(List.of("Leonardo DiCaprio", "Robert De Niro", "Lily Gladstone"))
                .genre(List.of("Crime", "Drama", "History"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Poor Things")
                .year(2023).duration("2h 21m").rating(8.0).popularity(82)
                .description("The incredible tale and fantastical adventures of Bella Baxter, a young woman brought back to life by the brilliant and unorthodox scientist Dr. Godwin Baxter.")
                .director("Yorgos Lanthimos")
                .writers(List.of("Tony McNamara"))
                .stars(List.of("Emma Stone", "Mark Ruffalo", "Willem Dafoe"))
                .genre(List.of("Comedy", "Drama", "Fantasy", "Romance"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Dune: Part Two")
                .year(2024).duration("2h 46m").rating(8.5).popularity(93)
                .description("Paul Atreides unites with Chani and the Fremen while on a warpath of revenge against the conspirators who destroyed his family.")
                .director("Denis Villeneuve")
                .writers(List.of("Denis Villeneuve", "Jon Spaihts"))
                .stars(List.of("Timothée Chalamet", "Zendaya", "Rebecca Ferguson"))
                .genre(List.of("Action", "Adventure", "Drama", "Sci-Fi"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Deadpool & Wolverine")
                .year(2024).duration("2h 7m").rating(7.7).popularity(90)
                .description("Deadpool is offered a chance to join the Time Variance Authority but instead recruits a reluctant Wolverine to help save his world.")
                .director("Shawn Levy")
                .writers(List.of("Ryan Reynolds", "Shawn Levy", "Rhett Reese"))
                .stars(List.of("Ryan Reynolds", "Hugh Jackman", "Emma Corrin"))
                .genre(List.of("Action", "Comedy", "Sci-Fi"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Inside Out 2")
                .year(2024).duration("1h 40m").rating(7.8).popularity(88)
                .description("Joy and the other emotions inside Riley's head face a new challenge when Anxiety and new emotions show up unexpectedly.")
                .director("Kelsey Mann")
                .writers(List.of("Meg LeFauve", "Dave Holstein"))
                .stars(List.of("Amy Poehler", "Maya Hawke", "Kensington Tallman"))
                .genre(List.of("Animation", "Adventure", "Comedy", "Family"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Alien: Romulus")
                .year(2024).duration("1h 59m").rating(7.3).popularity(82)
                .description("While scavenging the deep ends of a derelict space station, a group of young space colonists come face to face with the most terrifying life form in the universe.")
                .director("Fede Álvarez")
                .writers(List.of("Fede Álvarez", "Rodo Sayagues"))
                .stars(List.of("Cailee Spaeny", "David Jonsson", "Archie Renaux"))
                .genre(List.of("Horror", "Sci-Fi", "Thriller"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Conclave")
                .year(2024).duration("2h 0m").rating(7.5).popularity(78)
                .description("A cardinal oversees the top-secret election of a new Pope, uncovering shocking secrets about the candidates.")
                .director("Edward Berger")
                .writers(List.of("Peter Straughan"))
                .stars(List.of("Ralph Fiennes", "Stanley Tucci", "John Lithgow"))
                .genre(List.of("Drama", "Mystery", "Thriller"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Anora")
                .year(2024).duration("2h 18m").rating(7.9).popularity(76)
                .description("A young sex worker from Brooklyn gets her chance at a Cinderella story when she meets and impulsively marries the son of an oligarch.")
                .director("Sean Baker")
                .writers(List.of("Sean Baker"))
                .stars(List.of("Mikey Madison", "Yura Borisov", "Karren Karagulian"))
                .genre(List.of("Comedy", "Drama", "Romance"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Nosferatu")
                .year(2024).duration("2h 12m").rating(7.6).popularity(80)
                .description("A gothic tale of obsession between a haunted young woman and the terrifying vampire infatuated with her.")
                .director("Robert Eggers")
                .writers(List.of("Robert Eggers"))
                .stars(List.of("Lily-Rose Depp", "Nicholas Hoult", "Bill Skarsgård"))
                .genre(List.of("Fantasy", "Horror", "Romance"))
                .type(MovieType.movie).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Severance")
                .year(2022).duration("1h 0m per episode").rating(9.0).popularity(91)
                .description("Mark leads a team of office workers whose memories have been surgically divided between their work and personal lives, creating a perfect balance.")
                .director("Ben Stiller")
                .writers(List.of("Dan Erickson"))
                .stars(List.of("Adam Scott", "Zach Cherry", "Britt Lower"))
                .genre(List.of("Drama", "Mystery", "Sci-Fi", "Thriller"))
                .type(MovieType.series).status(MovieStatus.published).build(),

            Movie.builder()
                .title("The Last of Us")
                .year(2023).duration("1h per episode").rating(8.8).popularity(95)
                .description("After a global pandemic destroys civilization, a hardened survivor takes charge of a 14-year-old girl who may be humanity's last hope.")
                .director("Neil Druckmann")
                .writers(List.of("Craig Mazin", "Neil Druckmann"))
                .stars(List.of("Pedro Pascal", "Bella Ramsey", "Gabriel Luna"))
                .genre(List.of("Action", "Adventure", "Drama", "Horror"))
                .type(MovieType.series).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Shogun")
                .year(2024).duration("1h per episode").rating(8.6).popularity(89)
                .description("In feudal Japan, a shipwrecked English navigator becomes a key player in a deadly power struggle between warring warlords.")
                .director("Rachel Kondo")
                .writers(List.of("Rachel Kondo", "Caillin Pully"))
                .stars(List.of("Hiroyuki Sanada", "Cosmo Jarvis", "Anna Sawai"))
                .genre(List.of("Action", "Drama", "History"))
                .type(MovieType.series).status(MovieStatus.published).build(),

            Movie.builder()
                .title("Fallout")
                .year(2024).duration("1h per episode").rating(8.5).popularity(90)
                .description("Based on the Bethesda game series, Lucy MacLean leaves her underground vault to navigate the dystopian wasteland of 2296 Los Angeles.")
                .director("Jonathan Nolan")
                .writers(List.of("Geneva Robertson-Dworet", "Graham Wagner"))
                .stars(List.of("Ella Purnell", "Aaron Moten", "Walton Goggins"))
                .genre(List.of("Action", "Adventure", "Drama", "Sci-Fi"))
                .type(MovieType.series).status(MovieStatus.published).build(),

            // Upcoming (not published — tests only return published)
            Movie.builder()
                .title("Avatar 3")
                .year(2025).duration("TBD").rating(null).popularity(75)
                .description("The next chapter in the Avatar saga continues on Pandora.")
                .director("James Cameron")
                .writers(List.of("James Cameron"))
                .stars(List.of("Sam Worthington", "Zoe Saldana"))
                .genre(List.of("Action", "Adventure", "Sci-Fi"))
                .type(MovieType.movie).status(MovieStatus.upcoming).build()
        );

        movieRepository.saveAll(movies);
    }

    // ── Users ─────────────────────────────────────────────────────────────

    private void seedUsers() {
        User admin = User.builder()
                .username("admin")
                .email("admin@moviedb.com")
                .password(passwordEncoder.encode("admin123"))
                .role("ROLE_ADMIN")
                .build();

        User testUser = User.builder()
                .username("testuser")
                .email("testuser@moviedb.com")
                .password(passwordEncoder.encode("test123"))
                .role("ROLE_USER")
                .build();

        userRepository.saveAll(List.of(admin, testUser));
    }
}
