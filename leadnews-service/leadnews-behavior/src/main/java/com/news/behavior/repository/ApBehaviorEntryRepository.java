package com.news.behavior.repository;

import com.news.model.behavior.pojos.ApBehaviorEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApBehaviorEntryRepository extends JpaRepository<ApBehaviorEntry, Long> {

    Optional<ApBehaviorEntry> findByEntryIdAndType(Integer entryId, Short type);
}
