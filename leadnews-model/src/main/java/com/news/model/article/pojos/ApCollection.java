package com.news.model.article.pojos;

import com.news.model.persistence.SnowflakeId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ap_collection")
public class ApCollection {
    @Id
    @SnowflakeId
    @Column(name = "id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Column(name = "entry_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long entryId;

    @Column(name = "article_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long articleId;

    @Column(name = "type")
    private Short type;

    @Column(name = "published_time")
    private Date publishedTime;

    @Column(name = "collection_time")
    private Date collectionTime;
}
