package com.news.wemedia.service;

import com.news.model.wemedia.pojos.WmNews;

import java.util.Date;

public interface WmNewsPublishService {

    void reviewApproved(WmNews news);

    void reschedule(Integer newsId, Integer userId, Date publishTime);

    void cancelSchedule(Integer newsId, Integer userId);

    void cancelObsoleteTask(Long taskId);

    void publishScheduled(Integer newsId, Date expectedPublishTime, long scheduleVersion);
}
