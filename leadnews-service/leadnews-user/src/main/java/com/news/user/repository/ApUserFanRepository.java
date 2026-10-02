package com.news.user.repository;

import com.news.model.user.pojos.ApUserFan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApUserFanRepository extends JpaRepository<ApUserFan, Integer> {
    long deleteByFansIdAndUserId(Integer fansId, Integer userId);
}
