package com.sun.application.entity;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class CommonLog {
    private String index;
    private Map<String, Object> mapping;
    private List<String> agg;
    private Map<String, String> setting;
}
