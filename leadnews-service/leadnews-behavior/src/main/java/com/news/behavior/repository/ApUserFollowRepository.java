package com.news.behavior.repository;

import com.news.model.behavior.pojos.ApUserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApUserFollowRepository extends JpaRepository<ApUserFollow, Integer> {

    boolean existsByUserIdAndCreatorId(Integer userId, Integer creatorId);

    @Modifying
    @Query("delete from ApUserFollow follow where follow.userId = :userId and follow.creatorId = :creatorId")
    int deleteByUserIdAndCreatorId(
            @Param("userId") Integer userId,
            @Param("creatorId") Integer creatorId);
}
