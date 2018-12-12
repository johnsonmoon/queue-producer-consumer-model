package com.github.johnsonmoon.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Interval Queue consumer executor.
 *
 * @param <T> product type
 */
public class QueueExecutor<T> {
    private static Logger logger = LoggerFactory.getLogger(QueueExecutor.class);
    /**
     * shutdown flag
     */
    private AtomicBoolean shutdown = new AtomicBoolean(false);

    /**
     * maximum products blocking count
     */
    private int consumeMaxSize = 1000;

    /**
     * Interval between two consumes
     */
    private long consumeInterval = 200L;

    /**
     * The producer
     */
    private QueueProducer<T> producer;

    /**
     * The consumer array
     */
    private List<QueueConsumer<T>> consumers;

    /**
     * Set the max consuming count of one single consumption
     *
     * @param consumeMaxSize max consuming count
     * @return {@link QueueExecutor}
     */
    public QueueExecutor<T> consumeMaxSize(int consumeMaxSize) {
        this.consumeMaxSize = consumeMaxSize;
        return this;
    }

    /**
     * Set the interval between two consumes
     *
     * @param consumeInterval interval between two consumes
     * @return {@link QueueExecutor}
     */
    public QueueExecutor<T> consumeInterval(long consumeInterval) {
        this.consumeInterval = consumeInterval;
        return this;
    }

    /**
     * Set the producer
     *
     * @param producer producer queue
     * @return @return {@link QueueExecutor}
     */
    public QueueExecutor<T> producer(QueueProducer<T> producer) {
        this.producer = producer;
        return this;
    }

    /**
     * Add a consumer.
     *
     * @param consumer consumer
     * @return {@link QueueExecutor}
     */
    public QueueExecutor<T> consumer(QueueConsumer<T> consumer) {
        if (this.consumers == null) {
            this.consumers = new ArrayList<>();
        }
        this.consumers.add(consumer);
        return this;
    }

    /**
     * The executor loop thread.
     */
    private Thread executorLoopThread;

    /**
     * Start the executor loop
     */
    public void start() {
        if (shutdown.get()) {
            return;
        }
        if (producer == null || consumers == null) {
            throw new RuntimeException("Queue producer or consumers must not be null.");
        }
        shutdown.set(false);
        executorLoopThread = new Thread(() -> {
            List<T> list = new ArrayList<>();
            while (!shutdown.get()) {
                try {
                    /**
                     * <pre>
                     * XXX 防止频繁循环(无产品poll出来情况下类似于电机'空转')
                     * 导致占用CPU资源,
                     * 睡眠时间又不可以太大, 太大导致消费性能降低
                     * 实测稳定CPU占用率区间:
                     * XXX To prevent the CPU from 'idling'(like idling of a motor when no product was polled)
                     * leads to a high consumption of the CPU resources,
                     * it is necessary to sleep for a while.
                     * different sleep time CPU consumption:
                     *  1ms: 1.9% - 3.2%
                     *  2ms: 1.8% - 2.6%
                     *  3ms: 1.1% - 1.7%
                     *  4ms: 0.9% - 1.6%
                     *  5ms: 0.7% - 1.5%
                     * 10ms: 0.3% - 1.1% <==
                     * 20ms: 0.3% - 0.7%
                     * 30ms: 0.2% - 0.7%
                     * 40ms: 0.2% - 0.9%
                     * 50ms: 0.1% - 0.6%
                     * 60ms: 0.1% - 0.7%
                     * </pre>
                     * XXX 最终选择 10ms
                     * XXX And finally I chose 10ms
                     */
                    sleep(10);
                    T t = producer.poll();
                    if (t != null) {
                        list.add(t);
                    } else {
                        if (list.size() > 0) {
                            doConsume(list, consumers);
                            list = new ArrayList<>();
                            sleep(consumeInterval);
                        }
                    }
                    if (list.size() >= consumeMaxSize) {
                        doConsume(list, consumers);
                        list = new ArrayList<>();
                        sleep(consumeInterval);
                    }
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        });
        executorLoopThread.start();
    }

    /**
     * Stop the executor loop
     */
    public void stop() {
        if (!shutdown.get()) {
            return;
        }
        shutdown.set(true);
        executorLoopThread.interrupt();
    }

    private void doConsume(List<T> tList, List<QueueConsumer<T>> consumers) {
        for (QueueConsumer<T> consumer : consumers) {
            consumerThreadPool.submit(() -> consumer.consume(tList));
        }
    }

    /**
     * The consumer thread pool
     */
    private ExecutorService consumerThreadPool = Executors.newCachedThreadPool();

    private static void sleep(long timeMillis) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeMillis);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }
}