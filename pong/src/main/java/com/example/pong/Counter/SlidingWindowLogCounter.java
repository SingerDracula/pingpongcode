package com.example.pong.Counter;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

public class SlidingWindowLogCounter {
    private int rate;
    private long windowSizeInMillis;
    private Deque<Long> requestTimestamps = new LinkedList<>();
    private ReentrantLock lock = new ReentrantLock();

    public SlidingWindowLogCounter(int rate, long windowSizeInMillis) {
        this.rate = rate;
        this.windowSizeInMillis = windowSizeInMillis;
    }

    public boolean allowRequest() {
        long now = System.currentTimeMillis();
        lock.lock();
        try {
            while (!requestTimestamps.isEmpty() && requestTimestamps.peekFirst() <= now - windowSizeInMillis) {
                requestTimestamps.pollFirst();
            }
            if (requestTimestamps.size() < rate) {
                requestTimestamps.addLast(now);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
}
