package com.news.user.service.impl;

import com.news.apis.article.IArticleClient;
import com.news.apis.wemedia.IWemediaClient;
import com.news.model.article.pojos.ApAuthor;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.dtos.PageResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.user.dtos.AuthDto;
import com.news.model.user.pojos.ApUser;
import com.news.model.user.pojos.ApUserRealname;
import com.news.model.wemedia.pojos.WmUser;
import com.news.user.repository.ApUserRealnameRepository;
import com.news.user.repository.ApUserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApUserRealnameServiceImplTest {

    private final ApUserRealnameRepository realnameRepository =
            mock(ApUserRealnameRepository.class);
    private final ApUserRepository userRepository = mock(ApUserRepository.class);
    private final IWemediaClient wemediaClient = mock(IWemediaClient.class);
    private final IArticleClient articleClient = mock(IArticleClient.class);
    private final ApApUserRealnameServiceImpl service = new ApApUserRealnameServiceImpl(
            realnameRepository,
            userRepository,
            wemediaClient,
            articleClient);

    @Test
    void convertsFirstApiPageToZeroBasedPageableAndPreservesResponsePage() {
        AuthDto dto = new AuthDto();
        dto.setPage(1);
        dto.setSize(20);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        when(realnameRepository.findForReview(isNull(), isNull(), pageable.capture()))
                .thenReturn(new PageImpl<>(List.of(new ApUserRealname())));

        PageResponseResult response = (PageResponseResult) service.getList(dto);

        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(20, pageable.getValue().getPageSize());
        assertEquals(1, response.getCurrentPage());
        assertEquals(20, response.getSize());
        assertEquals(1, response.getTotal());
    }

    @Test
    void normalizesZeroPageBeforeCreatingPageable() {
        AuthDto dto = new AuthDto();
        dto.setPage(0);
        dto.setSize(10);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        when(realnameRepository.findForReview(isNull(), isNull(), pageable.capture()))
                .thenReturn(new PageImpl<>(List.of()));

        PageResponseResult response = (PageResponseResult) service.getList(dto);

        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(1, response.getCurrentPage());
    }

    @Test
    void reloadsCreatedWemediaUserBeforeCreatingAuthor() {
        AuthDto dto = new AuthDto();
        dto.setId(7);
        ApUserRealname realname = new ApUserRealname();
        realname.setUserId(9);
        ApUser user = new ApUser();
        user.setId(9);
        user.setName("author");
        WmUser createdWmUser = new WmUser();
        createdWmUser.setId(42);

        when(realnameRepository.findById(7)).thenReturn(java.util.Optional.of(realname));
        when(userRepository.findById(9)).thenReturn(java.util.Optional.of(user));
        when(wemediaClient.getByUserId(9)).thenReturn(null, createdWmUser);
        when(wemediaClient.saveWmUser(any(WmUser.class)))
                .thenReturn(ResponseResult.okResult(AppHttpCodeEnum.SUCCESS));
        when(articleClient.getByUserId(9)).thenReturn(null);
        when(articleClient.saveApAuthor(any(ApAuthor.class)))
                .thenReturn(ResponseResult.okResult(AppHttpCodeEnum.SUCCESS));
        ArgumentCaptor<ApAuthor> author = ArgumentCaptor.forClass(ApAuthor.class);

        ResponseResult response = service.pass(dto);

        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), response.getCode());
        verify(articleClient).saveApAuthor(author.capture());
        assertEquals(42, author.getValue().getWmUserId());
        assertEquals(com.news.common.constants.ApUserConstants.AUTHORIZATION_PASS,
                realname.getStatus());
        verify(realnameRepository).save(realname);
    }

    @Test
    void stopsWhenWemediaAccountCreationFails() {
        AuthDto dto = new AuthDto();
        dto.setId(7);
        ApUserRealname realname = new ApUserRealname();
        realname.setUserId(9);
        ApUser user = new ApUser();
        user.setId(9);

        when(realnameRepository.findById(7)).thenReturn(java.util.Optional.of(realname));
        when(userRepository.findById(9)).thenReturn(java.util.Optional.of(user));
        when(wemediaClient.getByUserId(9)).thenReturn(null);
        when(wemediaClient.saveWmUser(any(WmUser.class)))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR));

        ResponseResult response = service.pass(dto);

        assertEquals(AppHttpCodeEnum.SERVER_ERROR.getCode(), response.getCode());
        verify(articleClient, never()).getByUserId(any());
        verify(articleClient, never()).saveApAuthor(any());
        verify(realnameRepository, never()).save(realname);
    }

    @Test
    void keepsReviewPendingWhenAuthorCreationFails() {
        AuthDto dto = new AuthDto();
        dto.setId(7);
        ApUserRealname realname = new ApUserRealname();
        realname.setUserId(9);
        ApUser user = new ApUser();
        user.setId(9);
        WmUser wmUser = new WmUser();
        wmUser.setId(42);

        when(realnameRepository.findById(7)).thenReturn(java.util.Optional.of(realname));
        when(userRepository.findById(9)).thenReturn(java.util.Optional.of(user));
        when(wemediaClient.getByUserId(9)).thenReturn(wmUser);
        when(articleClient.getByUserId(9)).thenReturn(null);
        when(articleClient.saveApAuthor(any(ApAuthor.class)))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR));

        ResponseResult response = service.pass(dto);

        assertEquals(AppHttpCodeEnum.SERVER_ERROR.getCode(), response.getCode());
        assertNull(realname.getStatus());
        verify(realnameRepository, never()).save(realname);
    }
}
