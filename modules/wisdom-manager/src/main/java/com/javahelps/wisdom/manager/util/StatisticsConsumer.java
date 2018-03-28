package com.javahelps.wisdom.manager.util;

import com.javahelps.wisdom.service.Utility;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StatisticsConsumer implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsConsumer.class);

    private final String groupId;
    private final String topic;
    private final String bootstrapServers;
    private transient boolean active = true;
    private final Lock lock = new ReentrantLock();
    private Consumer<String, String> consumer;
    private final ExecutorService executorService;
    private final StatsListener statsListener;

    public StatisticsConsumer(String bootstrapServers, String topic, String groupId, ExecutorService executorService, StatsListener statsListener) {
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
        this.groupId = groupId;
        this.executorService = executorService;
        this.statsListener = statsListener;
    }

    @Override
    public void run() {
        while (active) {
            ConsumerRecords<String, String> records = null;
            try {
                lock.lock();
                records = this.consumer.poll(1000);
            } catch (CommitFailedException e) {
                LOGGER.error("Kafka commit failed for topic " + this.topic, e);
            } finally {
                lock.unlock();
            }

            if (records != null) {
                records.forEach(record -> {
                    LOGGER.info("Received {} from Kafka partition {} with key {} and offset {}",
                            record.value(), record.partition(), record.key(), record.offset());
                    this.statsListener.onStats(Utility.toMap(record.value()));
                });
                try {
                    lock.lock();
                    if (!records.isEmpty()) {
                        this.consumer.commitAsync();
                    }
                } catch (CommitFailedException e) {
                    LOGGER.error("Kafka commit failed for topic " + this.topic, e);
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    public void start() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, this.groupId);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // Create the consumer using props.
        this.consumer = new KafkaConsumer<>(props);
        // Subscribe to the topic.
        this.consumer.subscribe(Collections.singletonList(this.topic));
        this.executorService.execute(this);
    }

    public void stop() {
        this.active = false;
        try {
            lock.lock();
            this.consumer.unsubscribe();
            this.consumer.close();
        } catch (CommitFailedException e) {
            LOGGER.error("Kafka commit failed for topic " + this.topic, e);
        } finally {
            lock.unlock();
        }
    }

    public interface StatsListener {
        void onStats(Map<String, Comparable> stats);
    }
}
