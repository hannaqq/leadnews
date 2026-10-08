package com.news.model.article.vos;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class ArticleDetailVo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long articleId;

    private Integer creatorId;
    private String authorName;
    private String title;
    private String staticUrl;
    private Date publishTime;
}
