package com.news.behavior.repository;

import com.news.model.behavior.enums.ReactionType;
import com.news.model.behavior.pojos.ApArticleReaction;
import com.news.model.behavior.pojos.ApUserFollow;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class BehaviorRepositoryTest {

    @Autowired
    private ApArticleReactionRepository reactionRepository;

    @Autowired
    private ApUserFollowRepository followRepository;

    @Test
    void findsReactionByUserAndArticle() {
        Date now = new Date();
        ApArticleReaction reaction = new ApArticleReaction();
        reaction.setUserId(42);
        reaction.setArticleId(100L);
        reaction.setReactionType(ReactionType.LIKE);
        reaction.setCreatedTime(now);
        reaction.setUpdatedTime(now);
        reactionRepository.saveAndFlush(reaction);

        ApArticleReaction result = reactionRepository.findByUserIdAndArticleId(42, 100L)
                .orElseThrow();

        assertTrue(result.getId() > 0L);
        assertEquals(ReactionType.LIKE, result.getReactionType());
    }

    @Test
    void enforcesUniqueFollowRelation() {
        ApUserFollow follow = new ApUserFollow();
        follow.setUserId(42);
        follow.setCreatorId(7);
        follow.setCreatedTime(new Date());
        followRepository.saveAndFlush(follow);

        ApUserFollow duplicate = new ApUserFollow();
        duplicate.setUserId(42);
        duplicate.setCreatorId(7);
        duplicate.setCreatedTime(new Date());

        assertThrows(RuntimeException.class, () -> followRepository.saveAndFlush(duplicate));
    }
}
