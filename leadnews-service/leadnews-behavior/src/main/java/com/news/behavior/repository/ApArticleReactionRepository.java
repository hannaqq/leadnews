package com.news.behavior.repository;

import com.news.model.behavior.pojos.ApArticleReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApArticleReactionRepository extends JpaRepository<ApArticleReaction, Long> {

    Optional<ApArticleReaction> findByUserIdAndArticleId(Integer userId, Long articleId);
}
