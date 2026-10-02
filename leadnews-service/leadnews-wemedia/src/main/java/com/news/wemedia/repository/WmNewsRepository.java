package com.news.wemedia.repository;

import com.news.model.wemedia.pojos.WmNews;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Date;
import java.util.Optional;

public interface WmNewsRepository extends JpaRepository<WmNews, Integer> {
    boolean existsByChannelId(Integer channelId);
    Optional<WmNews> findByIdAndUserId(Integer id, Integer userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from WmNews n where n.id = :id and n.userId = :userId")
    Optional<WmNews> findByIdAndUserIdForUpdate(@Param("id") Integer id, @Param("userId") Integer userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update WmNews n set n.status = :nextStatus, n.reason = :reason " +
            "where n.id = :id and n.status = :currentStatus")
    int transitionStatus(@Param("id") Integer id, @Param("currentStatus") Short currentStatus,
                         @Param("nextStatus") Short nextStatus, @Param("reason") String reason);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update WmNews n set n.status = :nextStatus, n.reason = :reason, n.articleId = :articleId " +
            "where n.id = :id and n.status = :currentStatus")
    int completePublishing(@Param("id") Integer id, @Param("currentStatus") Short currentStatus,
                           @Param("nextStatus") Short nextStatus, @Param("reason") String reason,
                           @Param("articleId") Long articleId);

    @Query("""
            select n from WmNews n
            where n.userId = :userId
              and (:status is null or n.status = :status)
              and (:channelId is null or n.channelId = :channelId)
              and (:keyword is null or lower(n.title) like lower(concat('%', :keyword, '%')))
              and (:beginDate is null or :endDate is null or n.publishTime between :beginDate and :endDate)
            """)
    Page<WmNews> findForUser(Integer userId, Short status, Integer channelId,
                             String keyword, Date beginDate, Date endDate, Pageable pageable);

    @Query("""
            select n from WmNews n
            where (:title is null or n.title = :title)
              and (:status is null or n.status = :status)
            """)
    Page<WmNews> findForReview(String title, Short status, Pageable pageable);
}
