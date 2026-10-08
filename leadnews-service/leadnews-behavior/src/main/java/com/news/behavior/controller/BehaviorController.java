package com.news.behavior.controller;

import com.news.behavior.service.ApLikesBehaviorService;
import com.news.behavior.service.ApUnlikesBehaviorService;
import com.news.behavior.service.ApUserFollowService;
import com.news.behavior.service.ArticleReactionService;
import com.news.model.behavior.dtos.FollowRelationDto;
import com.news.model.behavior.dtos.LikesBehaviorDto;
import com.news.model.behavior.dtos.UnLikesBehaviorDto;
import com.news.model.behavior.enums.ReactionType;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BehaviorController {

    private final ApLikesBehaviorService apLikesBehaviorService;
    private final ApUnlikesBehaviorService apUnlikesBehaviorService;
    private final ArticleReactionService articleReactionService;
    private final ApUserFollowService userFollowService;

    @PostMapping("/likes_behavior")
    public ResponseResult like(@RequestBody LikesBehaviorDto dto) {
        return apLikesBehaviorService.like(dto);
    }

    @PostMapping("/un_likes_behavior")
    public ResponseResult unlike(@RequestBody UnLikesBehaviorDto dto) {
        return apUnlikesBehaviorService.unlike(dto);
    }

    @PutMapping("/articles/{articleId}/like")
    public ResponseResult likeArticle(@PathVariable Long articleId) {
        return articleReactionService.setReaction(articleId, ReactionType.LIKE);
    }

    @DeleteMapping("/articles/{articleId}/like")
    public ResponseResult cancelLike(@PathVariable Long articleId) {
        return articleReactionService.removeReaction(articleId, ReactionType.LIKE);
    }

    @PutMapping("/articles/{articleId}/dislike")
    public ResponseResult dislikeArticle(@PathVariable Long articleId) {
        return articleReactionService.setReaction(articleId, ReactionType.DISLIKE);
    }

    @DeleteMapping("/articles/{articleId}/dislike")
    public ResponseResult cancelDislike(@PathVariable Long articleId) {
        return articleReactionService.removeReaction(articleId, ReactionType.DISLIKE);
    }

    @PostMapping("/creators/{creatorId}/follow")
    public ResponseResult follow(@PathVariable Integer creatorId) {
        return userFollowService.follow(creatorId);
    }

    @DeleteMapping("/creators/{creatorId}/follow")
    public ResponseResult unfollow(@PathVariable Integer creatorId) {
        return userFollowService.unfollow(creatorId);
    }

    @PostMapping("/user/user_follow")
    public ResponseResult legacyFollow(@RequestBody FollowRelationDto dto) {
        if (dto == null || dto.getOperation() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        if (dto.getOperation() == 0) {
            return userFollowService.follow(dto.getAuthorId());
        }
        if (dto.getOperation() == 1) {
            return userFollowService.unfollow(dto.getAuthorId());
        }
        return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
    }
}
