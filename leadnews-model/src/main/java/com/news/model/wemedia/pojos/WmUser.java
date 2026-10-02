package com.news.model.wemedia.pojos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * WeMedia user information entity
 *
 * @author itheima
 */
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

    /**
     * Primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "ap_user_id")
    private Integer apUserId;

    @Column(name = "ap_author_id")
    private Integer apAuthorId;

    /**
     * Login username
     */
    @Column(name = "name")
    private String name;

    /**
     * Login password
     */
    @Column(name = "password")
    private String password;

    /**
     * Password salt
     */
    @Column(name = "salt")
    private String salt;

    /**
     * Nickname
     */
    @Column(name = "nickname")
    private String nickname;

    /**
     * Avatar URL
     */
    @Column(name = "image")
    private String image;

    /**
     * Location
     */
    @Column(name = "location")
    private String location;

    /**
     * Phone number
     */
    @Column(name = "phone")
    private String phone;

    /**
     * Account status
     * 0: Temporarily unavailable
     * 1: Permanently unavailable
     * 9: Available
     */
    @Column(name = "status")
    private Short status;

    /**
     * Email address
     */
    @Column(name = "email")
    private String email;

    /**
     * Account type
     * 0: Personal
     * 1: Enterprise
     * 2: Sub-account
     */
    @Column(name = "type")
    private Integer type;

    /**
     * Operation score
     */
    @Column(name = "score")
    private Integer score;

    /**
     * Last login time
     */
    @Column(name = "login_time")
    private Date loginTime;

    /**
     * Creation time
     */
    @Column(name = "created_time")
    private Date createdTime;

}
