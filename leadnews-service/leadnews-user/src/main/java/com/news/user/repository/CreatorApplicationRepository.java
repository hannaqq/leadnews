package com.news.user.repository;

import com.news.model.user.enums.CreatorApplicationStatus;
import com.news.model.user.pojos.CreatorApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

public interface CreatorApplicationRepository extends JpaRepository<CreatorApplication, Integer> {

    Optional<CreatorApplication> findByUserId(Integer userId);

    @Query("""
            select application from CreatorApplication application
            where (:id is null or application.id = :id)
              and (:status is null or application.status = :status)
            """)
    Page<CreatorApplication> findForReview(
            @Param("id") Integer id,
            @Param("status") CreatorApplicationStatus status,
            Pageable pageable);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update CreatorApplication application
               set application.status = :nextStatus,
                   application.reviewedTime = :reviewedTime,
                   application.updatedTime = :reviewedTime
             where application.id = :id
               and application.status = :currentStatus
            """)
    int completeReview(
            @Param("id") Integer id,
            @Param("currentStatus") CreatorApplicationStatus currentStatus,
            @Param("nextStatus") CreatorApplicationStatus nextStatus,
            @Param("reviewedTime") Date reviewedTime);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update CreatorApplication application
               set application.status = :nextStatus,
                   application.reviewNote = :reviewNote,
                   application.reviewedTime = :reviewedTime,
                   application.updatedTime = :reviewedTime
             where application.id = :id
               and application.status = :currentStatus
            """)
    int reject(
            @Param("id") Integer id,
            @Param("currentStatus") CreatorApplicationStatus currentStatus,
            @Param("nextStatus") CreatorApplicationStatus nextStatus,
            @Param("reviewNote") String reviewNote,
            @Param("reviewedTime") Date reviewedTime);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update CreatorApplication application
               set application.displayName = :displayName,
                   application.bio = :bio,
                   application.category = :category,
                   application.portfolioUrl = :portfolioUrl,
                   application.status = :nextStatus,
                   application.reviewNote = null,
                   application.reviewedTime = null,
                   application.submittedTime = :submittedTime,
                   application.updatedTime = :submittedTime
             where application.id = :id
               and application.status = :currentStatus
            """)
    int resubmit(
            @Param("id") Integer id,
            @Param("currentStatus") CreatorApplicationStatus currentStatus,
            @Param("nextStatus") CreatorApplicationStatus nextStatus,
            @Param("displayName") String displayName,
            @Param("bio") String bio,
            @Param("category") String category,
            @Param("portfolioUrl") String portfolioUrl,
            @Param("submittedTime") Date submittedTime);
}
