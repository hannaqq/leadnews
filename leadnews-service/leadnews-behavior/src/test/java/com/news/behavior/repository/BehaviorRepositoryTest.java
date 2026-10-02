package com.news.behavior.repository;

import com.news.model.behavior.pojos.ApBehaviorEntry;
import com.news.model.behavior.pojos.ApFollowBehavior;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class BehaviorRepositoryTest {

    @Autowired
    private ApBehaviorEntryRepository entryRepository;

    @Autowired
    private ApFollowBehaviorRepository followRepository;

    @Test
    void findsEntryByExternalIdAndType() {
        ApBehaviorEntry entry = new ApBehaviorEntry();
        entry.setEntryId(42);
        entry.setType((short) 1);
        entry.setCreatedTime(new Date());
        entryRepository.saveAndFlush(entry);

        ApBehaviorEntry result = entryRepository.findByEntryIdAndType(42, (short) 1)
                .orElseThrow();

        assertTrue(result.getId() > 0);
        assertEquals(42, result.getEntryId());
    }

    @Test
    void persistsFollowBehaviorWithIdentityId() {
        ApFollowBehavior follow = new ApFollowBehavior();
        follow.setEntryId(42);
        follow.setArticleId(100L);
        follow.setFollowId(7);
        follow.setCreatedTime(new Date());

        ApFollowBehavior saved = followRepository.saveAndFlush(follow);

        assertTrue(saved.getId() > 0);
        assertEquals(7, saved.getFollowId());
    }
}
