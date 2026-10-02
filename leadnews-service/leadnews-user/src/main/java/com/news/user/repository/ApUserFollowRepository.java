package com.news.user.repository;

import com.news.model.user.pojos.ApUserFollow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApUserFollowRepository extends JpaRepository<ApUserFollow, Integer> {
    boolean existsByUserIdAndFollowId(Integer userId, Integer followId);
    long deleteByUserIdAndFollowId(Integer userId, Integer followId);
}
