package com.news.model.user.dtos;

import lombok.Data;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class UserRelationDto  {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long articleId;
    private Integer authorId;

    /*
    0 follow
    1 unfollow
     */
    private Short operation;

}
