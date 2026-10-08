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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer userId;
    private String title;
    @Lob
    private String content;
    private Integer channelId;
    private String labels;
    private Date createdTime;
    private Date submitedTime;
    private Date publishTime;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long articleId;
    private String images;
    private Short enable;

    /**
     * Article layout type
     * 0: No image
     * 1: Single image
     * 3: Multiple images
     */
    private Short type;

    /**
     * Current article status
     * 0: Draft
     * 1: Submitted (pending review)
     * 2: Review failed
     * 3: Manual review
     * 7: Review or publication in progress
     * 8: Review passed (pending publish)
     * 9: Published
     */
    private Short status;
    private String reason;

    public enum Status{
        NORMAL((short)0),SUBMIT((short)1),FAIL((short)2),ADMIN_AUTH((short)3),
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
