package com.sun.application.config;

import com.sun.tomorrow.core.tool.elasticsearch.EsInstance;
import com.sun.tomorrow.core.tool.kafka.KafkaInstance;
import com.sun.tomorrow.core.tool.redis.RedisInstance;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

@Configuration
public class CommonConfig {

    private static final Logger LOG = LoggerFactory.getLogger(CommonConfig.class);

    @Bean(name = "kafkaConsumer")
    public KafkaConsumer<String, String> kafkaConsumer() {
        LOG.info("custom kafka instance consumer start ---- sun");
        return new KafkaInstance().getConsumerClient();
    }

    @Bean(name = "kafkaProducer")
    public KafkaProducer<String, String> kafkaProducer() {
        LOG.info("custom kafka instance producer start ---- sun");
        return new KafkaInstance().getProducerClient();
    }

    @Bean(name = "myEsRestClient")
    public RestHighLevelClient restHighLevelClient() {
        LOG.info("rest high level client ---- sun");
        return EsInstance.getInstance().getClient();
    }

    @Bean(name = "jedisPool")
    public JedisPool getJedisPool() {
        LOG.info("Jedis pool start ---- sun");
        return RedisInstance.getInstance().getJedisPool();
    }
}
