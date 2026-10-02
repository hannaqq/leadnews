package com.news.model.wemedia.pojos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * WeMedia material information entity
 *
 * @author itheima
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "wm_material")
public class WmMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * WeMedia user ID
     */
    @Column(name = "user_id")
    private Integer userId;

    /**
     * Material URL
     */
    @Column(name = "url")
    private String url;

    /**
     * Material type
     * 0: Image
     * 1: Video
     */
    @Column(name = "type")
    private Short type;

    /**
     * Whether collected
     */
    @Column(name = "is_collection")
    private Short isCollection;

    /**
     * Creation time
     */
    @Column(name = "created_time")
    private Date createdTime;

}
