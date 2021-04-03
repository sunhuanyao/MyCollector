package com.sun.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@SpringBootApplication
@RestController
public class TomApplication {

    public static void main(String[] args) {
        SpringApplication.run(TomApplication.class, args);
    }
}
