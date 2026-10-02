package com.news.model.article.pojos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "ap_author",
        uniqueConstraints = @UniqueConstraint(name = "uk_ap_author_user_id", columnNames = "user_id")
)
public class ApAuthor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    /**
     0 CRAWLER
     1 PARTNER
     2 CREATOR
     */
    @Column(name = "type")
    private Short type;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "wm_user_id")
    private Integer wmUserId;

    @Column(name = "created_time")
    private Date createdTime;

}
