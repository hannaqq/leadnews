package com.news.model.user.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("ap_user_fan")
public class ApUserFan implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("user_Id")
    private Integer userId;

    @TableField("fans_id")
    private Integer fansId;

    @TableField("fans_name")
    private String fansName;

    @TableField("level")
    private Short level;

    @TableField("is_display")
    private Short isDisplay;

    @TableField("is_shield_letter")
    private Short isShieldLetter;

    @TableField("is_shield_comment")
    private Short isShieldComment;

    @TableField("created_time")
    private Date createdTime;

}
