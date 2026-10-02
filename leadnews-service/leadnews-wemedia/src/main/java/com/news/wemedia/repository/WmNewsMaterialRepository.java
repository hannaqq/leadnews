package com.news.wemedia.repository;

import com.news.model.wemedia.pojos.WmNewsMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WmNewsMaterialRepository extends JpaRepository<WmNewsMaterial, Integer> {
    boolean existsByMaterialId(Integer materialId);
    void deleteByNewsId(Integer newsId);
    List<WmNewsMaterial> findByNewsIdOrderByTypeAscOrdAsc(Integer newsId);
}
