package com.news.model.wemedia.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * WeMedia user information entity
 *
 * @author itheima
 */
@Data
@TableName("wm_user")
public class WmUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("ap_user_id")
    private Integer apUserId;

    @TableField("ap_author_id")
    private Integer apAuthorId;

    /**
     * Login username
     */
    @TableField("name")
    private String name;

    /**
     * Login password
     */
    @TableField("password")
    private String password;

    /**
     * Password salt
     */
    @TableField("salt")
    private String salt;

    /**
     * Nickname
     */
    @TableField("nickname")
    private String nickname;

    /**
     * Avatar URL
     */
    @TableField("image")
    private String image;

    /**
     * Location
     */
    @TableField("location")
    private String location;

    /**
     * Phone number
     */
    @TableField("phone")
    private String phone;

    /**
     * Account status
     * 0: Temporarily unavailable
     * 1: Permanently unavailable
     * 9: Available
     */
    @TableField("status")
    private Short status;

    /**
     * Email address
     */
    @TableField("email")
    private String email;

    /**
     * Account type
     * 0: Personal
     * 1: Enterprise
     * 2: Sub-account
     */
    @TableField("type")
    private Integer type;

    /**
     * Operation score
     */
    @TableField("score")
    private Integer score;

    /**
     * Last login time
     */
    @TableField("login_time")
    private Date loginTime;

    /**
     * Creation time
     */
    @TableField("created_time")
    private Date createdTime;

}