package com.news.model.wemedia.pojos;

import jakarta.persistence.*;
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
        name = "wm_user",
        uniqueConstraints = @UniqueConstraint(name = "uk_wm_user_ap_user_id", columnNames = "ap_user_id")
)
public class WmUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer apUserId;

    private String name;

    private String password;

    private String nickname;

    private String image;

    private String location;

    private String phone;

    /**
     * Account status
     * 0: Temporarily unavailable
     * 1: Permanently unavailable
     * 9: Available
     */
    private Short status;

    private String email;

    /**
     * Account type
     * 0: Personal
     * 1: Enterprise
     * 2: Sub-account
     */
    private Integer type;

    private Integer score;

    private Date loginTime;

    private Date createdTime;

}
