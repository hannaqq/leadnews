package com.news.user.repository;

import com.news.model.user.pojos.ApUserRealname;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApUserRealnameRepository extends JpaRepository<ApUserRealname, Integer> {

    @Query("""
            select realname from ApUserRealname realname
            where (:id is null or realname.id = :id)
              and (:status is null or realname.status = :status)
            """)
    Page<ApUserRealname> findForReview(
            @Param("id") Integer id,
            @Param("status") Short status,
            Pageable pageable);
}
