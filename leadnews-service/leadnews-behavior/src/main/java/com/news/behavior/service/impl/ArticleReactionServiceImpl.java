package com.news.behavior.service.impl;

import com.news.behavior.repository.ApArticleReactionRepository;
import com.news.behavior.service.ArticleReactionService;
import com.news.model.behavior.enums.ReactionType;
import com.news.model.behavior.pojos.ApArticleReaction;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.utils.thread.AppThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArticleReactionServiceImpl implements ArticleReactionService {

    private final ApArticleReactionRepository reactionRepository;

    @Override
    public ResponseResult setReaction(Long articleId, ReactionType reactionType) {
        Integer userId = AppThreadLocalUtil.getUserId();
        ResponseResult validation = validate(userId, articleId, reactionType);
        if (validation != null) {
            return validation;
        }

        Date now = new Date();
        ApArticleReaction reaction = reactionRepository
                .findByUserIdAndArticleId(userId, articleId)
                .orElseGet(() -> {
                    ApArticleReaction created = new ApArticleReaction();
                    created.setUserId(userId);
                    created.setArticleId(articleId);
                    created.setCreatedTime(now);
                    return created;
                });
        reaction.setReactionType(reactionType);
        reaction.setUpdatedTime(now);
        try {
            reactionRepository.saveAndFlush(reaction);
        } catch (DataIntegrityViolationException exception) {
            // Another request inserted the same user/article row concurrently.
            ApArticleReaction concurrent = reactionRepository
                    .findByUserIdAndArticleId(userId, articleId)
                    .orElseThrow(() -> exception);
            concurrent.setReactionType(reactionType);
            concurrent.setUpdatedTime(now);
            reactionRepository.saveAndFlush(concurrent);
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode());
    }

    @Override
    @Transactional
    public ResponseResult removeReaction(Long articleId, ReactionType reactionType) {
        Integer userId = AppThreadLocalUtil.getUserId();
        ResponseResult validation = validate(userId, articleId, reactionType);
        if (validation != null) {
            return validation;
        }

        Optional<ApArticleReaction> current =
                reactionRepository.findByUserIdAndArticleId(userId, articleId);
        if (current.isPresent() && current.get().getReactionType() == reactionType) {
            reactionRepository.delete(current.get());
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode());
    }

    private ResponseResult validate(Integer userId, Long articleId, ReactionType reactionType) {
        if (userId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        if (articleId == null || articleId <= 0 || reactionType == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        return null;
    }
}
