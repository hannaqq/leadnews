package com.news.model.admin.pojos;

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

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ad_user")
public class AdUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "password")
    private String password;

    @Column(name = "salt")
    private String salt;

    @Column(name = "nickname")
    private String nickName;

    @Column(name = "image")
    private String image;

    @Column(name = "phone")
    private String phone;

    @Column(name = "status")
    private Short status;

    @Column(name = "email")
    private String email;

    @Column(name = "login_time")
    private Date loginTime;

    @Column(name = "created_time")
    private Date createdTime;

}
