package com.news.model.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskTypeEnum {

    NEWS_PUBLISH(1003, 1,"publish approved news"),
    REMOTEERROR(1002, 2,"error, try again");
    private final int taskType;
    private final int priority;
    private final String desc;
}