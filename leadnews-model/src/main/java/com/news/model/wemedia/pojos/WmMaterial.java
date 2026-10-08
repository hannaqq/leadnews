package com.news.model.wemedia.pojos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "wm_material")
public class WmMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;

    private String url;

    /**
     * Material type
     * 0: Image
     * 1: Video
     */
    private Short type;

    private Short isCollection;

    private Date createdTime;

}
