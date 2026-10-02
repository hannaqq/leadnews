package com.news.article.repository;

import com.news.model.article.pojos.ApArticleContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApArticleContentRepository extends JpaRepository<ApArticleContent, Long> {
    Optional<ApArticleContent> findByArticleId(Long articleId);
    long deleteByArticleId(Long articleId);
}
