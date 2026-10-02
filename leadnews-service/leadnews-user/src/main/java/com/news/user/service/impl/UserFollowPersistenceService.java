package com.news.user.service.impl;

import com.news.model.user.pojos.ApUserFan;
import com.news.model.user.pojos.ApUserFollow;
import com.news.user.repository.ApUserFanRepository;
import com.news.user.repository.ApUserFollowRepository;
import com.news.user.repository.ApUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserFollowPersistenceService {

    private final ApUserFollowRepository followRepository;
    private final ApUserFanRepository fanRepository;
    private final ApUserRepository userRepository;

    @Transactional
    public boolean createIfAbsent(ApUserFollow follow, ApUserFan fan) {
        userRepository.findByIdForUpdate(follow.getUserId()).orElseThrow();
        if (followRepository.existsByUserIdAndFollowId(follow.getUserId(), follow.getFollowId())) {
            return false;
        }
        followRepository.save(follow);
        fanRepository.save(fan);
        return true;
    }

    @Transactional
    public void delete(Integer userId, Integer authorId) {
        userRepository.findByIdForUpdate(userId).orElseThrow();
        followRepository.deleteByUserIdAndFollowId(userId, authorId);
        fanRepository.deleteByFansIdAndUserId(userId, authorId);
    }
}
