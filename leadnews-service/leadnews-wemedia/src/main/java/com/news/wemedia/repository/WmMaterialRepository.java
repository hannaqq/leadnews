package com.news.wemedia.repository;

import com.news.model.wemedia.pojos.WmMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WmMaterialRepository extends JpaRepository<WmMaterial, Integer> {
    Page<WmMaterial> findByUserId(Integer userId, Pageable pageable);
    Page<WmMaterial> findByUserIdAndIsCollection(Integer userId, Short isCollection, Pageable pageable);
    List<WmMaterial> findByUserIdAndUrlIn(Integer userId, Collection<String> urls);
    Optional<WmMaterial> findByIdAndUserId(Integer id, Integer userId);
}
