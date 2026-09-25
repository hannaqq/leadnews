package com.news.model.article.dtos;

import lombok.Data;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class ArticleInfoDto {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long articleId;
    private Integer authorId;
}
