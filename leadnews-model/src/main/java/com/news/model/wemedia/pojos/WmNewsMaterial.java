package com.news.model.wemedia.pojos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * WeMedia news material reference entity
 *
 * @author itheima
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "wm_news_material")
public class WmNewsMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * Material ID
     */
    @Column(name = "material_id")
    private Integer materialId;

    /**
     * News ID
     */
    @Column(name = "news_id")
    private Integer newsId;

    /**
     * Reference type
     * 0: Content reference
     * 1: Cover image reference
     */
    @Column(name = "type")
    private Short type;

    /**
     * Reference sort order
     */
    @Column(name = "ord")
    private Short ord;

}
