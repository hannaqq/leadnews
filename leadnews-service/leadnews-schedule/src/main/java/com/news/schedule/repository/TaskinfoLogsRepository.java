package com.news.schedule.repository;

import com.news.model.schedule.pojos.TaskinfoLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskinfoLogsRepository extends JpaRepository<TaskinfoLogs, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update TaskinfoLogs t
               set t.status = :nextStatus,
                   t.version = t.version + 1
             where t.taskId = :taskId
               and t.status = :scheduledStatus
            """)
    int finalizeScheduledTask(@Param("taskId") long taskId,
                              @Param("scheduledStatus") int scheduledStatus,
                              @Param("nextStatus") int nextStatus);
}
