package com.sun.application.util;

import java.util.List;
import java.util.Map;

public class ToolUtil {

    public static String generateEsId(String index, List<String> fields, Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append(index).append("#");
        fields.forEach(tmp -> {
            sb.append(map.get(tmp));
            sb.append("_");
        });
        return sb.delete(sb.length() - 1, sb.length()).toString();
    }

}
