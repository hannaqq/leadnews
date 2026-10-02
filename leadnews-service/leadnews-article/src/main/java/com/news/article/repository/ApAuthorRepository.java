package com.news.article.repository;

import com.news.model.article.pojos.ApAuthor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApAuthorRepository extends JpaRepository<ApAuthor, Integer> {
    Optional<ApAuthor> findByUserId(Integer userId);
}
