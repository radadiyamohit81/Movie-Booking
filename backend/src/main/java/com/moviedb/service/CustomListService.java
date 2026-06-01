package com.moviedb.service;

import com.moviedb.dto.AddMovieToListRequest;
import com.moviedb.dto.CustomListRequest;
import com.moviedb.model.CustomList;
import com.moviedb.exception.ResourceNotFoundException;
import com.moviedb.repository.CustomListRepository;
import com.moviedb.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomListService {

    private final CustomListRepository customListRepository;
    private final MovieRepository      movieRepository;

    @Transactional(readOnly = true)
    public List<CustomList> getLists(Long userId) {
        return customListRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public CustomList createList(Long userId, CustomListRequest req) {
        CustomList list = CustomList.builder()
                .userId(userId)
                .name(req.getName())
                .isPublic(req.getIsPublic() != null ? req.getIsPublic() : false)
                .build();
        return customListRepository.save(list);
    }

    @Transactional
    public CustomList updateList(Long listId, Long userId, CustomListRequest req) {
        CustomList list = customListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("List", listId));

        if (req.getName()     != null) list.setName(req.getName());
        if (req.getIsPublic() != null) list.setIsPublic(req.getIsPublic());
        return customListRepository.save(list);
    }

    @Transactional
    public void deleteList(Long listId, Long userId) {
        CustomList list = customListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("List", listId));
        customListRepository.delete(list);
    }

    @Transactional
    public CustomList addMovieToList(Long listId, Long userId, AddMovieToListRequest req) {
        if (!movieRepository.existsById(req.getMovieId())) {
            throw new ResourceNotFoundException("Movie", req.getMovieId());
        }
        CustomList list = customListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("List", listId));

        if (!list.getMovieIds().contains(req.getMovieId())) {
            list.getMovieIds().add(req.getMovieId());
            customListRepository.save(list);
        }
        return list;
    }
}
