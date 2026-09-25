package com.news.model.schedule.dtos;

import lombok.Data;

import java.io.Serializable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class Task implements Serializable {


    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskId;

    private Integer taskType;

    private Integer priority;

    private long executeTime;

    private byte[] parameters;
    
}