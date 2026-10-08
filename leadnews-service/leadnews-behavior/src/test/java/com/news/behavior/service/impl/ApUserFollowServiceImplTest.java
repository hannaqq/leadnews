package com.news.behavior.service.impl;

import com.news.apis.wemedia.IWemediaClient;
import com.news.behavior.repository.ApUserFollowRepository;
import com.news.model.behavior.pojos.ApUserFollow;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.wemedia.vos.CreatorAccountVo;
import com.news.utils.thread.AppThreadLocalUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApUserFollowServiceImplTest {

    private final ApUserFollowRepository repository = mock(ApUserFollowRepository.class);
    private final IWemediaClient wemediaClient = mock(IWemediaClient.class);
    private final ApUserFollowServiceImpl service =
            new ApUserFollowServiceImpl(repository, wemediaClient);

    @AfterEach
    void clearThreadLocal() {
        AppThreadLocalUtil.clear();
    }

    @Test
    void requiresLogin() {
        ResponseResult result = service.follow(20);
        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
    }

    @Test
    void rejectsInvalidCreator() {
        AppThreadLocalUtil.setUserId(10);
        ResponseResult result = service.follow(0);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void savesCreatorRelation() {
        AppThreadLocalUtil.setUserId(10);
        when(wemediaClient.getById(20)).thenReturn(new CreatorAccountVo(20, 30));

        ResponseResult result = service.follow(20);

        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        ArgumentCaptor<ApUserFollow> relation = ArgumentCaptor.forClass(ApUserFollow.class);
        verify(repository).saveAndFlush(relation.capture());
        assertEquals(10, relation.getValue().getUserId());
        assertEquals(20, relation.getValue().getCreatorId());
    }

    @Test
    void rejectsFollowingOwnCreatorAccount() {
        AppThreadLocalUtil.setUserId(10);
        when(wemediaClient.getById(20)).thenReturn(new CreatorAccountVo(20, 10));

        ResponseResult result = service.follow(20);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void rejectsMissingOrUnavailableCreator() {
        AppThreadLocalUtil.setUserId(10);
        when(wemediaClient.getById(20)).thenReturn(null);

        ResponseResult result = service.follow(20);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    @Test
    void rejectsExistingOrConcurrentDuplicate() {
        AppThreadLocalUtil.setUserId(10);
        when(wemediaClient.getById(20)).thenReturn(new CreatorAccountVo(20, 30));
        when(repository.saveAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        ResponseResult result = service.follow(20);

        assertEquals(AppHttpCodeEnum.HAVE_FOLLOWED.getCode(), result.getCode());
    }

    @Test
    void repeatedUnfollowIsSuccessful() {
        AppThreadLocalUtil.setUserId(10);
        when(repository.deleteByUserIdAndCreatorId(10, 20)).thenReturn(0);

        ResponseResult result = service.unfollow(20);

        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(repository).deleteByUserIdAndCreatorId(10, 20);
    }
}
