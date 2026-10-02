package com.news.user.service.impl;

import com.news.model.user.pojos.ApUser;
import com.news.model.user.pojos.ApUserFan;
import com.news.model.user.pojos.ApUserFollow;
import com.news.user.repository.ApUserFanRepository;
import com.news.user.repository.ApUserFollowRepository;
import com.news.user.repository.ApUserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserFollowPersistenceServiceTest {

    private final ApUserFollowRepository followRepository = mock(ApUserFollowRepository.class);
    private final ApUserFanRepository fanRepository = mock(ApUserFanRepository.class);
    private final ApUserRepository userRepository = mock(ApUserRepository.class);
    private final UserFollowPersistenceService service = new UserFollowPersistenceService(
            followRepository, fanRepository, userRepository);

    @Test
    void createsBothRelationsAfterLockingUser() {
        ApUserFollow follow = follow(10, 20);
        ApUserFan fan = new ApUserFan();
        when(userRepository.findByIdForUpdate(10)).thenReturn(Optional.of(new ApUser()));

        assertTrue(service.createIfAbsent(follow, fan));

        verify(userRepository).findByIdForUpdate(10);
        verify(followRepository).save(follow);
        verify(fanRepository).save(fan);
    }

    @Test
    void rejectsDuplicateAfterAcquiringLock() {
        ApUserFollow follow = follow(10, 20);
        ApUserFan fan = new ApUserFan();
        when(userRepository.findByIdForUpdate(10)).thenReturn(Optional.of(new ApUser()));
        when(followRepository.existsByUserIdAndFollowId(10, 20)).thenReturn(true);

        assertFalse(service.createIfAbsent(follow, fan));

        verify(followRepository, never()).save(follow);
        verify(fanRepository, never()).save(fan);
    }

    private static ApUserFollow follow(int userId, int followId) {
        ApUserFollow follow = new ApUserFollow();
        follow.setUserId(userId);
        follow.setFollowId(followId);
        return follow;
    }
}
