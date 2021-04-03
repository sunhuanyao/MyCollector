package com.sun.application.entity;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.elasticsearch.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class CommonLog {
    private String index;
    private Map<String, Object> mapping;
    private String agg;
    private Map<String, String> setting;

    public static List<String> parseAgg(String agg) {
        return CollectionUtils.arrayAsArrayList(agg.split("#"));
    }
}
