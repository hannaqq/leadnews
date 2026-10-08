package com.news.model.article.pojos;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "ap_article",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ap_article_source_news_id",
                columnNames = "source_news_id"))
public class ApArticle implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private Integer sourceNewsId;

    private String title;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long authorId;

    private String authorName;

    private Integer channelId;

    private String channelName;

    /**
     * Article layout type
     * 0: No image
     * 1: Single image
     * 2: Multiple images
     */
    private Short layout;

    /**
     * Article flag
     * 0: Normal article
     * 1: Hot article
     * 2: Pinned article
     * 3: Featured article
     * 4: VIP article
     */
    private Byte flag;

    private String images;

    private String labels;

    private Integer likes;

    private Integer collection;

    private Integer comment;

    private Integer views;


    private Integer provinceId;

    private Integer cityId;

    private Integer countyId;


    private Date createdTime;

    private Date publishTime;

    private Boolean syncStatus;

    private Boolean origin;

    private String staticUrl;
}
