package com.news.schedule.service.transaction;

import com.news.common.constants.ScheduleConstants;
import com.news.model.schedule.dtos.Task;
import com.news.model.schedule.pojos.TaskinfoLogs;
import com.news.schedule.repository.TaskinfoLogsRepository;
import com.news.schedule.repository.TaskinfoRepository;
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
@Import(ScheduleTransactionService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ScheduleTransactionServiceTest {
    @Autowired private ScheduleTransactionService transactionService;
    @Autowired private TaskinfoRepository taskinfoRepository;
    @Autowired private TaskinfoLogsRepository taskinfoLogsRepository;

    @Test
    void onlyOneConcurrentTerminalTransitionWins() throws Exception {
        Task task = new Task();
        task.setExecuteTime(System.currentTimeMillis());
        task.setTaskType(10);
        task.setPriority(1);
        task.setParameters(new byte[]{1, 2, 3});
        long taskId = transactionService.addTaskToDB(task);

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Task> executed = executor.submit(() -> {
                start.await();
                return transactionService.markExecutedInDB(taskId);
            });
            Future<Task> cancelled = executor.submit(() -> {
                start.await();
                return transactionService.cancelTaskInDB(taskId);
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
        assertNull(transactionService.cancelTaskInDB(taskId));
    }
}
