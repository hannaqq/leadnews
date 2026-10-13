package com.news.model.wemedia.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsPublishTaskPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer newsId;
    private Date expectedPublishTime;
    private Long scheduleVersion;
}
