package com.news.model.behavior.dtos;

import lombok.Data;

@Data
public class FollowRelationDto {
    private Integer authorId;
    private Short operation;
}
