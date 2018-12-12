package com.github.johnsonmoon.queue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Create by johnsonmoon at 2018/11/1 12:43.
 */
public class ConsumerTest {
    private static Logger logger = LoggerFactory.getLogger(ConsumerTest.class);

    @Test
    public void test() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

        QueueProducer<String> producer = new QueueProducer<>(1000);
        QueueExecutor<String> executor = new QueueExecutor<String>()
                .consumeMaxSize(100)
                .consumeInterval(300L)
                .producer(producer)
                .consumer(list -> System.out.println(String.format("[%s] Consumes list, size: %s", simpleDateFormat.format(new Date()), list.size())));

        executor.start();

        for (int i = 0; i < 2000; i++) {
            producer.put("Product_" + i);
            try {
                Thread.sleep(2);
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }

        executor.stop();
    }
}
