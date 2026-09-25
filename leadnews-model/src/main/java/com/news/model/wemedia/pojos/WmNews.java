package com.news.model.wemedia.pojos;

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
@TableName("wm_news")
public class WmNews implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField("user_id")
    private Integer userId;
    @TableField("title")
    private String title;
    @TableField("content")
    private String content;
    @TableField("channel_id")
    private Integer channelId;
    @TableField("labels")
    private String labels;
    @TableField("created_time")
    private Date createdTime;
    @TableField("submited_time")
    private Date submitedTime;
    @TableField("publish_time")
    private Date publishTime;
    @TableField("article_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long articleId;
    @TableField("images")
    private String images;
    @TableField("enable")
    private Short enable;

    /**
     * Article layout type
     * 0: No image
     * 1: Single image
     * 3: Multiple images
     */
    @TableField("type")
    private Short type;

    /**
     * Current article status
     * 0: Draft
     * 1: Submitted (pending review)
     * 2: Review failed
     * 3: Manual review
     * 4: Manual review passed
     * 8: Review passed (pending publish)
     * 9: Published
     */
    @TableField("status")
    private Short status;
    /**
     * Rejection reason
     */
    @TableField("reason")
    private String reason;

    /**
     * Article status enumeration
     */
    public enum Status{
        NORMAL((short)0),SUBMIT((short)1),FAIL((short)2),ADMIN_AUTH((short)3),ADMIN_SUCCESS((short)4),SUCCESS((short)8),PUBLISHED((short)9);
         short code;
         Status(short code){
             this.code = code;
         }
         public short getCode(){
             return this.code;
         }
    }

}