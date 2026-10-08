package com.news.user.repository;

import com.news.model.user.pojos.ApUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApUserRepository extends JpaRepository<ApUser, Integer> {
    Optional<ApUser> findByPhone(String phone);
}
