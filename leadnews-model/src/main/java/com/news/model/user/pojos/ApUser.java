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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String password;
    private String phone;
    private String image;
    private Boolean sex;
    private Boolean isCertification;
    private Boolean isIdentityAuthentication;
    private Short status;
    private Integer flag;
    private Date createdTime;

}
