package com.news.article.service.impl;

import com.news.article.service.transaction.ArticleTransactionService;
import com.news.article.repository.ApArticleRepository;
import com.news.article.service.ArticleFreemarkerService;
import com.news.common.constants.ArticleConstants;
import com.news.model.article.dtos.ArticleDto;
import com.news.model.article.dtos.ArticleHomeDto;
import com.news.model.article.pojos.ApArticle;
import com.news.model.article.vos.ArticleDetailVo;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApArticleServiceImplTest {

    private final ApArticleRepository repository = mock(ApArticleRepository.class);
    private final ArticleTransactionService transactionService = mock(ArticleTransactionService.class);
    private final ArticleFreemarkerService freemarkerService = mock(ArticleFreemarkerService.class);
    private final ApArticleServiceImpl service = new ApArticleServiceImpl(
            repository, transactionService, freemarkerService);

    @Test
    void mapsLoadMoreToBeforeBoundaryAndCapsPageSize() {
        ArticleHomeDto dto = new ArticleHomeDto();
        Date minimum = new Date(1_000);
        dto.setMinBehotTime(minimum);
        dto.setMaxBehotTime(new Date(2_000));
        dto.setTag("3");
        dto.setSize(100);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        when(repository.findFeed(eq(minimum), eq(null), eq(3), pageable.capture()))
                .thenReturn(List.of());

        service.load(dto, ArticleConstants.LOADTYPE_LOAD_MORE);

        assertEquals(50, pageable.getValue().getPageSize());
    }

    @Test
    void mapsLoadNewToAfterBoundaryWithoutChannelFilter() {
        ArticleHomeDto dto = new ArticleHomeDto();
        Date maximum = new Date(2_000);
        dto.setMaxBehotTime(maximum);
        dto.setMinBehotTime(new Date(1_000));
        dto.setTag(ArticleConstants.DEFAULT_TAG);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        when(repository.findFeed(eq(null), eq(maximum), eq(null), pageable.capture()))
                .thenReturn(List.of());

        service.load(dto, ArticleConstants.LOADTYPE_LOAD_NEW);

        assertEquals(7, pageable.getValue().getPageSize());
    }

    @Test
    void startsStaticPageWorkOnlyAfterPersistenceReturns() {
        ArticleDto dto = new ArticleDto();
        dto.setContent("[]");
        ApArticle saved = new ApArticle();
        saved.setId(99L);
        when(transactionService.save(dto)).thenReturn(saved);

        service.saveArticle(dto);

        InOrder order = inOrder(transactionService, freemarkerService);
        order.verify(transactionService).save(dto);
        order.verify(freemarkerService).buildArticleToMinIO(saved, "[]");
        verify(repository, org.mockito.Mockito.never())
                .findFeed(any(), any(), any(), any());
        assertNull(dto.getId());
    }

    @Test
    void loadsArticleDetailWithCreatorId() {
        ApArticle article = new ApArticle();
        article.setId(100L);
        article.setAuthorId(20L);
        article.setAuthorName("creator");
        article.setTitle("title");
        when(repository.findById(100L)).thenReturn(java.util.Optional.of(article));

        ResponseResult result = service.loadArticleInfo(100L);

        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        ArticleDetailVo detail = (ArticleDetailVo) result.getData();
        assertEquals(100L, detail.getArticleId());
        assertEquals(20, detail.getCreatorId());
        assertEquals("creator", detail.getAuthorName());
    }

    @Test
    void returnsNotFoundForMissingArticleDetail() {
        when(repository.findById(100L)).thenReturn(java.util.Optional.empty());

        ResponseResult result = service.loadArticleInfo(100L);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }
}
