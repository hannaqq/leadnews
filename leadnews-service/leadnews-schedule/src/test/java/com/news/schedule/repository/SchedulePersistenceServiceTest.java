package com.news.schedule.repository;

import com.news.common.constants.ScheduleConstants;
import com.news.model.schedule.dtos.Task;
import com.news.model.schedule.pojos.TaskinfoLogs;
import com.news.schedule.service.impl.SchedulePersistenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
@Import(SchedulePersistenceService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SchedulePersistenceServiceTest {
    @Autowired private SchedulePersistenceService persistenceService;
    @Autowired private TaskinfoRepository taskinfoRepository;
    @Autowired private TaskinfoLogsRepository taskinfoLogsRepository;

    @Test
    void onlyOneConcurrentTerminalTransitionWins() throws Exception {
        Task task = new Task();
        task.setExecuteTime(System.currentTimeMillis());
        task.setTaskType(10);
        task.setPriority(1);
        task.setParameters(new byte[]{1, 2, 3});
        long taskId = persistenceService.addTask(task);

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Task> executed = executor.submit(() -> {
                start.await();
                return persistenceService.finalizeTask(taskId, ScheduleConstants.EXECUTED);
            });
            Future<Task> cancelled = executor.submit(() -> {
                start.await();
                return persistenceService.finalizeTask(taskId, ScheduleConstants.CANCELLED);
            });
            start.countDown();

            Task first = executed.get();
            Task second = cancelled.get();
            assertEquals(1, (first == null ? 0 : 1) + (second == null ? 0 : 1));
            assertNotNull(first != null ? first : second);
        } finally {
            executor.shutdownNow();
        }

        TaskinfoLogs log = taskinfoLogsRepository.findById(taskId).orElseThrow();
        assertEquals(1, log.getVersion());
        assertFalse(taskinfoRepository.existsById(taskId));
        assertNull(persistenceService.finalizeTask(taskId, ScheduleConstants.CANCELLED));
    }
}
