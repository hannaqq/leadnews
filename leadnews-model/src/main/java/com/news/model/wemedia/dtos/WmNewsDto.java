package com.news.model.wemedia.dtos;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class WmNewsDto {
    
    private Integer id;
    private String title;
    private String content;
    private Integer channelId;
    private String labels;
    private Date publishTime;
    private Date submitedTime;
    private List<String> images;

    /**
     * 0 no image 1 single image 3 multi images -1 auto
     */
    private Short type;
     /**
     * upload: 1  draft:0
     */
    private Short status;

    /**
     * 0down 1up
     */
    private Short enable;
}