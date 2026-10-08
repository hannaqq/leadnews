package com.news.article.repository;

import com.news.article.service.transaction.ArticleTransactionService;
import com.news.model.article.dtos.ArticleDto;
import com.news.model.article.pojos.ApArticle;
import com.news.model.article.pojos.ApArticleConfig;
import com.news.model.article.pojos.ApArticleContent;
import com.news.model.article.pojos.ApCollection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(ArticleTransactionService.class)
class ArticleRepositoryTest {

    @Autowired private ApArticleRepository articleRepository;
    @Autowired private ApArticleConfigRepository configRepository;
    @Autowired private ApArticleContentRepository contentRepository;
    @Autowired private ApCollectionRepository collectionRepository;
    @Autowired private ArticleTransactionService transactionService;

    @Test
    void savesArticleConfigAndContentWithDatabaseGeneratedIds() {
        ArticleDto dto = article("new", 1, new Date(2_000), "content");

        ApArticle saved = transactionService.save(dto);

        assertNotNull(saved.getId());
        ApArticleConfig config = configRepository.findByArticleId(saved.getId()).orElseThrow();
        assertNotNull(config.getId());
        assertEquals(saved.getId(), config.getArticleId());
        assertFalse(config.getIsDelete());
        ApArticleContent content = contentRepository.findByArticleId(saved.getId()).orElseThrow();
        assertNotNull(content.getId());
        assertEquals(saved.getId(), content.getArticleId());
        assertEquals("content", content.getContent());
    }

    @Test
    void savesCollectionWithDatabaseGeneratedId() {
        ApCollection collection = new ApCollection();
        collection.setEntryId(10L);
        collection.setArticleId(20L);
        collection.setType((short) 0);
        collection.setPublishedTime(new Date());
        collection.setCollectionTime(new Date());

        ApCollection saved = collectionRepository.saveAndFlush(collection);

        assertNotNull(saved.getId());
    }

    @Test
    void updatesOnlyProvidedArticleFieldsAndContent() {
        ApArticle saved = transactionService.save(article("old", 7, new Date(2_000), "old content"));
        ArticleDto update = new ArticleDto();
        update.setId(saved.getId());
        update.setTitle("updated");
        update.setContent("updated content");

        transactionService.save(update);

        ApArticle reloaded = articleRepository.findById(saved.getId()).orElseThrow();
        assertEquals(saved.getId(), reloaded.getId());
        assertEquals("updated", reloaded.getTitle());
        assertEquals(7, reloaded.getChannelId());
        assertEquals("updated content",
                contentRepository.findByArticleId(saved.getId()).orElseThrow().getContent());
    }

    @Test
    void leavesContentUnchangedWhenUpdateOmitsIt() {
        ApArticle saved = transactionService.save(article("old", 7, new Date(2_000), "old content"));
        ArticleDto update = new ArticleDto();
        update.setId(saved.getId());
        update.setTitle("updated");

        transactionService.save(update);

        assertEquals("old content",
                contentRepository.findByArticleId(saved.getId()).orElseThrow().getContent());
    }

    @Test
    void reusesArticleForTheSameWemediaNews() {
        ArticleDto first = article("first", 1, new Date(2_000), "first content");
        first.setSourceNewsId(10);
        ApArticle saved = transactionService.save(first);

        ArticleDto retry = article("updated", 1, new Date(2_000), "updated content");
        retry.setSourceNewsId(10);
        ApArticle retried = transactionService.save(retry);

        assertEquals(saved.getId(), retried.getId());
        assertEquals(1, articleRepository.count());
        assertEquals("updated content",
                contentRepository.findByArticleId(saved.getId()).orElseThrow().getContent());
    }

    @Test
    void appliesFeedBoundariesChannelVisibilityAndLimit() {
        ApArticle older = transactionService.save(article("older", 1, new Date(1_000), "a"));
        transactionService.save(article("newer", 1, new Date(3_000), "b"));
        transactionService.save(article("other channel", 2, new Date(2_000), "c"));
        ApArticle hidden = transactionService.save(article("hidden", 1, new Date(1_500), "d"));
        ApArticleConfig hiddenConfig = configRepository.findByArticleId(hidden.getId()).orElseThrow();
        hiddenConfig.setIsDown(true);
        configRepository.save(hiddenConfig);

        var results = articleRepository.findFeed(
                new Date(2_500), null, 1, PageRequest.of(0, 1));

        assertEquals(1, results.size());
        assertEquals(older.getId(), results.get(0).getId());
    }

    @Test
    void deletesArticleAndContentAndMarksConfigDeleted() {
        ApArticle saved = transactionService.save(article("delete", 1, new Date(), "content"));

        transactionService.delete(saved.getId());

        assertTrue(articleRepository.findById(saved.getId()).isEmpty());
        assertTrue(contentRepository.findByArticleId(saved.getId()).isEmpty());
        assertTrue(configRepository.findByArticleId(saved.getId()).orElseThrow().getIsDelete());
    }

    private static ArticleDto article(String title, int channelId, Date publishTime, String content) {
        ArticleDto dto = new ArticleDto();
        dto.setTitle(title);
        dto.setChannelId(channelId);
        dto.setPublishTime(publishTime);
        dto.setContent(content);
        return dto;
    }
}
