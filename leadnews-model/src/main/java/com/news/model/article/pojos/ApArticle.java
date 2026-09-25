package com.news.model.article.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
@TableName("ap_article")
public class ApArticle implements Serializable {

    @TableId(value = "id",type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String title;
    @TableField("author_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long authorId;
    @TableField("author_name")
    private String authorName;
    @TableField("channel_id")
    private Integer channelId;
    @TableField("channel_name")
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

    /**
     * Article cover images (comma-separated)
     */
    private String images;

    /**
     * Article tags
     */
    private String labels;

    /**
     * Like count
     */
    private Integer likes;

    /**
     * Collection count
     */
    private Integer collection;

    /**
     * Comment count
     */
    private Integer comment;

    /**
     * View count
     */
    private Integer views;


    @TableField("province_id")
    private Integer provinceId;

    @TableField("city_id")
    private Integer cityId;

    @TableField("county_id")
    private Integer countyId;


    @TableField("created_time")
    private Date createdTime;

    @TableField("publish_time")
    private Date publishTime;

    /**
     * Synchronization status
     */
    @TableField("sync_status")
    private Boolean syncStatus;

    /**
     * Article origin
     */
    private Boolean origin;

    /**
     * Static page URL
     */
    @TableField("static_url")
    private String staticUrl;
}
