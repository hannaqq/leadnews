package com.news.model.behavior.dtos;

import lombok.Data;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class UnLikesBehaviorDto {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long articleId;
    private Short type;
}
