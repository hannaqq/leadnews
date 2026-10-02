package com.news.model.user.pojos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "ap_user_realname")
public class ApUserRealname implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id")
    private Integer id;
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "name")
    private String name;
    @Column(name = "idno")
    private String idno;
    @Column(name = "font_image")
    private String fontImage;
    @Column(name = "back_image")
    private String backImage;
    @Column(name = "hold_image")
    private String holdImage;
    @Column(name = "live_image")
    private String liveImage;
    @Column(name = "status")
    private Short status;
    @Column(name = "reason")
    private String reason;
    @Column(name = "created_time")
    private Date createdTime;
    @Column(name = "submitted_time")
    private Date submittedTime;
    @Column(name = "updated_time")
    private Date updatedTime;
}
