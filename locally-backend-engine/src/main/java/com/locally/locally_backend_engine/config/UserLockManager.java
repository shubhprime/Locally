package com.locally.locally_backend_engine.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Slf4j
public class UserLockManager {
    private final ConcurrentHashMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    public ReentrantLock getUserLock(Long userId) {
        return userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
    }

    public void releaseUserLock(Long userId) {
        ReentrantLock lock = userLocks.get(userId);
        if (lock != null && !lock.hasQueuedThreads()) {
            userLocks.remove(userId);
        }
    }

    // Simple cleanup every 10 minutes
    @Scheduled(fixedRate = 600000)
    public void cleanup() {
        userLocks.entrySet().removeIf(entry ->
                !entry.getValue().isLocked() && !entry.getValue().hasQueuedThreads());
    }
}