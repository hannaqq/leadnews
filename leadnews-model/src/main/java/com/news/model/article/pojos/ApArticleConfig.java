package com.news.model.article.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;


@Data
@NoArgsConstructor
@TableName("ap_article_config")
public class ApArticleConfig implements Serializable {

    public ApArticleConfig(Long articleId){
        this.articleId = articleId;
        this.isComment = true;
        this.isDelete = false;
        this.isDown = false;
        this.isForward = true;
    }

    @TableId(value = "id",type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @TableField("article_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long articleId;

    /**
     * true: can comment   1
     * false: can't comment  0
     */
    @TableField("is_comment")
    private Boolean isComment;

    /**
     * true: can be forwarded   1
     * false: can not be forwarded  0
     */
    @TableField("is_forward")
    private Boolean isForward;

    @TableField("is_down")
    private Boolean isDown;

    @TableField("is_delete")
    private Boolean isDelete;
}
