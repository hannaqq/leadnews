package com.news.article.repository;

import com.news.model.article.pojos.ApArticleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ApArticleConfigRepository extends JpaRepository<ApArticleConfig, Long> {
    Optional<ApArticleConfig> findByArticleId(Long articleId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update ApArticleConfig config set config.isDown = :isDown where config.articleId = :articleId")
    int updateDownByArticleId(@Param("articleId") Long articleId, @Param("isDown") boolean isDown);
}
