package com.example.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class DLQUtil {
    //    TODO process delay queue is forced to empty when application is shutdown
    public static final DelayQueue<DLQEntry<Long>> COMMON_DLQ = new DelayQueue<>();
    private final ExecutorService executorService;

    public DLQUtil() {
        int MAX_THREAD = 2;
        executorService = Executors.newFixedThreadPool(MAX_THREAD);
        execute();
    }

    public static void addToDLQ(Long requestId, long delay, TimeUnit timeUnit, Runnable runnable) {
        COMMON_DLQ.add(new DLQEntry<>(requestId, delay, timeUnit, runnable));
    }

    public void execute() {
        Thread evictionThread = new Thread(() -> {
            while (true) {
                try {
                    var entry = COMMON_DLQ.take();
                    executorService.execute(entry::apply);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }

            }
        });
        evictionThread.setDaemon(true);
        evictionThread.start();
    }
}
