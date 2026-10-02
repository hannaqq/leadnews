package com.news.wemedia.repository;

import com.news.model.wemedia.pojos.WmUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WmUserRepository extends JpaRepository<WmUser, Integer> {
    Optional<WmUser> findByName(String name);
    Optional<WmUser> findByApUserId(Integer apUserId);
}
