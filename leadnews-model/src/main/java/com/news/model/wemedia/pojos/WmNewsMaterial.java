package com.news.model.wemedia.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * WeMedia news material reference entity
 *
 * @author itheima
 */
@Data
@TableName("wm_news_material")
public class WmNewsMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * Material ID
     */
    @TableField("material_id")
    private Integer materialId;

    /**
     * News ID
     */
    @TableField("news_id")
    private Integer newsId;

    /**
     * Reference type
     * 0: Content reference
     * 1: Cover image reference
     */
    @TableField("type")
    private Short type;

    /**
     * Reference sort order
     */
    @TableField("ord")
    private Short ord;

}