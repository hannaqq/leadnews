package com.news.wemedia.service.transaction;

import com.news.common.exception.CustomException;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.wemedia.pojos.WmNews;
import com.news.wemedia.repository.WmNewsRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WmNewsScheduleTransactionService {

    private final WmNewsRepository newsRepository;
    private final EntityManager entityManager;

    @Transactional
    public ScheduleChange prepareApprovedSchedule(Integer newsId, Date publishTime) {
        WmNews news = newsRepository.findByIdForUpdate(newsId)
                .orElseThrow(() -> new CustomException(AppHttpCodeEnum.DATA_NOT_EXIST));
        if (news.getStatus() != WmNews.Status.PROCESSING.getCode()) {
            throw new CustomException(AppHttpCodeEnum.PARAM_INVALID);
        }
        return prepareSchedule(news, publishTime);
    }

    @Transactional
    public ScheduleChange reschedule(Integer newsId, Integer userId, Date publishTime) {
        WmNews news = newsRepository.findByIdAndUserIdForUpdate(newsId, userId)
                .orElseThrow(() -> new CustomException(AppHttpCodeEnum.DATA_NOT_EXIST));
        if (news.getStatus() != WmNews.Status.SUCCESS.getCode()
                && news.getStatus() != WmNews.Status.APPROVED_UNSCHEDULED.getCode()) {
            throw new CustomException(AppHttpCodeEnum.PARAM_INVALID);
        }
        return prepareSchedule(news, publishTime);
    }

    @Transactional
    public Long cancelSchedule(Integer newsId, Integer userId) {
        WmNews news = newsRepository.findByIdAndUserIdForUpdate(newsId, userId)
                .orElseThrow(() -> new CustomException(AppHttpCodeEnum.DATA_NOT_EXIST));
        if (news.getStatus() == WmNews.Status.APPROVED_UNSCHEDULED.getCode()) {
            return null;
        }
        if (news.getStatus() != WmNews.Status.SUCCESS.getCode()) {
            throw new CustomException(AppHttpCodeEnum.PARAM_INVALID);
        }

        Long obsoleteTaskId = news.getScheduleTaskId();
        news.setScheduleVersion(nextVersion(news));
        news.setScheduleTaskId(null);
        news.setPublishTime(null);
        news.setStatus(WmNews.Status.APPROVED_UNSCHEDULED.getCode());
        news.setReason("approved; publication schedule cancelled");
        newsRepository.save(news);
        return obsoleteTaskId;
    }

    @Transactional
    public boolean attachTask(Integer newsId, long scheduleVersion, long taskId) {
        WmNews news = newsRepository.findByIdForUpdate(newsId).orElse(null);
        if (news == null
                || news.getStatus() != WmNews.Status.SUCCESS.getCode()
                || !Long.valueOf(scheduleVersion).equals(news.getScheduleVersion())) {
            return false;
        }
        news.setScheduleTaskId(taskId);
        newsRepository.save(news);
        return true;
    }

    @Transactional
    public void markScheduleCreationFailed(Integer newsId, long scheduleVersion) {
        WmNews news = newsRepository.findByIdForUpdate(newsId).orElse(null);
        if (news == null
                || news.getStatus() != WmNews.Status.SUCCESS.getCode()
                || !Long.valueOf(scheduleVersion).equals(news.getScheduleVersion())) {
            return;
        }
        news.setScheduleVersion(nextVersion(news));
        news.setScheduleTaskId(null);
        news.setPublishTime(null);
        news.setStatus(WmNews.Status.APPROVED_UNSCHEDULED.getCode());
        news.setReason("approved; publication could not be scheduled");
        newsRepository.save(news);
    }

    @Transactional
    public Optional<WmNews> claimPublication(
            Integer newsId, Date expectedPublishTime, long scheduleVersion) {
        int rows = newsRepository.claimScheduledPublication(
                newsId,
                expectedPublishTime,
                scheduleVersion,
                WmNews.Status.SUCCESS.getCode(),
                WmNews.Status.PUBLISHING.getCode());
        if (rows == 0) {
            return Optional.empty();
        }
        return newsRepository.findById(newsId);
    }

    @Transactional
    public boolean restoreWaitingForRetry(Integer newsId, long scheduleVersion) {
        WmNews news = newsRepository.findByIdForUpdate(newsId).orElse(null);
        if (news == null
                || news.getStatus() != WmNews.Status.PUBLISHING.getCode()
                || !Long.valueOf(scheduleVersion).equals(news.getScheduleVersion())) {
            return false;
        }
        news.setStatus(WmNews.Status.SUCCESS.getCode());
        news.setReason("publication failed; waiting to retry");
        newsRepository.save(news);
        return true;
    }

    @Transactional
    public boolean completePublication(Integer newsId, long scheduleVersion, long articleId) {
        WmNews news = newsRepository.findByIdForUpdate(newsId).orElse(null);
        if (news == null
                || news.getStatus() != WmNews.Status.PUBLISHING.getCode()
                || !Long.valueOf(scheduleVersion).equals(news.getScheduleVersion())) {
            return false;
        }
        news.setArticleId(articleId);
        news.setScheduleTaskId(null);
        news.setStatus(WmNews.Status.PUBLISHED.getCode());
        news.setReason("processed");
        newsRepository.save(news);
        return true;
    }

    private ScheduleChange prepareSchedule(WmNews news, Date publishTime) {
        Long obsoleteTaskId = news.getScheduleTaskId();
        long version = nextVersion(news);
        news.setPublishTime(publishTime);
        news.setScheduleVersion(version);
        news.setScheduleTaskId(null);
        news.setStatus(WmNews.Status.SUCCESS.getCode());
        news.setReason("approved and waiting for publication");
        newsRepository.saveAndFlush(news);
        entityManager.refresh(news);
        return new ScheduleChange(
                news.getId(), obsoleteTaskId, version, news.getPublishTime());
    }

    private long nextVersion(WmNews news) {
        return news.getScheduleVersion() == null ? 1L : news.getScheduleVersion() + 1;
    }

    public record ScheduleChange(
            Integer newsId, Long obsoleteTaskId, long scheduleVersion, Date publishTime) {
    }
}
