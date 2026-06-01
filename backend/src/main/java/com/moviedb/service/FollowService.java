package com.moviedb.service;

import com.moviedb.model.Follow;
import com.moviedb.model.Notification;
import com.moviedb.model.User;
import com.moviedb.exception.ResourceNotFoundException;
import com.moviedb.repository.FollowRepository;
import com.moviedb.repository.NotificationRepository;
import com.moviedb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository        followRepository;
    private final UserRepository          userRepository;
    private final NotificationRepository  notificationRepository;

    @Transactional
    public Follow followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }
        if (!userRepository.existsById(followingId)) {
            throw new ResourceNotFoundException("User", followingId);
        }
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new IllegalStateException("Already following this user");
        }

        Follow follow = Follow.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();
        followRepository.save(follow);

        // Notify the followed user
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", followerId));
        Notification notification = Notification.builder()
                .userId(followingId)
                .type("FOLLOW")
                .message(follower.getUsername() + " started following you")
                .read(false)
                .build();
        notificationRepository.save(notification);

        return follow;
    }

    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        if (!followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new ResourceNotFoundException(
                    "Follow relationship not found for user: " + followingId);
        }
        followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Transactional(readOnly = true)
    public List<Follow> getFollowers(Long userId) {
        return followRepository.findByFollowingId(userId);
    }

    @Transactional(readOnly = true)
    public List<Follow> getFollowing(Long userId) {
        return followRepository.findByFollowerId(userId);
    }
}
