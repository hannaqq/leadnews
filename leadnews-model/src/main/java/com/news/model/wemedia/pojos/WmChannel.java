package com.news.model.wemedia.pojos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Channel information entity
 *
 * @author itheima
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "wm_channel")
public class WmChannel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * Channel name
     */
    @Column(name = "name")
    private String name;

    /**
     * Channel description
     */
    @Column(name = "description")
    private String description;

    /**
     * Whether default channel
     * 1: Default (true)
     * 0: Non-default (false)
     */
    @Column(name = "is_default")
    private Boolean isDefault;

    /**
     * Whether enabled
     * 1: Enabled (true)
     * 0: Disabled (false)
     */
    @Column(name = "status")
    private Boolean status;

    /**
     * Default sort order
     */
    @Column(name = "ord")
    private Integer ord;

    /**
     * Creation time
     */
    @Column(name = "created_time")
    private Date createdTime;

}
