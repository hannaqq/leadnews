package com.news.behavior.service.impl;

import com.news.behavior.repository.ApArticleReactionRepository;
import com.news.model.behavior.enums.ReactionType;
import com.news.model.behavior.pojos.ApArticleReaction;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.utils.thread.AppThreadLocalUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArticleReactionServiceImplTest {

    private final ApArticleReactionRepository repository = mock(ApArticleReactionRepository.class);
    private final ArticleReactionServiceImpl service = new ArticleReactionServiceImpl(repository);

    @AfterEach
    void clearThreadLocal() {
        AppThreadLocalUtil.clear();
    }

    @Test
    void requiresLogin() {
        ResponseResult result = service.setReaction(100L, ReactionType.LIKE);
        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
    }

    @Test
    void createsLike() {
        AppThreadLocalUtil.setUserId(10);
        when(repository.findByUserIdAndArticleId(10, 100L)).thenReturn(Optional.empty());

        ResponseResult result = service.setReaction(100L, ReactionType.LIKE);

        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        ArgumentCaptor<ApArticleReaction> reaction =
                ArgumentCaptor.forClass(ApArticleReaction.class);
        verify(repository).saveAndFlush(reaction.capture());
        assertEquals(ReactionType.LIKE, reaction.getValue().getReactionType());
    }

    @Test
    void switchesLikeToDislikeInSameRow() {
        AppThreadLocalUtil.setUserId(10);
        ApArticleReaction existing = new ApArticleReaction();
        existing.setReactionType(ReactionType.LIKE);
        when(repository.findByUserIdAndArticleId(10, 100L))
                .thenReturn(Optional.of(existing));

        service.setReaction(100L, ReactionType.DISLIKE);

        verify(repository).saveAndFlush(existing);
        assertEquals(ReactionType.DISLIKE, existing.getReactionType());
    }

    @Test
    void removingOppositeReactionIsIdempotent() {
        AppThreadLocalUtil.setUserId(10);
        ApArticleReaction existing = new ApArticleReaction();
        existing.setReactionType(ReactionType.DISLIKE);
        when(repository.findByUserIdAndArticleId(10, 100L))
                .thenReturn(Optional.of(existing));

        ResponseResult result = service.removeReaction(100L, ReactionType.LIKE);

        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(repository, never()).delete(any());
    }
}
