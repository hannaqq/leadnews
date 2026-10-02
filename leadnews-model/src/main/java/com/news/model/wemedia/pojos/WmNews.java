package com.news.model.wemedia.pojos;

import jakarta.persistence.*;
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
@Table(name = "wm_news")
public class WmNews implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "title")
    private String title;
    @Lob
    @Column(name = "content")
    private String content;
    @Column(name = "channel_id")
    private Integer channelId;
    @Column(name = "labels")
    private String labels;
    @Column(name = "created_time")
    private Date createdTime;
    @Column(name = "submited_time")
    private Date submitedTime;
    @Column(name = "publish_time")
    private Date publishTime;
    @Column(name = "article_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long articleId;
    @Column(name = "images")
    private String images;
    @Column(name = "enable")
    private Short enable;

    /**
     * Article layout type
     * 0: No image
     * 1: Single image
     * 3: Multiple images
     */
    @Column(name = "type")
    private Short type;

    /**
     * Current article status
     * 0: Draft
     * 1: Submitted (pending review)
     * 2: Review failed
     * 3: Manual review
     * 4: Manual review passed
     * 7: Automatic review in progress
     * 8: Review passed (pending publish)
     * 9: Published
     */
    @Column(name = "status")
    private Short status;
    /**
     * Rejection reason
     */
    @Column(name = "reason")
    private String reason;

    /**
     * Article status enumeration
     */
    public enum Status{
        NORMAL((short)0),SUBMIT((short)1),FAIL((short)2),ADMIN_AUTH((short)3),ADMIN_SUCCESS((short)4),
        PROCESSING((short)7),SUCCESS((short)8),PUBLISHED((short)9);
         short code;
         Status(short code){
             this.code = code;
         }
         public short getCode(){
             return this.code;
         }
    }

}
