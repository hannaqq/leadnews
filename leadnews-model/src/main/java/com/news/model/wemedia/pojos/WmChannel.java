package com.news.model.wemedia.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Channel information entity
 *
 * @author itheima
 */
@Data
@TableName("wm_channel")
public class WmChannel implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * Channel name
     */
    @TableField("name")
    private String name;

    /**
     * Channel description
     */
    @TableField("description")
    private String description;

    /**
     * Whether default channel
     * 1: Default (true)
     * 0: Non-default (false)
     */
    @TableField("is_default")
    private Boolean isDefault;

    /**
     * Whether enabled
     * 1: Enabled (true)
     * 0: Disabled (false)
     */
    @TableField("status")
    private Boolean status;

    /**
     * Default sort order
     */
    @TableField("ord")
    private Integer ord;

    /**
     * Creation time
     */
    @TableField("created_time")
    private Date createdTime;

}