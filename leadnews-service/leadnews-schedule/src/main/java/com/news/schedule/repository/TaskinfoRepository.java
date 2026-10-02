package com.news.schedule.repository;

import com.news.model.schedule.pojos.Taskinfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface TaskinfoRepository extends JpaRepository<Taskinfo, Long> {
    List<Taskinfo> findByExecuteTimeBefore(Date executeTime);
}
