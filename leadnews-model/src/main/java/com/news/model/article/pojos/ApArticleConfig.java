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
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ap_article_config")
public class ApArticleConfig implements Serializable {

    public ApArticleConfig(Long articleId){
        this.articleId = articleId;
        this.isComment = true;
        this.isDelete = false;
        this.isDown = false;
        this.isForward = true;
    }

    @Id
    @SnowflakeId
    @Column(name = "id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Column(name = "article_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long articleId;

    /**
     * true: can comment   1
     * false: can't comment  0
     */
    @Column(name = "is_comment")
    private Boolean isComment;

    /**
     * true: can be forwarded   1
     * false: can not be forwarded  0
     */
    @Column(name = "is_forward")
    private Boolean isForward;

    @Column(name = "is_down")
    private Boolean isDown;

    @Column(name = "is_delete")
    private Boolean isDelete;
}
