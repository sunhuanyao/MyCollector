package com.sun.application.controller;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class CollectorController {

    @Autowired
    private KafkaProducer<String, String> producer;

    @RequestMapping(value = "/log")
    public String log(HttpServletRequest request, @RequestBody String content) {
        ProducerRecord<String, String> record = new ProducerRecord<>("test", "test_1", content);
        producer.send(record);
        return "success";
    }

    @RequestMapping("/")
    String hello() {
        return "hello";
    }


}
