package com.sun.application.listen;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.sun.application.entity.CommonLog;
import com.sun.application.entity.CommonLogs;
import com.sun.application.util.ToolUtil;
import com.sun.tomorrow.core.tool.config.JsonConfigReader;
import com.sun.tomorrow.core.tool.elasticsearch.EsInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 监听类 -- 主要 消费 定制化数据。定制化数据来源 参考来源common.json
 */
@Component
@Slf4j
public class DataListener {

    @Autowired
    private KafkaConsumer<String, String> kafkaConsumer;

    @Autowired
    private RestHighLevelClient myEsRestClient;

    @Autowired
    private JedisPool jedisPool;

    @PostConstruct
    public void listener() {
        log.info("start es init. . .");
        init();
        log.info("start listener ...");
        CompletableFuture.runAsync(() -> {
            kafkaConsumer.subscribe(Collections.singleton("test"));
            while(true) {
                ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(500));
                records.forEach(tmp -> {
                    log.info(tmp.key() + " " + tmp.value());
                    if (StringUtils.isEmpty(tmp.key())) {
                        log.error("index is null, please check.");
                        return;
                    }
                    dealData(tmp.key(), tmp.value());
                });
            }
        });
        log.info("end listener ... ");
    }


    /**
     * 初始化 es 和 redis, es 存的是 处理完逻辑的数据。
     * redis用于存储需要聚合的字段。
     */
    public void init() {
        String content = JsonConfigReader.parse(CommonLogs.class.getClassLoader(), "common.json");
        List<CommonLog> list = JSONObject.parseObject(content, new TypeReference<List<CommonLog>>(){}.getType());
        list.forEach(tmp -> {
            try {
                workEsInit(tmp);
                // 初始化要聚合的字段，放入缓存即可。如果存在，则无需初始化
                workRedis(tmp);
            } catch (IOException ex) {
                log.error("create index happen io exception. index: {}", tmp.getIndex());
            }
        });
    }

    /**
     * 初始化 redis 数据 扫描 所有的聚合字段，存缓存。
     *
     * @param commonLog common
     */
    public void workRedis(CommonLog commonLog) {
        String agg = commonLog.getAgg();
        if (StringUtils.isEmpty(agg)) {
            log.error("agg is null");
            return;
        }
        Jedis jedis = jedisPool.getResource();
        String key = commonLog.getIndex() + "_agg";
        if (!jedis.exists(key)) {
            log.info("init redis key:{}", key);
            jedis.set(key, agg);
        }
    }

    /**
     * 初始化 es
     *
     * @param commonLog common log
     * @throws IOException io 异常
     */
    public void workEsInit(CommonLog commonLog) throws IOException {
        String index = commonLog.getIndex();
        boolean mark = EsInstance.isIndexExists(myEsRestClient, index);
        if (mark) {
            log.info("index {} is created.", index);
            return;
        }
        CreateIndexRequest indexRequest = new CreateIndexRequest(index);
        Settings.Builder build = Settings.builder();
        // 加载setting
        for(Map.Entry<String, String> entry : commonLog.getSetting().entrySet()) {
            build.put(entry.getKey(), entry.getValue());
        }
        indexRequest.settings(build.build());
        Map<String, Object> map = commonLog.getMapping();
        // 加载mapping
        indexRequest.mapping(map);

        myEsRestClient.indices().create(indexRequest, RequestOptions.DEFAULT);

    }

    /**
     * 处理es数据  聚合 或者  技术。
     *
     * @param index 索引
     * @param data 数据
     */
    public void dealData (String index, String data) {
        log.info("start to deal data.");
        String redisKey = index + "_agg";
        Jedis jedis = jedisPool.getResource();
        Map<String, Object> objectMap = JSONObject.parseObject(data, new TypeReference<Map<String, Object>>(){}.getType());
        String fields = jedis.get(redisKey);
        String esId;
        long count = 1;
        if (!StringUtils.isEmpty(fields)) {
            log.info("field need to agg");
            List<String> agg = CommonLog.parseAgg(fields);
            esId = ToolUtil.generateEsId(index, agg, objectMap);
            if (jedis.exists(esId)) {
                count = jedis.incr(esId);
            } else {
                jedis.set(esId, "1");
                objectMap.put("createTime", System.currentTimeMillis());
            }
        } else {
            esId = UUID.randomUUID().toString();
            objectMap.put("createTime", System.currentTimeMillis());
        }

//        objectMap.put("_id", esId);
        objectMap.put("id", esId);
        objectMap.put("updateTime", System.currentTimeMillis());
        objectMap.put("count", count);
        try {
            String response = EsInstance.update(myEsRestClient, index, esId, objectMap);
            log.info("update data:{}", response);
        } catch (IOException ex) {
            log.error("update deal data is wrong.");
        }
    }


    public static void main(String[] args) {
        String content = JsonConfigReader.parse(CommonLogs.class.getClassLoader(), "common.json");
        List<CommonLog> list = JSONObject.parseObject(content, new TypeReference<List<CommonLog>>(){}.getType());
        System.out.println(list);
    }
}
