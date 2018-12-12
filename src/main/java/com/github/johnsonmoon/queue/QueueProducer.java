package com.github.johnsonmoon.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @param <T> product type
 */
public class QueueProducer<T> {
    private static Logger logger = LoggerFactory.getLogger(QueueProducer.class);

    private BlockingQueue<T> blockingQueue;

    private Integer maxSize = 1000;

    public QueueProducer() {
    }

    public QueueProducer(Integer maxSize) {
        this.maxSize = maxSize;
    }

    private BlockingQueue<T> getQueue() {
        if (blockingQueue == null) {
            blockingQueue = new LinkedBlockingQueue<>(maxSize);
        }
        return blockingQueue;
    }

    /**
     * Get the product
     */
    T poll() {
        T result = null;
        try {
            result = getQueue().poll();
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Add a product
     */
    public void put(T t) {
        try {
            getQueue().put(t);
        } catch (InterruptedException e) {
            logger.warn(e.getMessage(), e);
        }
    }
}
