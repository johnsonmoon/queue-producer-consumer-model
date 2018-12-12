package com.github.johnsonmoon.queue;


import java.util.List;

/**
 * Create by johnsonmoon at 2018/11/6 19:06.
 *
 * @param <T> product type
 */
public interface QueueConsumer<T> {
    /**
     * Consumes products where {@link QueueExecutor} outputs.
     *
     * @param tList products
     */
    void consume(List<T> tList);
}
