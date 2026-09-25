package com.news.model.wemedia.dtos;

import com.news.model.wemedia.pojos.WmNews;
import lombok.Data;

@Data
public class NewsDto extends WmNews {
    private String authorName;

}
