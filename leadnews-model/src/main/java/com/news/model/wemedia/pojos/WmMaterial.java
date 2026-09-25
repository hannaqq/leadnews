package com.news.model.wemedia.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * WeMedia material information entity
 *
 * @author itheima
 */
@Data
@TableName("wm_material")
public class WmMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * WeMedia user ID
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * Material URL
     */
    @TableField("url")
    private String url;

    /**
     * Material type
     * 0: Image
     * 1: Video
     */
    @TableField("type")
    private Short type;

    /**
     * Whether collected
     */
    @TableField("is_collection")
    private Short isCollection;

    /**
     * Creation time
     */
    @TableField("created_time")
    private Date createdTime;

}