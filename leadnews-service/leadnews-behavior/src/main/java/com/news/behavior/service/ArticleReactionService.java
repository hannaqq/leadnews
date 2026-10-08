package com.news.behavior.service;

import com.news.model.behavior.enums.ReactionType;
import com.news.model.common.dtos.ResponseResult;

public interface ArticleReactionService {

    ResponseResult setReaction(Long articleId, ReactionType reactionType);

    ResponseResult removeReaction(Long articleId, ReactionType reactionType);
}
