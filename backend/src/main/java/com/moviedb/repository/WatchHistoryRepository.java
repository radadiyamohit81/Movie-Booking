package com.moviedb.repository;

import com.moviedb.entity.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {

    List<WatchHistory> findByUserIdOrderByWatchedAtDesc(Long userId);
}
