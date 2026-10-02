package com.news.wemedia.repository;

import com.news.model.wemedia.pojos.WmChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WmChannelRepository extends JpaRepository<WmChannel, Integer> {
    Page<WmChannel> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
