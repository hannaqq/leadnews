package com.news.behavior.controller;

import com.news.behavior.service.ApLikesBehaviorService;
import com.news.behavior.service.ApUnlikesBehaviorService;
import com.news.behavior.service.ApUserFollowService;
import com.news.behavior.service.ArticleReactionService;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BehaviorController.class)
class BehaviorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApLikesBehaviorService likesBehaviorService;

    @MockitoBean
    private ApUnlikesBehaviorService unlikesBehaviorService;

    @MockitoBean
    private ArticleReactionService reactionService;

    @MockitoBean
    private ApUserFollowService followService;

    @Test
    void routesAuthenticatedLikeToReactionService() throws Exception {
        when(reactionService.setReaction(
                100L, com.news.model.behavior.enums.ReactionType.LIKE))
                .thenReturn(ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode()));

        mockMvc.perform(put("/api/v1/articles/100/like")
                        .header("userId", "10"))
                .andExpect(status().isOk());

        verify(reactionService).setReaction(
                100L, com.news.model.behavior.enums.ReactionType.LIKE);
    }

    @Test
    void supportsLegacyFollowPayload() throws Exception {
        when(followService.follow(20))
                .thenReturn(ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode()));

        mockMvc.perform(post("/api/v1/user/user_follow")
                        .header("userId", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authorId\":20,\"operation\":0}"))
                .andExpect(status().isOk());

        verify(followService).follow(20);
    }

    @Test
    void rejectsMalformedUserHeader() throws Exception {
        mockMvc.perform(put("/api/v1/articles/100/like")
                        .header("userId", "not-a-number"))
                .andExpect(status().isUnauthorized());
    }
}
