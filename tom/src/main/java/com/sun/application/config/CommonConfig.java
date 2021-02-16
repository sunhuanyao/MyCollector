package com.sun.application.config;

import com.sun.tomorrow.core.tool.elasticsearch.EsInstance;
import com.sun.tomorrow.core.tool.kafka.KafkaInstance;
import com.sun.tomorrow.core.tool.redis.RedisInstance;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

@Configuration
public class CommonConfig {

    @Bean(name = "kafkaConsumer")
    public KafkaConsumer<String, String> kafkaConsumer() {
        return new KafkaInstance().getConsumerClient();
    }

    @Bean(name = "kafkaProducer")
    public KafkaProducer<String, String> kafkaProducer() {
        return new KafkaInstance().getProducerClient();
    }

    @Bean(name = "myEsRestClient")
    public RestHighLevelClient restHighLevelClient() {
        return EsInstance.getInstance().getClient();
    }

    @Bean(name = "jedisPool")
    public JedisPool getJedisPool() {
        return RedisInstance.getInstance().getJedisPool();
    }
}
