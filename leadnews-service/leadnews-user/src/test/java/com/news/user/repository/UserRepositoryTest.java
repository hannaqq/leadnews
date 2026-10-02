package com.news.user.repository;

import com.news.model.user.pojos.ApUser;
import com.news.model.user.pojos.ApUserFan;
import com.news.model.user.pojos.ApUserFollow;
import com.news.model.user.pojos.ApUserRealname;
import com.news.user.service.impl.UserFollowPersistenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(UserFollowPersistenceService.class)
class UserRepositoryTest {

    @Autowired private ApUserRepository userRepository;
    @Autowired private ApUserRealnameRepository realnameRepository;
    @Autowired private ApUserFollowRepository followRepository;
    @Autowired private ApUserFanRepository fanRepository;
    @Autowired private UserFollowPersistenceService persistenceService;

    @Test
    void findsUserByPhone() {
        ApUser user = new ApUser();
        user.setPhone("5551000");
        userRepository.saveAndFlush(user);

        assertEquals(user.getId(), userRepository.findByPhone("5551000").orElseThrow().getId());
    }

    @Test
    void locksUserBeforeChangingFollowRelations() {
        ApUser user = new ApUser();
        user.setPhone("5552000");
        userRepository.saveAndFlush(user);

        assertEquals(user.getId(), userRepository.findByIdForUpdate(user.getId()).orElseThrow().getId());
    }

    @Test
    void filtersAndPagesRealnameReviewsUsingZeroBasedPageable() {
        saveRealname("older", (short) 1, new Date(1_000));
        saveRealname("newer", (short) 1, new Date(2_000));
        saveRealname("other", (short) 2, new Date(3_000));

        var page = realnameRepository.findForReview(
                null, (short) 1,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdTime")));

        assertEquals(2, page.getTotalElements());
        assertEquals("newer", page.getContent().get(0).getName());
    }

    @Test
    void supportsFollowExistenceAndConditionalDeletes() {
        ApUserFollow follow = new ApUserFollow();
        follow.setUserId(10);
        follow.setFollowId(20);
        followRepository.saveAndFlush(follow);
        ApUserFan fan = new ApUserFan();
        fan.setFansId(10);
        fan.setUserId(20);
        fanRepository.saveAndFlush(fan);

        assertTrue(followRepository.existsByUserIdAndFollowId(10, 20));
        assertEquals(1, followRepository.deleteByUserIdAndFollowId(10, 20));
        assertEquals(1, fanRepository.deleteByFansIdAndUserId(10, 20));
        assertFalse(followRepository.existsByUserIdAndFollowId(10, 20));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void serializesConcurrentFollowCreation() throws Exception {
        ApUser user = new ApUser();
        user.setPhone("5553000");
        user = userRepository.saveAndFlush(user);
        int userId = user.getId();
        CyclicBarrier start = new CyclicBarrier(2);
        var executor = Executors.newFixedThreadPool(2);

        try {
            var first = executor.submit(() -> {
                start.await();
                return persistenceService.createIfAbsent(follow(userId, 20), fan(userId, 20));
            });
            var second = executor.submit(() -> {
                start.await();
                return persistenceService.createIfAbsent(follow(userId, 20), fan(userId, 20));
            });

            int winners = (first.get() ? 1 : 0) + (second.get() ? 1 : 0);
            assertEquals(1, winners);
            assertEquals(1, followRepository.findAll().stream()
                    .filter(item -> item.getUserId().equals(userId) && item.getFollowId().equals(20))
                    .count());
            assertEquals(1, fanRepository.findAll().stream()
                    .filter(item -> item.getFansId().equals(userId) && item.getUserId().equals(20))
                    .count());
        } finally {
            executor.shutdownNow();
        }
    }

    private void saveRealname(String name, short status, Date createdTime) {
        ApUserRealname realname = new ApUserRealname();
        realname.setName(name);
        realname.setStatus(status);
        realname.setCreatedTime(createdTime);
        realnameRepository.save(realname);
    }

    private static ApUserFollow follow(int userId, int followId) {
        ApUserFollow follow = new ApUserFollow();
        follow.setUserId(userId);
        follow.setFollowId(followId);
        return follow;
    }

    private static ApUserFan fan(int fansId, int userId) {
        ApUserFan fan = new ApUserFan();
        fan.setFansId(fansId);
        fan.setUserId(userId);
        return fan;
    }
}
