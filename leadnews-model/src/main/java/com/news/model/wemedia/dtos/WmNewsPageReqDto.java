package com.news.model.wemedia.dtos;

import com.news.model.common.dtos.PageRequestDto;
import lombok.Data;

import java.util.Date;

@Data
public class WmNewsPageReqDto extends PageRequestDto {


    private Short status;

    private Date beginPubDate;

    private Date endPubDate;

    private Integer channelId;

    private String keyword;
}