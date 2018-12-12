package com.github.johnsonmoon.queue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CPU资源占用测试
 * <p>
 * Create by johnsonmoon at 2018/11/16 13:47.
 */
public class CPUOccupationTest {
    private static Logger logger = LoggerFactory.getLogger(CPUOccupationTest.class);

    @Test
    public void test() {
        QueueProducer<String> producer = new QueueProducer<>();
        QueueExecutor<String> executor = new QueueExecutor<String>()
                .consumeInterval(200)
                .consumeMaxSize(100)
                .producer(producer)
                .consumer(System.out::println);

        executor.start();

        for (int j = 0; j < 500; j++) {
            for (int i = 0; i < 150; i++) {
                producer.put(i + "");
            }
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }

        executor.stop();
    }
}
