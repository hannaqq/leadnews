package com.news.article.repository;

import com.news.model.article.pojos.ApCollection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApCollectionRepository extends JpaRepository<ApCollection, Long> {
    long deleteByEntryId(Long entryId);
}
