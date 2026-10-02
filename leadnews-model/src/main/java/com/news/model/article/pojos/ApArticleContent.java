package com.news.model.article.pojos;

import com.news.model.persistence.SnowflakeId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
@Table(name = "ap_article_content")
public class ApArticleContent implements Serializable {

    @Id
    @SnowflakeId
    @Column(name = "id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Column(name = "article_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long articleId;

    @Lob
    @Column(name = "content")
    private String content;
}
