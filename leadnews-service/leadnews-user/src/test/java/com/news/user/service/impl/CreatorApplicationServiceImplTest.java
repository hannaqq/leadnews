package com.news.user.service.impl;

import com.news.apis.wemedia.IWemediaClient;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.user.dtos.CreatorApplicationRejectDto;
import com.news.model.user.dtos.CreatorApplicationSubmitDto;
import com.news.model.user.enums.CreatorApplicationStatus;
import com.news.model.user.pojos.ApUser;
import com.news.model.user.pojos.CreatorApplication;
import com.news.model.user.vos.CreatorApplicationVo;
import com.news.model.wemedia.vos.CreatorAccountVo;
import com.news.user.repository.ApUserRepository;
import com.news.user.repository.CreatorApplicationRepository;
import com.news.utils.thread.AppThreadLocalUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreatorApplicationServiceImplTest {

    private final CreatorApplicationRepository applicationRepository =
            mock(CreatorApplicationRepository.class);
    private final ApUserRepository userRepository = mock(ApUserRepository.class);
    private final IWemediaClient wemediaClient = mock(IWemediaClient.class);
    private final CreatorApplicationServiceImpl service = new CreatorApplicationServiceImpl(
            applicationRepository,
            userRepository,
            wemediaClient);

    @AfterEach
    void clearUserContext() {
        AppThreadLocalUtil.clear();
    }

    @Test
    void submitsPendingApplicationForAuthenticatedUser() {
        AppThreadLocalUtil.setUserId(7);
        when(applicationRepository.findByUserId(7)).thenReturn(Optional.empty());
        when(applicationRepository.saveAndFlush(any(CreatorApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseResult result = service.submit(submission());

        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        CreatorApplicationVo saved = (CreatorApplicationVo) result.getData();
        assertEquals(7, saved.getUserId());
        assertEquals(CreatorApplicationStatus.PENDING, saved.getStatus());
        assertNotNull(saved.getSubmittedTime());
    }

    @Test
    void approvesPendingApplicationWhenWemediaAccountAlreadyExists() {
        CreatorApplication application = application(10, CreatorApplicationStatus.PENDING);
        when(applicationRepository.findById(10)).thenReturn(Optional.of(application));
        when(userRepository.findById(7)).thenReturn(Optional.of(user(7)));
        CreatorAccountVo creatorAccount = new CreatorAccountVo(20, 7);
        when(wemediaClient.getByUserId(7)).thenReturn(creatorAccount);
        when(applicationRepository.completeReview(
                any(), any(), any(), any())).thenReturn(1);

        ResponseResult result = service.approve(10);

        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(wemediaClient, never()).provisionCreatorAccount(any());
        verify(applicationRepository).completeReview(
                any(), any(), any(), any());
    }

    @Test
    void returnsClaimedApplicationToPendingWhenProvisioningResultIsUnknown() {
        CreatorApplication application = application(10, CreatorApplicationStatus.PENDING);
        when(applicationRepository.findById(10)).thenReturn(Optional.of(application));
        when(userRepository.findById(7)).thenReturn(Optional.of(user(7)));
        when(wemediaClient.getByUserId(7)).thenThrow(new IllegalStateException("timeout"));

        ResponseResult result = service.approve(10);

        assertEquals(AppHttpCodeEnum.SERVER_ERROR.getCode(), result.getCode());
        verify(applicationRepository, never()).completeReview(
                any(), any(), any(), any());
    }

    @Test
    void rejectsOnlyThroughConditionalPersistenceOperation() {
        CreatorApplicationRejectDto dto = new CreatorApplicationRejectDto();
        dto.setReviewNote("Portfolio does not demonstrate relevant experience");
        when(applicationRepository.reject(
                any(), any(), any(), any(), any())).thenReturn(1);

        ResponseResult result = service.reject(10, dto);

        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(applicationRepository).reject(
                any(), any(), any(), any(), any());
    }

    private static CreatorApplication application(
            int id,
            CreatorApplicationStatus status) {
        CreatorApplication application = new CreatorApplication();
        application.setId(id);
        application.setUserId(7);
        application.setDisplayName("Reporter");
        application.setStatus(status);
        return application;
    }

    private static ApUser user(int id) {
        ApUser user = new ApUser();
        user.setId(id);
        user.setName("reporter");
        return user;
    }

    private static CreatorApplicationSubmitDto submission() {
        CreatorApplicationSubmitDto dto = new CreatorApplicationSubmitDto();
        dto.setDisplayName("Reporter");
        dto.setBio("An experienced local news reporter.");
        dto.setCategory("Local News");
        return dto;
    }
}
