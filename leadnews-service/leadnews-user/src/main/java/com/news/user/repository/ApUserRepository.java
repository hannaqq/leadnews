package com.news.user.repository;

import com.news.model.user.pojos.ApUser;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ApUserRepository extends JpaRepository<ApUser, Integer> {
    Optional<ApUser> findByPhone(String phone);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from ApUser user where user.id = :id")
    Optional<ApUser> findByIdForUpdate(@Param("id") Integer id);
}
