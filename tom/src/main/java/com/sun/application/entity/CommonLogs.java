package com.sun.application.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class CommonLogs {
    private List<CommonLog> commonLogs;
}
