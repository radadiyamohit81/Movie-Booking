package com.moviedb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MovieDB API — entry point.
 *
 * WHY @SpringBootApplication?
 *   Meta-annotation that combines:
 *   - @Configuration      → marks as bean definition source
 *   - @EnableAutoConfiguration → auto-wires Spring context from classpath
 *   - @ComponentScan      → scans com.moviedb.** for beans
 *
 * Architecture layers (outermost → innermost):
 *   Controller → Service → Repository → Entity
 *
 * Each layer has a single responsibility:
 *   Controller  — HTTP I/O, request validation, response mapping
 *   Service     — business logic, transaction boundaries
 *   Repository  — data access (Spring Data JPA + Specifications)
 *   Entity      — domain model, persistence mapping
 */
@SpringBootApplication
public class MoviedbApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoviedbApplication.class, args);
    }
}
