package com.news.behavior.service.impl;

import com.news.behavior.service.ApLikesBehaviorService;
import com.news.behavior.service.ArticleReactionService;
import com.news.model.behavior.dtos.LikesBehaviorDto;
import com.news.model.behavior.enums.ReactionType;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApLikesBehaviorServiceImpl implements ApLikesBehaviorService {

    private final ArticleReactionService reactionService;

    @Override
    public ResponseResult like(LikesBehaviorDto dto) {
        if (dto == null || dto.getOperation() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        if (dto.getOperation() == 0) {
            return reactionService.setReaction(dto.getArticleId(), ReactionType.LIKE);
        }
        if (dto.getOperation() == 1) {
            return reactionService.removeReaction(dto.getArticleId(), ReactionType.LIKE);
        }
        return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
    }
}
