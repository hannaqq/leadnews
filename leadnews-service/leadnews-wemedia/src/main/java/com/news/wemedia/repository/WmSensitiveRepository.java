package com.news.wemedia.repository;

import com.news.model.wemedia.pojos.WmSensitive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WmSensitiveRepository extends JpaRepository<WmSensitive, Integer> {
    Page<WmSensitive> findBySensitivesContainingIgnoreCase(String value, Pageable pageable);
}
