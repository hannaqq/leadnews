package com.news.model.search.vos;

import lombok.Data;

import java.util.Date;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class SearchArticleVo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String title;

    private Date publishTime;

    private Integer layout;

    private String images;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long authorId;

    private String authorName;

    private String staticUrl;

    private String content;

}