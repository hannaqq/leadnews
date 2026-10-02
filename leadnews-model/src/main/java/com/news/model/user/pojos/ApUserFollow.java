package com.news.model.user.pojos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "ap_user_follow")
public class ApUserFollow implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id")
    private Integer id;
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "follow_id")
    private Integer followId;
    @Column(name = "follow_name")
    private String followName;
    @Column(name = "level")
    private Short level;
    @Column(name = "is_notice")
    private Short isNotice;
    @Column(name = "created_time")
    private Date createdTime;
}
