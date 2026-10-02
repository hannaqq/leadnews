package com.news.es.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class SearchArticleVo {

    public SearchArticleVo() {
    }

    public SearchArticleVo(Long id, String title, Date publishTime, Short layout, String images,
                           Long authorId, String authorName, String staticUrl, String content) {
        this.id = id;
        this.title = title;
        this.publishTime = publishTime;
        this.layout = layout == null ? null : layout.intValue();
        this.images = images;
        this.authorId = authorId;
        this.authorName = authorName;
        this.staticUrl = staticUrl;
        this.content = content;
    }

    private Long id;
    private String title;
    private Date publishTime;
    private Integer layout;
    private String images;
    private Long authorId;
    private String authorName;
    private String staticUrl;
    private String content;

}
