package com.news.admin.repository;

import com.news.model.admin.pojos.AdUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdUserRepository extends JpaRepository<AdUser, Integer> {

    Optional<AdUser> findByName(String name);
}
