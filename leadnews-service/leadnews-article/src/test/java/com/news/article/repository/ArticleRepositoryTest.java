package com.news.article.repository;

import com.news.article.service.impl.ArticlePersistenceService;
import com.news.model.article.dtos.ArticleDto;
import com.news.model.article.pojos.ApArticle;
import com.news.model.article.pojos.ApArticleConfig;
import com.news.model.article.pojos.ApArticleContent;
import com.news.model.article.pojos.ApAuthor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(ArticlePersistenceService.class)
class ArticleRepositoryTest {

    @Autowired private ApArticleRepository articleRepository;
    @Autowired private ApArticleConfigRepository configRepository;
    @Autowired private ApArticleContentRepository contentRepository;
    @Autowired private ApAuthorRepository authorRepository;
    @Autowired private ArticlePersistenceService persistenceService;

    @Test
    void savesArticleConfigAndContentWithSnowflakeIds() {
        ArticleDto dto = article("new", 1, new Date(2_000), "content");

        ApArticle saved = persistenceService.save(dto);

        assertNotNull(saved.getId());
        ApArticleConfig config = configRepository.findByArticleId(saved.getId()).orElseThrow();
        assertNotNull(config.getId());
        assertFalse(config.getIsDelete());
        ApArticleContent content = contentRepository.findByArticleId(saved.getId()).orElseThrow();
        assertNotNull(content.getId());
        assertEquals("content", content.getContent());
    }

    @Test
    void updatesOnlyProvidedArticleFieldsAndContent() {
        ApArticle saved = persistenceService.save(article("old", 7, new Date(2_000), "old content"));
        ArticleDto update = new ArticleDto();
        update.setId(saved.getId());
        update.setTitle("updated");
        update.setContent("updated content");

        persistenceService.save(update);

        ApArticle reloaded = articleRepository.findById(saved.getId()).orElseThrow();
        assertEquals("updated", reloaded.getTitle());
        assertEquals(7, reloaded.getChannelId());
        assertEquals("updated content",
                contentRepository.findByArticleId(saved.getId()).orElseThrow().getContent());
    }

    @Test
    void leavesContentUnchangedWhenUpdateOmitsIt() {
        ApArticle saved = persistenceService.save(article("old", 7, new Date(2_000), "old content"));
        ArticleDto update = new ArticleDto();
        update.setId(saved.getId());
        update.setTitle("updated");

        persistenceService.save(update);

        assertEquals("old content",
                contentRepository.findByArticleId(saved.getId()).orElseThrow().getContent());
    }

    @Test
    void appliesFeedBoundariesChannelVisibilityAndLimit() {
        ApArticle older = persistenceService.save(article("older", 1, new Date(1_000), "a"));
        persistenceService.save(article("newer", 1, new Date(3_000), "b"));
        persistenceService.save(article("other channel", 2, new Date(2_000), "c"));
        ApArticle hidden = persistenceService.save(article("hidden", 1, new Date(1_500), "d"));
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
        ApArticle saved = persistenceService.save(article("delete", 1, new Date(), "content"));

        persistenceService.delete(saved.getId());

        assertTrue(articleRepository.findById(saved.getId()).isEmpty());
        assertTrue(contentRepository.findByArticleId(saved.getId()).isEmpty());
        assertTrue(configRepository.findByArticleId(saved.getId()).orElseThrow().getIsDelete());
    }

    @Test
    void enforcesOneAuthorPerUser() {
        authorRepository.saveAndFlush(author(10, 20));

        assertThrows(RuntimeException.class,
                () -> authorRepository.saveAndFlush(author(10, 21)));
    }

    private static ApAuthor author(int userId, int wmUserId) {
        ApAuthor author = new ApAuthor();
        author.setName("author");
        author.setType((short) 2);
        author.setUserId(userId);
        author.setWmUserId(wmUserId);
        author.setCreatedTime(new Date());
        return author;
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
