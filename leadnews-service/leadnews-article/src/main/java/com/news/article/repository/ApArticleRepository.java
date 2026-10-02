package com.news.article.repository;

import com.news.model.article.pojos.ApArticle;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface ApArticleRepository extends JpaRepository<ApArticle, Long> {

    @Query("""
            select article from ApArticle article
            join ApArticleConfig config on config.articleId = article.id
            where config.isDelete = false
              and config.isDown = false
              and (:beforeTime is null or article.publishTime < :beforeTime)
              and (:afterTime is null or article.publishTime > :afterTime)
              and (:channelId is null or article.channelId = :channelId)
            order by article.publishTime desc
            """)
    List<ApArticle> findFeed(
            @Param("beforeTime") Date beforeTime,
            @Param("afterTime") Date afterTime,
            @Param("channelId") Integer channelId,
            Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update ApArticle article set article.staticUrl = :staticUrl where article.id = :articleId")
    int updateStaticUrl(@Param("articleId") Long articleId, @Param("staticUrl") String staticUrl);
}
