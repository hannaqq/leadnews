package com.news.model.user.pojos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "ap_user_fan")
public class ApUserFan implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id")
    private Integer id;
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "fans_id")
    private Integer fansId;
    @Column(name = "fans_name")
    private String fansName;
    @Column(name = "level")
    private Short level;
    @Column(name = "is_display")
    private Short isDisplay;
    @Column(name = "is_shield_letter")
    private Short isShieldLetter;
    @Column(name = "is_shield_comment")
    private Short isShieldComment;
    @Column(name = "created_time")
    private Date createdTime;

}
