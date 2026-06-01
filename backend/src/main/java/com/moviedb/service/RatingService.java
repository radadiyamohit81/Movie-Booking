package com.moviedb.service;

import com.moviedb.dto.RatingRequest;
import com.moviedb.model.Rating;
import com.moviedb.exception.ResourceNotFoundException;
import com.moviedb.repository.MovieRepository;
import com.moviedb.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final MovieRepository  movieRepository;

    @Transactional
    public Rating rateMovie(Long userId, RatingRequest req) {
        if (!movieRepository.existsById(req.getMovieId())) {
            throw new ResourceNotFoundException("Movie", req.getMovieId());
        }

        // Upsert: if user already rated this movie, update the existing rating
        Rating rating = ratingRepository
                .findByUserIdAndMovieId(userId, req.getMovieId())
                .orElseGet(() -> Rating.builder()
                        .userId(userId)
                        .movieId(req.getMovieId())
                        .build());

        rating.setRating(req.getRating());
        return ratingRepository.save(rating);
    }

    @Transactional(readOnly = true)
    public List<Rating> getRatingsForMovie(Long movieId) {
        return ratingRepository.findByMovieId(movieId);
    }

    @Transactional
    public void deleteRating(Long ratingId, Long userId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating", ratingId));

        if (!rating.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own ratings");
        }
        ratingRepository.deleteById(ratingId);
    }
}
