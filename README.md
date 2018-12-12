# queue-producer-consumer-model
A queue producer-consumer model algorithm  

When a producer produces fast, while consumers consumes slow, this algorithm may help you solve those problems. 

## Usage

Example like: 
```
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
```

And the result is: 
```
[2018-12-12 15:06:46:363] Consumes list, size: 100
[2018-12-12 15:06:47:810] Consumes list, size: 100
[2018-12-12 15:06:49:271] Consumes list, size: 100
[2018-12-12 15:06:50:724] Consumes list, size: 100
[2018-12-12 15:06:52:187] Consumes list, size: 100
[2018-12-12 15:06:53:626] Consumes list, size: 100
[2018-12-12 15:06:55:088] Consumes list, size: 100
[2018-12-12 15:06:56:540] Consumes list, size: 100
[2018-12-12 15:06:57:969] Consumes list, size: 100
[2018-12-12 15:06:59:415] Consumes list, size: 100
```