package com.news.model.admin.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("ad_user")
public class AdUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

    @TableField("name")
    private String name;

    @TableField("password")
    private String password;

    @TableField("salt")
    private String salt;

    @TableField("nickname")
    private String nickName;

    @TableField("image")
    private String image;

    @TableField("phone")
    private String phone;

    @TableField("status")
    private Short status;

    @TableField("email")
    private String email;

    @TableField("login_time")
    private Date loginTime;

    @TableField("created_time")
    private Date createdTime;

}
