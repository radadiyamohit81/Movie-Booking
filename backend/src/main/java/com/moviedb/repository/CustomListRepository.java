package com.moviedb.repository;

import com.moviedb.model.CustomList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomListRepository extends JpaRepository<CustomList, Long> {

    List<CustomList> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<CustomList> findByIdAndUserId(Long id, Long userId);
}
