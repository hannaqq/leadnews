package com.news.model.behavior.dtos;

import lombok.Data;

import java.util.Date;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class CollectionBehaviorDto {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long entryId;
    private Short operation;
    private Date publishedTime;
    private Short type;
}
