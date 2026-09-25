package com.news.model.user.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("ap_user_realname")
public class ApUserRealname implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("user_Id")
    private Integer userId;


    @TableField("name")
    private String name;

    @TableField("idno")
    private String idno;

    @TableField("font_image")
    private String fontImage;

    @TableField("back_image")
    private String backImage;

    @TableField("hold_image")
    private String holdImage;

    @TableField("live_image")
    private String liveImage;

    @TableField("status")
    private Short status;

    @TableField("reason")
    private String reason;

    @TableField("created_time")
    private Date createdTime;

    @TableField("submitted_time")
    private Date submittedTime;

    @TableField("updated_time")
    private Date updatedTime;
}
