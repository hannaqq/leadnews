package com.news.model.user.pojos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "ap_user")
public class ApUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id")
    private Integer id;
    @Column(name = "salt")
    private String salt;
    @Column(name = "name")
    private String name;
    @Column(name = "password")
    private String password;
    @Column(name = "phone")
    private String phone;
    @Column(name = "image")
    private String image;
    @Column(name = "sex")
    private Boolean sex;
    @Column(name = "is_certification")
    private Boolean isCertification;
    @Column(name = "is_identity_authentication")
    private Boolean isIdentityAuthentication;
    @Column(name = "status")
    private Short status;
    @Column(name = "flag")
    private Integer flag;
    @Column(name = "created_time")
    private Date createdTime;

}
