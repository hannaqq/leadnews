package com.news.behavior.service.impl;


import com.news.behavior.service.ArticleReactionService;
import com.news.behavior.service.ApUnlikesBehaviorService;
import com.news.model.behavior.dtos.UnLikesBehaviorDto;
import com.news.model.behavior.enums.ReactionType;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApUnLikesBehaviorServiceImpl implements ApUnlikesBehaviorService {

    private final ArticleReactionService reactionService;

    @Override
    public ResponseResult unlike(UnLikesBehaviorDto dto) {
        if (dto == null || dto.getType() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        if (dto.getType() == 0) {
            return reactionService.setReaction(dto.getArticleId(), ReactionType.DISLIKE);
        }
        if (dto.getType() == 1) {
            return reactionService.removeReaction(dto.getArticleId(), ReactionType.DISLIKE);
        }
        return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
    }
}
