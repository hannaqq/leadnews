package com.news.model.article.pojos;

import com.news.model.persistence.SnowflakeId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "ap_article")
public class ApArticle implements Serializable {

    @Id
    @SnowflakeId
    @Column(name = "id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "author_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long authorId;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "channel_id")
    private Integer channelId;

    @Column(name = "channel_name")
    private String channelName;

    /**
     * Article layout type
     * 0: No image
     * 1: Single image
     * 2: Multiple images
     */
    @Column(name = "layout")
    private Short layout;

    /**
     * Article flag
     * 0: Normal article
     * 1: Hot article
     * 2: Pinned article
     * 3: Featured article
     * 4: VIP article
     */
    @Column(name = "flag")
    private Byte flag;

    /**
     * Article cover images (comma-separated)
     */
    @Column(name = "images")
    private String images;

    /**
     * Article tags
     */
    @Column(name = "labels")
    private String labels;

    /**
     * Like count
     */
    @Column(name = "likes")
    private Integer likes;

    /**
     * Collection count
     */
    @Column(name = "collection")
    private Integer collection;

    /**
     * Comment count
     */
    @Column(name = "comment")
    private Integer comment;

    /**
     * View count
     */
    @Column(name = "views")
    private Integer views;


    @Column(name = "province_id")
    private Integer provinceId;

    @Column(name = "city_id")
    private Integer cityId;

    @Column(name = "county_id")
    private Integer countyId;


    @Column(name = "created_time")
    private Date createdTime;

    @Column(name = "publish_time")
    private Date publishTime;

    /**
     * Synchronization status
     */
    @Column(name = "sync_status")
    private Boolean syncStatus;

    /**
     * Article origin
     */
    @Column(name = "origin")
    private Boolean origin;

    /**
     * Static page URL
     */
    @Column(name = "static_url")
    private String staticUrl;
}
