package com.news.model.user.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("ap_user")
public class ApUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("salt")
    private String salt;

    @TableField("name")
    private String name;

    @TableField("password")
    private String password;

    @TableField("phone")
    private String phone;

    @TableField("image")
    private String image;

    @TableField("sex")
    private Boolean sex;

    @TableField("is_certification")
    private Boolean isCertification;

    @TableField("is_identity_authentication")
    private Boolean isIdentityAuthentication;

    @TableField("status")
    private Short status;

    @TableField("flag")
    private Integer flag;

    @TableField("created_time")
    private Date createdTime;

}
