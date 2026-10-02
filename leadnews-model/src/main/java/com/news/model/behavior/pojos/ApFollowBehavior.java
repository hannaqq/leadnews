package com.news.model.behavior.pojos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "ap_follow_behavior")
public class ApFollowBehavior implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Column(name = "entry_id")
    private Integer entryId;

    @Column(name = "article_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long articleId;

    @Column(name = "follow_id")
    private Integer followId;

    @Column(name = "created_time")
    private Date createdTime;
}
