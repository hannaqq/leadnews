package com.news.schedule.service.impl;

import com.news.common.constants.ScheduleConstants;
import com.news.model.schedule.dtos.Task;
import com.news.model.schedule.pojos.TaskinfoLogs;
import com.news.schedule.ScheduleApplication;
import com.news.schedule.repository.TaskinfoLogsRepository;
import com.news.schedule.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Concurrent stress test for execute vs cancel race on delayed tasks.
 *
 * Scenario:
 * 1) Create many immediately-ready tasks
 * 2) Start cancel threads and poll threads at the same time
 * 3) Assert every task ends in exactly one terminal state (EXECUTED or CANCELLED)
 *
 * Requires local MySQL + Redis to be available.
 */
@SpringBootTest(
        classes = ScheduleApplication.class,
        properties = {
                "spring.config.import=optional:consul:",
                "spring.cloud.consul.discovery.enabled=false"
        }
)
@ExtendWith(SpringExtension.class)
@EnabledIfEnvironmentVariable(named = "RUN_SCHEDULE_STRESS_TEST", matches = "true")
public class TaskRaceConditionStressTest {

    private static final int TASK_TYPE = 9001;
    private static final int PRIORITY = 1;
    private static final int TASK_COUNT = 200;
    private static final int CANCEL_THREADS = 8;
    private static final int POLL_THREADS = 8;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskinfoLogsRepository taskinfoLogsRepository;

    @Test
    public void executeAndCancelShouldNotOverwriteEachOther() throws Exception {
        List<Long> taskIds = new ArrayList<>(TASK_COUNT);
        for (int i = 0; i < TASK_COUNT; i++) {
            Task task = new Task();
            task.setTaskType(TASK_TYPE);
            task.setPriority(PRIORITY);
            task.setParameters(("race-test-" + i).getBytes());
            // ready immediately -> goes into Redis List
            task.setExecuteTime(System.currentTimeMillis());
            taskIds.add(taskService.addTask(task));
        }

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(CANCEL_THREADS + POLL_THREADS);
        AtomicInteger cancelSuccess = new AtomicInteger();
        AtomicInteger pollSuccess = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(CANCEL_THREADS + POLL_THREADS);

        for (int i = 0; i < CANCEL_THREADS; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    for (Long taskId : taskIds) {
                        if (taskService.cancelTask(taskId)) {
                            cancelSuccess.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    done.countDown();
                }
            });
        }

        for (int i = 0; i < POLL_THREADS; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    // keep polling until list is mostly drained
                    for (int j = 0; j < TASK_COUNT * 2; j++) {
                        Task polled = taskService.poll(TASK_TYPE, PRIORITY);
                        if (polled != null) {
                            pollSuccess.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        assertTrue(done.await(60, TimeUnit.SECONDS), "stress test timed out");
        pool.shutdownNow();

        int executed = 0;
        int cancelled = 0;
        int scheduled = 0;
        int unexpected = 0;

        for (Long taskId : taskIds) {
            TaskinfoLogs log = taskinfoLogsRepository.findById(taskId).orElse(null);
            assertNotNull(log, "missing task log for id=" + taskId);
            Integer status = log.getStatus();
            if (status == null) {
                unexpected++;
            } else if (status == ScheduleConstants.EXECUTED) {
                executed++;
            } else if (status == ScheduleConstants.CANCELLED) {
                cancelled++;
            } else if (status == ScheduleConstants.SCHEDULED) {
                scheduled++;
            } else {
                unexpected++;
            }
        }

        System.out.println("===== Race Stress Result =====");
        System.out.println("tasks=" + TASK_COUNT);
        System.out.println("cancelSuccessCalls=" + cancelSuccess.get());
        System.out.println("pollSuccessCalls=" + pollSuccess.get());
        System.out.println("EXECUTED=" + executed);
        System.out.println("CANCELLED=" + cancelled);
        System.out.println("SCHEDULED(left)=" + scheduled);
        System.out.println("UNEXPECTED=" + unexpected);

        // With state-machine conditional update:
        // - no illegal transition / overwrite
        // - every task should be finalized exactly once
        assertEquals(0, unexpected);
        assertEquals(0, scheduled);
        assertEquals(TASK_COUNT, executed + cancelled);
        assertEquals(TASK_COUNT, cancelSuccess.get() + pollSuccess.get());
    }
}
