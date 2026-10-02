package com.news.es.repository;

import com.news.es.pojo.SearchArticleVo;
import com.news.model.article.pojos.ApArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ApArticleRepository extends JpaRepository<ApArticle, Long> {

    @Query("""
            select new com.news.es.pojo.SearchArticleVo(
                article.id,
                article.title,
                article.publishTime,
                article.layout,
                article.images,
                article.authorId,
                article.authorName,
                article.staticUrl,
                content.content)
            from ApArticle article, ApArticleConfig config, ApArticleContent content
            where article.id = config.articleId
              and article.id = content.articleId
              and config.isDelete <> true
              and config.isDown <> true
            """)
    List<SearchArticleVo> loadArticleList();
}
