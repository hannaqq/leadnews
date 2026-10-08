package com.news.wemedia.service;

import com.news.model.wemedia.pojos.WmNews;

import java.util.Date;

public interface WmNewsPublishService {

    void reviewApproved(WmNews news);

    void publishScheduled(Integer newsId, Date expectedPublishTime);
}
