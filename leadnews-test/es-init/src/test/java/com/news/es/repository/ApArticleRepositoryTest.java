package com.news.es.repository;

import com.news.es.pojo.SearchArticleVo;
import com.news.model.article.pojos.ApArticle;
import com.news.model.article.pojos.ApArticleConfig;
import com.news.model.article.pojos.ApArticleContent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ApArticleRepositoryTest {
    @Autowired private TestEntityManager entityManager;
    @Autowired private ApArticleRepository repository;

    @Test
    void loadsOnlyVisibleArticlesWithContent() {
        ApArticle visible = article("visible", (short) 1);
        entityManager.persistAndFlush(visible);
        entityManager.persist(config(visible.getId(), false, false));
        entityManager.persist(content(visible.getId(), "visible content"));

        ApArticle down = article("down", (short) 0);
        entityManager.persistAndFlush(down);
        entityManager.persist(config(down.getId(), false, true));
        entityManager.persist(content(down.getId(), "down content"));
        entityManager.flush();

        List<SearchArticleVo> results = repository.loadArticleList();

        assertEquals(1, results.size());
        SearchArticleVo result = results.get(0);
        assertEquals(visible.getId(), result.getId());
        assertEquals("visible", result.getTitle());
        assertEquals(1, result.getLayout());
        assertEquals("visible content", result.getContent());
    }

    private static ApArticle article(String title, short layout) {
        ApArticle article = new ApArticle();
        article.setTitle(title);
        article.setLayout(layout);
        article.setPublishTime(new Date());
        article.setAuthorId(10L);
        article.setAuthorName("author");
        article.setImages("image");
        article.setStaticUrl("static");
        return article;
    }

    private static ApArticleConfig config(Long articleId, boolean deleted, boolean down) {
        ApArticleConfig config = new ApArticleConfig(articleId);
        config.setIsDelete(deleted);
        config.setIsDown(down);
        return config;
    }

    private static ApArticleContent content(Long articleId, String value) {
        ApArticleContent content = new ApArticleContent();
        content.setArticleId(articleId);
        content.setContent(value);
        return content;
    }
}
